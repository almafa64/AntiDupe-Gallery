package com.cyberegylet.antiDupeGallery.helpers

import android.provider.MediaStore
import com.cyberegylet.antiDupeGallery.backend.Config
import com.cyberegylet.antiDupeGallery.models.Album
import com.cyberegylet.antiDupeGallery.models.ImageFile

object ConfigSort
{
	@JvmStatic
	fun toConfigString(isAscending: Boolean, sortType: SortType): String = isAscending.toString() + sortType.ordinal

	@JvmStatic
	fun getSortType(configString: String): SortType = SortType.values()[configString[1].toString().toInt()]

	@JvmStatic
	fun isAscending(configString: String): Boolean = configString[0] == '1'

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

	@JvmStatic
	fun toMediaSQLString(configString: String): String
	{
		var sort = when (getSortType(configString))
		{
			SortType.MODIFICATION_DATE -> "mtime"
			SortType.CREATION_DATE -> "ctime"
			SortType.SIZE -> "size"
			SortType.NAME -> "name"
		}
		if (!isAscending(configString)) sort += " DESC"
		return sort
	}

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