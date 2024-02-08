package com.cyberegylet.antiDupeGallery.helpers

import android.provider.MediaStore
import com.cyberegylet.antiDupeGallery.backend.Cache
import com.cyberegylet.antiDupeGallery.backend.Config
import com.cyberegylet.antiDupeGallery.models.Album
import com.cyberegylet.antiDupeGallery.models.ImageFile

object ConfigSort
{
	/**
	 * Converts isAscending and SortType into String that can be saved in Config.
	 * @return configString
	 */
	@JvmStatic
	fun toConfigString(isAscending: Boolean, sortType: SortType): String = isAscending.toString() + sortType.ordinal

	/**
	 * @param configString sorting Property from Config
	 */
	@JvmStatic
	fun getSortType(configString: String): SortType = SortType.entries[configString[1].toString().toInt()]

	/**
	 * @param configString sorting Property from Config
	 */
	@JvmStatic
	fun isAscending(configString: String): Boolean = configString[0] == '1'

	/**
	 * Turns configString into SQL sort by field for MediaStore
	 * @param configString sorting Property from Config
	 * @return MediaStore sql sort by compatible String
	 */
	@JvmStatic
	fun toSQLString(configString: String): String
	{
		var sort = when (getSortType(configString))
		{
			SortType.MODIFICATION_DATE -> MediaStore.MediaColumns.DATE_MODIFIED
			SortType.CREATION_DATE -> MediaStore.MediaColumns.DATE_TAKEN
			SortType.SIZE -> MediaStore.MediaColumns.SIZE
			SortType.NAME -> MediaStore.MediaColumns.DISPLAY_NAME
		}
		if (!isAscending(configString)) sort += " DESC"
		return sort
	}

	/**
	 * Turns configString into SQL sort by field for Cache
	 * @param configString sorting Property from Config
	 * @return Cache sql sort by compatible String
	 */
	@JvmStatic
	fun toMediaSQLString(configString: String): String
	{
		var sort = when (getSortType(configString))
		{
			SortType.MODIFICATION_DATE -> Cache.Media.MODIFICATION_TIME
			SortType.CREATION_DATE -> Cache.Media.CREATION_TIME
			SortType.SIZE -> Cache.Media.SIZE
			SortType.NAME -> Cache.Media.NAME
		}
		if (!isAscending(configString)) sort += " DESC"
		return sort
	}

	/**
	 * Gets Album Comparator from Config
	 */
	@JvmStatic
	val albumComparator: Comparator<Album>
		get()
		{
			val sortData = Config.getStringProperty(Config.Property.ALBUM_SORT)
			var comparator = when (getSortType(sortData))
			{
				SortType.MODIFICATION_DATE, SortType.CREATION_DATE -> Comparator.comparing(Album::modifiedDate)
				SortType.SIZE -> Comparator.comparing(Album::size)
				SortType.NAME -> Comparator.comparing { f: Album -> f.name.lowercase() }
			}
			if (!isAscending(sortData)) comparator = comparator.reversed()
			return comparator
		}

	/**
	 * Gets Image Comparator from Config
	 */
	@JvmStatic
	val imageComparator: Comparator<ImageFile>
		get()
		{
			val sortData = Config.getStringProperty(Config.Property.IMAGE_SORT)
			var comparator = when (getSortType(sortData))
			{
				SortType.MODIFICATION_DATE -> Comparator.comparing(ImageFile::modifiedDate)
				SortType.CREATION_DATE -> Comparator.comparing(ImageFile::creationDate)
				SortType.SIZE -> Comparator.comparing(ImageFile::size)
				SortType.NAME -> Comparator.comparing { f: ImageFile -> f.name.lowercase() }
			}
			if (!isAscending(sortData)) comparator = comparator.reversed()
			return comparator
		}

	enum class SortType
	{
		MODIFICATION_DATE,
		CREATION_DATE,
		SIZE,
		NAME
	}
}