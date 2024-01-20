package com.cyberegylet.antiDupeGallery.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Date;

public class Utils
{
	public static String msToDate(long ms)
	{
		return DateFormat.getDateTimeInstance().format(new Date(ms));
	}

	public static String getByteStringFromSize(long size)
	{
		final double kb = 1024;
		if (size >= kb * kb * kb * kb) return doubleString(size / (kb * kb * kb * kb)) + " TB";
		else if (size >= kb * kb * kb) return doubleString(size / (kb * kb * kb)) + " GB";
		else if (size >= kb * kb) return doubleString(size / (kb * kb)) + " MB";
		else if (size >= kb) return doubleString(size / kb) + " KB";
		return doubleString((double) size) + " B";
	}

	private static String doubleString(double d)
	{
		return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).toPlainString() + "";
	}
}
