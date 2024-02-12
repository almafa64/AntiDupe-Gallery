package com.cyberegylet.antiDupeGallery.backend

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cyberegylet.antiDupeGallery.models.Album
import com.cyberegylet.antiDupeGallery.models.ImageFile

object Cache
{
	const val DATABASE_NAME = "data.db"
	private var _database: SQLiteDatabase? = null

	object Tables
	{
		const val DIGESTS = "digests"
		const val ALBUMS = "albums"
		const val MEDIA = "media"
	}

	object Albums
	{
		const val NAME = "name"
		const val PATH = "path"
		const val SIZE = "size"
		const val MODIFICATION_TIME = "mtime"
		const val MEDIA_COUNT = "mediaCount"
	}

	object Media
	{
		const val NAME = Albums.NAME
		const val PATH = Albums.PATH
		const val SIZE = Albums.SIZE
		const val MODIFICATION_TIME = Albums.MODIFICATION_TIME

		const val ID = "id"
		const val ALBUM_PATH = "album_path"
		const val CREATION_TIME = "ctime"
		const val MIME_TYPE = "mimeType"
		const val C_HASH = "chash"
		const val P_HASH = "phash"
	}

	object Digests
	{
		const val ID = Media.ID
		const val PATH = Media.PATH
		const val DIGEST = "digest"
	}

	@JvmStatic
	val cache
		get() = _database!!

	@JvmStatic
	fun init(activity: Activity)
	{
		if (_database != null) return
		_database = SQLiteDatabase.openOrCreateDatabase(activity.getDatabasePath(DATABASE_NAME), null)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS ${Tables.ALBUMS} (" +
					"${Albums.NAME} TEXT," +
					"${Albums.PATH} TEXT PRIMARY KEY, " +
					"${Albums.MODIFICATION_TIME} INTEGER," +
					"${Albums.MEDIA_COUNT} INTEGER," +
					"${Albums.SIZE} INTEGER" +
					")"
		)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS ${Tables.MEDIA} (" +
					"${Media.ID} INTEGER PRIMARY KEY," +
					"${Media.ALBUM_PATH} TEXT," +
					"${Media.NAME} TEXT," +
					"${Media.PATH} TEXT," +
					"${Media.CREATION_TIME} INTEGER," +
					"${Media.MODIFICATION_TIME} INTEGER," +
					"${Media.SIZE} INTEGER," +
					"${Media.MIME_TYPE} TEXT," +
					"${Media.C_HASH} BLOB," +
					"${Media.P_HASH} BLOB," +
					"FOREIGN KEY(${Media.ALBUM_PATH}) REFERENCES ${Tables.ALBUMS}(${Albums.PATH})" +
					")"
		)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS ${Tables.DIGESTS} (" +
					"${Digests.ID} INTEGER, ${Digests.PATH} TEXT, ${Digests.DIGEST} BLOB)"
		)
	}

	/**
	 * @param name the name of the database
	 * @param activity calling activity instance
	 * @return absolute path of database
	 */
	@JvmStatic
	fun getDbPath(name: String, activity: Activity): String = activity.getDatabasePath(name).absolutePath

	/**
	 * @param imageFile the imageFile to delete
	 */
	@JvmStatic
	fun deleteMedia(imageFile: ImageFile) = delete(Tables.MEDIA, imageFile.path)

	/**
	 * @param imageFile the imageFile containing new data
	 * @param oldPath the path that is in the cache
	 */
	@JvmStatic
	fun updateMedia(imageFile: ImageFile, oldPath: String)
	{
		val values = ContentValues()
		values.put(Media.PATH, imageFile.path)
		values.put(Media.ALBUM_PATH, imageFile.file.parent)
		values.put(Media.SIZE, imageFile.size)
		values.put(Media.MIME_TYPE, imageFile.mime)
		values.put(Media.MODIFICATION_TIME, imageFile.modifiedDate)
		cache.update(Tables.MEDIA, values, "${Media.PATH} = ?", arrayOf(oldPath))
	}

	/**
	 * @param imageFile the imageFile to check
	 * @return true if stored imageFile is out of date, false otherwise
	 */
	@JvmStatic
	fun checkIfOutDated(imageFile: ImageFile): Boolean
	{
		cache.rawQuery(
			"select ${Media.MODIFICATION_TIME} from ${Tables.MEDIA} where ${Media.ID} = ${imageFile.id}", null
		).use { cursor ->
			if (!cursor.moveToFirst()) return false
			return cursor.getLong(0) != imageFile.modifiedDate
		}
	}

	/**
	 * @param imageFile the imageFile to check and update
	 */
	@JvmStatic
	fun updateOutDated(imageFile: ImageFile)
	{
		if (checkIfOutDated(imageFile)) updateMedia(imageFile, imageFile.path)
	}

	/**
	 * @param imageFile the imageFile to add to database
	 * @param album containing the imageFile
	 */
	@JvmStatic
	fun addMedia(imageFile: ImageFile, album: Album)
	{
		cache.rawQuery(
			"select count(*) from ${Tables.MEDIA} where ${Media.ID} = ${imageFile.id}",
			null
		).use { cursor ->
			cursor.moveToFirst()
			if (cursor.getInt(0) > 0) return
		}
		val values = ContentValues()
		values.put(Media.PATH, imageFile.path)
		values.put(Media.NAME, imageFile.name)
		values.put(Media.CREATION_TIME, imageFile.creationDate)
		values.put(Media.MODIFICATION_TIME, imageFile.modifiedDate)
		values.put(Media.SIZE, imageFile.size)
		values.put(Media.MIME_TYPE, imageFile.mime)
		values.put(Media.ALBUM_PATH, album.path)
		values.put(Media.ID, imageFile.id)
		insert(Tables.MEDIA, values)
	}

	/**
	 * @param album the album to delete
	 */
	@JvmStatic
	fun deleteAlbum(album: Album)
	{
		cache.delete(Tables.MEDIA, "${Media.ALBUM_PATH} = ?", arrayOf(album.path))
		delete(Tables.ALBUMS, album.path)
	}

	/**
	 * @param album the album with new values
	 * @param oldPath the path that is in the cache
	 */
	@JvmStatic
	fun updateAlbum(album: Album, oldPath: String)
	{
		val values = ContentValues()
		values.put(Media.ALBUM_PATH, album.path)
		cache.update(Tables.MEDIA, values, "${Media.ALBUM_PATH} = ?", arrayOf(oldPath))
		val values2 = ContentValues()
		values2.put(Albums.PATH, album.path)
		cache.update(Tables.ALBUMS, values2, "${Albums.PATH} = ?", arrayOf(oldPath))
	}

	/**
	 * @param album the album to add to cache
	 */
	@JvmStatic
	fun addAlbum(album: Album)
	{
		cache.rawQuery(
			"select count(*) from ${Tables.ALBUMS} where ${Albums.PATH} = ?", arrayOf(album.path)
		).use { cursor ->
			cursor.moveToFirst()
			if (cursor.getInt(0) > 0) return
		}
		val values = ContentValues()
		values.put(Albums.PATH, album.path)
		values.put(Albums.NAME, album.name)
		values.put(Albums.MODIFICATION_TIME, album.modifiedDate)
		values.put(Albums.SIZE, album.size)
		values.put(Albums.MEDIA_COUNT, album.count)
		insert(Tables.ALBUMS, values)
	}

	private fun insert(table: String, values: ContentValues)
	{
		cache.insert(table, null, values)
	}

	private fun delete(table: String, path: String)
	{
		cache.delete(table, "${Media.PATH} like ?", arrayOf(path))
	}
}