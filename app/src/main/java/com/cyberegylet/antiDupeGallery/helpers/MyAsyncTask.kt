package com.cyberegylet.antiDupeGallery.helpers

abstract class MyAsyncTask
{
	private var shouldStop: Boolean = false
	var thread: Thread? = null
		private set

	fun execute()
	{
		thread = Thread {
			onPreExecute()
			if (!shouldStop) doInBackground()
			if (!shouldStop) onPostExecute()
		}
		thread?.start()
	}

	abstract fun doInBackground()
	abstract fun onPostExecute()
	abstract fun onPreExecute()

	/**
	 * sets thread stop variable, which will block remaining steps
	 */
	fun stop() { shouldStop = true }

	fun running(): Boolean = thread?.isAlive ?: false
}