package com.cyberegylet.antiDupeGallery.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.activities.FilterActivity

/*class Test(val name: String)
{

}*/

class FilterService : Service()
{
	companion object
	{
		/*@JvmField
		var mutableLiveData = MutableLiveData<Test>()*/

		const val ID = 6969
		const val ACTION_START_FILTERING = "action_filtering"

		@JvmStatic
		var CHANNEL_ID: String? = null
			private set

		private var isRunning: Boolean = false
		private var mNotificationManager: NotificationManager? = null

	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		if (intent?.action == ACTION_START_FILTERING) start()
		else stop()
		return START_STICKY
	}

	private fun start()
	{
		if (isRunning) return
		isRunning = true

		CHANNEL_ID = resources.getText(R.string.default_notification_channel_id).toString()
		mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

		startForeground(ID, getNotification())

		// ToDo run filtering here + update notification
	}

	private fun getNotification(): Notification
	{
		val notificationIntent = Intent(applicationContext, FilterActivity::class.java)
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

		val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

		val channel = NotificationChannel(CHANNEL_ID, "channel name", NotificationManager.IMPORTANCE_HIGH)
		channel.description = "description"

		mNotificationManager!!.createNotificationChannel(channel)

		val notification = NotificationCompat.Builder(this, CHANNEL_ID!!).setContentTitle("title")
			.setTicker("ticker").setContentText("context").setSmallIcon(android.R.drawable.star_big_on).setLargeIcon(
				Bitmap.createScaledBitmap(
					BitmapFactory.decodeResource(
						resources,
						android.R.drawable.star_big_on
					), 128, 128, false
				)
			).setContentIntent(contentPendingIntent).setOngoing(true).build()
		notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

		return notification
	}

	private fun stop()
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

	override fun onBind(intent: Intent?): IBinder?
	{
		return null
	}
}