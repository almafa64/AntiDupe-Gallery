package com.cyberegylet.antiDupeGallery.helpers;

public abstract class MyAsyncTask
{
	public void execute()
	{
		new Thread(() -> {
			onPreExecute();
			doInBackground();
			onPostExecute();
		}).start();
	}

	public abstract void doInBackground();

	public abstract void onPostExecute();

	public abstract void onPreExecute();
}