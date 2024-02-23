package com.cyberegylet.antiDupeGallery.helpers

abstract class MyAsyncTask
{
	var thread: Thread? = null
		private set

	fun execute()
	{
		thread = Thread {
			onPreExecute()
			if (!stopped()) doInBackground()
			if (!stopped()) onPostExecute()
		}
		thread?.start()
	}

	abstract fun doInBackground()
	abstract fun onPostExecute()
	abstract fun onPreExecute()

	/**
	 * stops the thread (only sets flag -> check with isStopped to see if code should stop)
	 */
	fun stop()
	{
		thread?.interrupt()
		thread = null
	}

	fun running(): Boolean = thread?.isAlive ?: false
	fun stopped() = thread?.isInterrupted ?: true
	fun wait() = thread?.join()
}