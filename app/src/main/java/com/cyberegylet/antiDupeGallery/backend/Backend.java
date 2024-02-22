package com.cyberegylet.antiDupeGallery.backend;

import com.cyberegylet.antiDupeGallery.activities.AlbumActivity;

public class Backend
{
	static
	{
		System.loadLibrary("backend");
	}

	public static final class Digest
	{
		public final byte[] digest;

		private Digest(byte[] digest)
		{
			this.digest = digest;
		}
	}

	public static final class HashStatus {
		public final long totalCount;
		public final long completed;

		private HashStatus(long totalCount, long completed) { this.totalCount = totalCount; this.completed = completed; }
	}

	public static native void init(String dbPath);
	public static native void runHashProcess(boolean chash, boolean phash);
	public static native void stopHashProcess();
	public static native HashStatus getHashStatus();

	public static native void shutdown();
}
