package com.cyberegylet.antiDupeGallery.backend;

import com.cyberegylet.antiDupeGallery.activities.FoldersActivity;

public class Backend
{
	static
	{
		System.loadLibrary("backend");
	}

	public static final class Digest
	{
		public byte[] digest;

		private Digest(byte[] digest)
		{
			this.digest = digest;
		}
	}

	public static native void init(FoldersActivity mainActivity);

	public static native void queueFile(long id, String path);

	public static native long getQueuedFileProgress();

	public static native void shutdown();
}
