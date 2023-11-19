package com.cyberegylet.antiDupeGallery.helpers;

import android.provider.MediaStore;

public class ConfigSort
{
	public enum SortType
	{
		MODIFICATION_DATE,
		CREATION_DATE,
		SIZE,
		NAME
	}

	public static String toConfigString(boolean isAscending, SortType sortType)
	{
		return String.valueOf(isAscending) + sortType.ordinal();
	}

	public static SortType getSortType(String configString)
	{
		return SortType.values()[Integer.parseInt(String.valueOf(configString.charAt(1)))];
	}

	public static boolean isAscending(String configString)
	{
		return configString.charAt(0) == '1';
	}

	public static String toSQLString(String configString)
	{
		String sort = "";
		switch (ConfigSort.getSortType(configString))
		{
			case MODIFICATION_DATE:
				sort = MediaStore.MediaColumns.DATE_MODIFIED;
				break;
			case CREATION_DATE:
				sort = MediaStore.MediaColumns.DATE_TAKEN;
				break;
			case SIZE:
				sort = MediaStore.MediaColumns.SIZE;
				break;
			case NAME:
				sort = MediaStore.MediaColumns.DISPLAY_NAME;
				break;
		}
		if (!ConfigSort.isAscending(configString)) sort += " DESC";
		return sort;
	}
}