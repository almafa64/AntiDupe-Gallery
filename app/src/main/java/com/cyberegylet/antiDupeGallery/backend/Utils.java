package com.cyberegylet.antiDupeGallery.backend;

public class Utils
{
	public static String getByteStringFromSize(long size)
	{
		final float kb = 1024;
		if(size >= kb * kb * kb * kb) return (size / (kb * kb * kb * kb)) + " TB";
		else if(size >= kb * kb * kb) return (size / (kb * kb * kb)) + " GB";
		else if(size >= kb * kb) return (size / (kb * kb)) + " MB";
		else if(size >= kb) return (size / kb) + " KB";
		return (float)size + " B";
	}
}
