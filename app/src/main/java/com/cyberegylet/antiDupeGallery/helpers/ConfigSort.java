package com.cyberegylet.antiDupeGallery.helpers;

import android.provider.MediaStore;

import com.cyberegylet.antiDupeGallery.backend.Config;
import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

import java.util.Comparator;
import java.util.Locale;

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

	public static Comparator<Album> getAlbumComparator()
	{
		String sortData = Config.getStringProperty(Config.Property.ALBUM_SORT);
		Comparator<Album> comparator;
		switch (ConfigSort.getSortType(sortData))
		{
			case MODIFICATION_DATE:
				comparator = Comparator.comparing(Album::getModifiedDate);
				break;
			case CREATION_DATE:
				comparator = Comparator.comparing(Album::getCreationDate);
				break;
			case SIZE:
				comparator = Comparator.comparing(Album::getSize);
				break;
			case NAME:
				comparator = Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT));
				break;
			default:
				throw new RuntimeException("Bad folder sorting");
		}
		if (!ConfigSort.isAscending(sortData)) comparator = comparator.reversed();
		return comparator;
	}

	public static Comparator<ImageFile> getImageComparator()
	{
		String sortData = Config.getStringProperty(Config.Property.IMAGE_SORT);
		Comparator<ImageFile> comparator;
		switch (ConfigSort.getSortType(sortData))
		{
			case MODIFICATION_DATE:
				comparator = Comparator.comparing(ImageFile::getModifiedDate);
				break;
			case CREATION_DATE:
				comparator = Comparator.comparing(ImageFile::getCreationDate);
				break;
			case SIZE:
				comparator = Comparator.comparing(ImageFile::getSize);
				break;
			case NAME:
				comparator = Comparator.comparing(f -> f.getName().toLowerCase(Locale.ROOT));
				break;
			default:
				throw new RuntimeException("Bad folder sorting");
		}
		if (!ConfigSort.isAscending(sortData)) comparator = comparator.reversed();
		return comparator;
	}
}