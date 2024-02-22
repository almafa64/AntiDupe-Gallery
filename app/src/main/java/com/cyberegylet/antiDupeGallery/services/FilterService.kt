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
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
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

class TestReceiver : BroadcastReceiver()
{
	override fun onReceive(context: Context?, intent: Intent?)
	{
		if (intent == null) return
		when (intent.action)
		{
			FilterService.ACTION_STOP_FILTERING ->
			{
				FilterService.filterService?.stopNotification()
				FilterService.filterService?.stop()
			}
		}
	}
}

class FilterService : Service()
{
	companion object
	{
		@JvmField
		var mutableLiveData = MutableLiveData<RecyclerView>()

		@JvmStatic
		var filterService: FilterService? = null
			private set

		const val ID = 6969
		const val ACTION_START_FILTERING = "action_start_filtering"
		const val ACTION_STOP_FILTERING = "action_stop_filtering"
		const val PATHS_PARAM = "paths"

		@JvmStatic
		var CHANNEL_ID: String? = null
			private set

		private var paths: Array<String>? = null
		private var isRunning: Boolean = false
		private var mNotificationManager: NotificationManager? = null
		private var db: SQLiteDatabase? = null
		private var intent: Intent? = null
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

		CHANNEL_ID = resources.getText(R.string.default_notification_channel_id).toString()
		mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
		db = Cache.cache
		filterService = this

		val notificationIntent = Intent(applicationContext, FilterActivity::class.java)
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

		val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

		val channel = NotificationChannel(CHANNEL_ID, "filtering status channel", NotificationManager.IMPORTANCE_LOW)
		channel.enableLights(true)
		channel.lightColor = Color.BLUE
		mNotificationManager!!.createNotificationChannel(channel)

		val stopIntent = Intent(this, TestReceiver::class.java)
		stopIntent.action = ACTION_STOP_FILTERING
		val stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

		notificationBuilder =
			NotificationCompat.Builder(this, CHANNEL_ID!!).setContentTitle(getText(R.string.notifications_filtering))
				.setSmallIcon(R.drawable.ic_adg_logo_foreground).setColor(getColor(R.color.blue_500))
				.setContentIntent(contentPendingIntent).setOngoing(true)
				.addAction(R.drawable.ic_info, "X", stopPendingIntent)

		startForeground(ID, getNotification())

		val albums = (mutableLiveData.value?.adapter as FilterAdapter).albums

		//val idk = Handler(Looper.getMainLooper())

		val show = object : MyAsyncTask()
		{
			var count = 0

			@SuppressLint("NotifyDataSetChanged")
			override fun doInBackground()
			{
				db?.query(
					Cache.Tables.DIGESTS,
					arrayOf(Cache.Digests.PATH, "HEX(${Cache.Digests.DIGEST})", "COUNT(${Cache.Digests.DIGEST})"),
					null,
					null,
					Cache.Digests.DIGEST,
					"COUNT(${Cache.Digests.DIGEST}) > 1",
					"COUNT(${Cache.Digests.DIGEST}) desc"
				).use {
					if (it == null || !it.moveToFirst()) return
					val pathCol = it.getColumnIndexOrThrow(Cache.Digests.PATH)
					val digestCol = it.getColumnIndexOrThrow("HEX(${Cache.Digests.DIGEST})")
					val countCol = it.getColumnIndexOrThrow("COUNT(${Cache.Digests.DIGEST})")

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
						}
						else
						{
							count++
							albums.add(
								FilteredAlbum(
									f,
									"${getText(R.string.texts_group)} $count",
									it.getInt(countCol),
									hex
								)
							)
							albums.sortByDescending { a -> a.count }
						}

						Log.d("app", "looper: ${Looper.getMainLooper()}")
						Handler(Looper.getMainLooper()).post {
							Log.d("app", "updating")
							(mutableLiveData.value as RecyclerView).adapter!!.notifyDataSetChanged()
						}

					} while (it.moveToNext())
				}
			}

			override fun onPostExecute() = Unit
			override fun onPreExecute() = Unit
		}

		object : MyAsyncTask()
		{
			var maxFiles: Int = 0

			override fun doInBackground()
			{
				var old = Backend.getQueuedFileProgress()
				while (true)
				{
					val files = Backend.getQueuedFileProgress()
					if (files == 0L) break
					if (old - files > 1L)
					{
						old = files
						mNotificationManager?.notify(ID, getNotification(maxFiles, (maxFiles - files).toInt()))
						if (!show.running()) show.running()
					}
				}
			}

			override fun onPostExecute()
			{
				show.thread?.join()
				show.execute()
				stopNotification()
				this@FilterService.stop()
			}

			override fun onPreExecute()
			{
				db?.query(Cache.Tables.MEDIA, arrayOf(Cache.Media.PATH, Cache.Media.ID), null, null, null, null, null)
					.use {
						if (it?.moveToFirst() != true)
						{
							this@FilterService.stop()
							stop()
							return
						}

						val pathCol = it.getColumnIndex(Cache.Media.PATH)
						val idCol = it.getColumnIndex(Cache.Media.ID)
						maxFiles = it.count

						do
						{
							Backend.queueFile(it.getLong(idCol), it.getString(pathCol))
						} while (it.moveToNext())
					}
			}
		}.execute()
	}

	fun stopNotification()
	{
		// ToDo set notification to completed
		// ToDo click = open FilterActivity and close notification
		mNotificationManager?.cancel(ID)
		mNotificationManager?.deleteNotificationChannel(CHANNEL_ID);
	}

	private fun getNotification(max: Int = -1, current: Int = -1): Notification?
	{
		if (max >= 0 && current >= 0)
		{
			notificationBuilder?.setProgress(max, current, false)
				?.setContentText("$current/$max ${getText(R.string.texts_files)}")
		}

		val notification = notificationBuilder?.build() ?: return null
		notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

		return notification
	}

	fun stop()
	{
		stopForeground(STOP_FOREGROUND_REMOVE)
		stopSelf()
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