package com.cyberegylet.antiDupeGallery.helpers

abstract class MyAsyncTask
{
	fun execute()
	{
		Thread {
			onPreExecute()
			doInBackground()
			onPostExecute()
		}.start()
	}

	abstract fun doInBackground()
	abstract fun onPostExecute()
	abstract fun onPreExecute()
}