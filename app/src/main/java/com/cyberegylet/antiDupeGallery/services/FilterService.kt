package com.cyberegylet.antiDupeGallery.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.activities.FilterActivity
import com.cyberegylet.antiDupeGallery.adapters.FilterAdapter
import com.cyberegylet.antiDupeGallery.backend.Backend
import com.cyberegylet.antiDupeGallery.backend.Cache
import com.cyberegylet.antiDupeGallery.helpers.MyAsyncTask
import com.cyberegylet.antiDupeGallery.models.FilteredAlbum
import java.io.File
import java.util.Timer
import java.util.TimerTask

class StopReceiver : BroadcastReceiver()
{
	override fun onReceive(context: Context?, intent: Intent?)
	{
		if (intent == null || context == null) return
		when (intent.action)
		{
			FilterService.ACTION_STOP_FILTERING ->
			{
				val filterService = FilterService.filterService!!
				if (FilterService.isFilterActivityOpen) filterService.stop()
				else filterService.endNotification()
			}
		}
	}
}

class FilterService : Service()
{
	companion object
	{
		@JvmStatic
		var recyclerViewMutable = MutableLiveData<RecyclerView>()
			private set

		@JvmField
		var isFilterActivityOpen = false

		@JvmStatic
		var notificationManager: NotificationManager? = null
			private set

		@JvmStatic
		var filterService: FilterService? = null
			private set

		@JvmStatic
		var isRunning: Boolean = false
			private set

		const val NOTIFY_ID = 6969
		const val CHANNEL_ID = "filter_channel"
		const val ACTION_START_FILTERING = "action_start_filtering"
		const val ACTION_STOP_FILTERING = "action_stop_filtering"
		const val PATHS_PARAM = "paths"
		const val FILTER_DONE_PARAM = "filter_done"

		private var paths: Array<String>? = null
		private var db: SQLiteDatabase? = null
		private var intent: Intent? = null
		private var showThread: MyAsyncTask? = null
		private var filterThread: MyAsyncTask? = null
	}

	private var notificationBuilder: NotificationCompat.Builder? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (intent?.action == ACTION_START_FILTERING)
		{
			if (!isRunning) FilterService.intent = intent
			start()
		}
		else stop()
		return START_STICKY
	}

	private fun start()
	{
		if (isRunning) return
		isRunning = true

		paths = intent?.getStringArrayExtra(PATHS_PARAM)

		notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		db = Cache.cache
		filterService = this

		val notificationIntent = Intent(applicationContext, FilterActivity::class.java)
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

		val channel =
			NotificationChannel(CHANNEL_ID, "filtering status channel", NotificationManager.IMPORTANCE_DEFAULT)
		channel.enableLights(true)
		channel.lightColor = Color.BLUE
		notificationManager!!.createNotificationChannel(channel)

		val stopIntent = Intent(this, StopReceiver::class.java)
		stopIntent.action = ACTION_STOP_FILTERING
		val stopPendingIntent = PendingIntent.getBroadcast(
			this,
			0,
			stopIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		notificationBuilder =
			NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(getText(R.string.notifications_filtering))
				.setSmallIcon(R.drawable.ic_adg_logo_foreground).setColor(getColor(R.color.blue_500))
				.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
				.setContentIntent(contentPendingIntent).setOngoing(true)
				.addAction(R.drawable.ic_info, getText(R.string.texts_stop), stopPendingIntent)

		var maxFiles = 0L
		var filesGlobal = 0L

		startForeground(NOTIFY_ID, getNotification(text = getString(R.string.notifications_filtering_starting)))

		val albums = (recyclerViewMutable.value?.adapter as FilterAdapter).albums

		val handler = Handler(Looper.getMainLooper())

		showThread = object : MyAsyncTask()
		{
			var count = 0
			var fileCount = 0L

			@SuppressLint("NotifyDataSetChanged")
			override fun doInBackground()
			{
				db?.rawQuery(
					"""SELECT ${Cache.Media.PATH},
							HEX(${Cache.Tables.CHASH}.${Cache.CHash.BYTES}) AS digest,
							COUNT(${Cache.Tables.CHASH}.${Cache.CHash.BYTES}) AS dup_count
							FROM ${Cache.Tables.MEDIA}
							INNER JOIN ${Cache.Tables.CHASH}
							ON ${Cache.Tables.CHASH}.${Cache.CHash.MEDIA_ID} = ${Cache.Tables.MEDIA}.${Cache.Media.ID}
							GROUP BY digest
							HAVING dup_count > 1
							ORDER BY dup_count DESC""",
					null
				).use {
					if (it == null || !it.moveToFirst()) return
					val pathCol = it.getColumnIndexOrThrow(Cache.Digests.PATH)
					val digestCol = it.getColumnIndexOrThrow("digest")
					val countCol = it.getColumnIndexOrThrow("dup_count")

					val hasPaths = paths?.isNotEmpty() ?: false

					do
					{
						val path = it.getString(pathCol)
						val f = File(path)
						if (!f.canRead() || (hasPaths && f.parent?.let { it1 -> path.contains(it1) } != true)) continue

						val hex = it.getString(digestCol)

						val album = albums.stream().filter { a -> a.digestHex.equals(hex) }.findAny().orElse(null)

						if (album != null)
						{
							album.setData(null, null, it.getInt(countCol), null)
							if (fileCount++ > 30) fileCount = 0
							else continue
						}
						else
						{
							count++
							albums.add(
								FilteredAlbum(
									f, "${getText(R.string.texts_group)} $count", it.getInt(countCol), hex
								)
							)
							albums.sortByDescending { a -> a.count }
						}

						if (isFilterActivityOpen)
						{
							handler.post {
								(recyclerViewMutable.value as RecyclerView).adapter?.notifyDataSetChanged()
							}
						}

					} while (it.moveToNext())
				}
			}

			override fun onPostExecute() = Unit
			override fun onPreExecute() = Unit
		}

		filterThread = object : MyAsyncTask()
		{
			override fun doInBackground()
			{
				val timer = Timer().also {
					it.schedule(object : TimerTask()
					{
						override fun run()
						{
							notificationManager?.notify(
								NOTIFY_ID,
								getNotification(maxFiles.toInt(), filesGlobal.toInt())
							)
						}
					}, 0, 400)
				}

				var old = Backend.getHashStatus().completed
				while (!stopped())
				{
					val files = Backend.getHashStatus().completed
					if (files == maxFiles)
					{
						showThread?.wait()
						showThread?.execute()
						break
					}
					if (files - old > 1L)
					{
						old = files
						filesGlobal = files
						if (showThread?.running() != true) showThread?.execute()
					}
				}

				timer.also {
					it.cancel()
					it.purge()
				}
			}

			override fun onPostExecute()
			{
				if (isFilterActivityOpen) this@FilterService.stop()
				else this@FilterService.endNotification()
			}

			override fun onPreExecute()
			{
				Backend.runHashProcess(true, false)
				while (!Backend.getHashStatus().running)
				{
					continue
				}
				maxFiles = Backend.getHashStatus().totalCount
			}
		}
		filterThread?.execute()
	}

	fun getNotification(max: Int = -1, current: Int = -1, text: String = ""): Notification?
	{
		if (max >= 0 && current >= 0)
		{
			notificationBuilder?.let {
				it.setProgress(max, current, false)
				it.setContentText("$current/$max ${getText(R.string.texts_files)}")
			}
		}
		else
		{
			notificationBuilder?.let {
				it.setContentText(text)
				it.setProgress(0, 0, false)
			}
		}

		val notification = notificationBuilder?.build() ?: return null
		notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

		return notification
	}

	fun endNotification()
	{
		val intent = Intent(applicationContext, FilterActivity::class.java)
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
		intent.putExtra(FILTER_DONE_PARAM, 1)
		val pIntent =
			PendingIntent.getActivity(
				applicationContext,
				0,
				intent,
				PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
			)

		notificationBuilder?.let {
			it.clearActions()
			it.setContentIntent(pIntent)
			it.setOngoing(false)
			it.setAutoCancel(true)
		}
		notificationManager?.notify(
			NOTIFY_ID,
			getNotification(text = getString(R.string.notifications_filtering_ended))
		)

		Backend.stopHashProcess()
		showThread?.stop()
		filterThread?.stop()
	}

	fun stop()
	{
		Backend.stopHashProcess()
		showThread?.stop()
		filterThread?.stop()
		stopForeground(STOP_FOREGROUND_REMOVE)
		stopSelf()
		notificationManager?.cancel(NOTIFY_ID)
		notificationManager?.deleteNotificationChannel(CHANNEL_ID)
		isRunning = false
	}

	override fun onCreate()
	{
		super.onCreate()
		start()
	}

	override fun onDestroy()
	{
		isRunning = false
		super.onDestroy()
	}

	override fun onBind(intent: Intent?): IBinder? = null
}