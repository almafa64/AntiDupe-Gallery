package com.cyberegylet.antiDupeGallery.backend

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.cyberegylet.antiDupeGallery.models.Album
import com.cyberegylet.antiDupeGallery.models.ImageFile

object Cache
{
	const val DATABASE_NAME = "data.db"
	private var _database: SQLiteDatabase? = null

	object Tables
	{
		const val ALBUMS = "albums"
		const val MEDIA = "media"
		const val CHASH = "chash"
		const val PHASH = "phash"
	}

	object Base
	{
		const val NAME = "name"
		const val PATH = "path"

		//const val ID = "id"
		const val MODIFICATION_TIME = "mtime"
		const val SIZE = "size"
		const val HIDDEN = "hidden"
	}

	object Albums
	{
		const val NAME = Base.NAME
		const val PATH = Base.PATH
		const val SIZE = Base.SIZE
		const val MODIFICATION_TIME = Base.MODIFICATION_TIME
		const val HIDDEN = Base.HIDDEN
		//const val ID = Base.ID

		const val MEDIA_COUNT = "mediaCount"
	}

	object CHash {
		const val MEDIA_ID = "media_id"
		const val BYTES = "bytes"
		const val CALC_MTIME = "calc_mtime"
	}

	object PHash {
		const val MEDIA_ID = "media_id"
		const val BYTES = "bytes"
		const val CALC_MTIME = "calc_mtime"
	}

	object Media
	{
		const val NAME = Base.NAME
		const val PATH = Base.PATH
		const val SIZE = Base.SIZE
		const val MODIFICATION_TIME = Base.MODIFICATION_TIME
		const val HIDDEN = Base.HIDDEN

		//const val ID = Base.ID
		const val ID = "id"

		const val MEDIA_STORE_ID = "store_id"

		const val ALBUM_PATH = "album_path"

		//const val ALBUM_ID = "album_id"
		const val CREATION_TIME = "ctime"

		/** int type. See Mimes.Type */
		const val MIME_TYPE = "mimeType"
		const val C_HASH = "chash"
		const val P_HASH = "phash"
	}

	object Digests
	{
		const val ID = Media.ID
		const val PATH = Base.PATH
		const val DIGEST = "digest"
	}

	private lateinit var mediaInsert: SQLiteStatement
	private lateinit var albumInsert: SQLiteStatement

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
					"${Albums.PATH} TEXT PRIMARY KEY," +
					"${Albums.MODIFICATION_TIME} INTEGER," +
					"${Albums.MEDIA_COUNT} INTEGER," +
					"${Albums.SIZE} INTEGER," +
					"${Albums.HIDDEN} INTEGER" +
					")"
		)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS ${Tables.MEDIA} (" +
					"${Media.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
					"${Media.NAME} TEXT," +
					"${Media.PATH} TEXT UNIQUE," +
					"${Media.CREATION_TIME} INTEGER," +
					"${Media.MODIFICATION_TIME} INTEGER," +
					"${Media.HIDDEN} INTEGER," +
					"${Media.SIZE} INTEGER," +
					"${Media.MIME_TYPE} INTEGER," +
					"${Media.MEDIA_STORE_ID} INTEGER," +
					"${Media.ALBUM_PATH} TEXT," +
					"${Media.C_HASH} BLOB," +
					"${Media.P_HASH} BLOB," +
					"FOREIGN KEY(${Media.ALBUM_PATH}) REFERENCES ${Tables.ALBUMS}(${Albums.PATH})" +
					")"
		)
		//cache.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_${Media.PATH} ON ${Tables.MEDIA}(${Media.PATH})")
		cache.execSQL("CREATE TABLE IF NOT EXISTS ${Tables.CHASH} (" +
				"${CHash.MEDIA_ID} INTEGER PRIMARY KEY," +
				"${CHash.BYTES} BLOB," +
				"${CHash.CALC_MTIME} INTEGER," +
				"FOREIGN KEY(${CHash.MEDIA_ID}) REFERENCES ${Tables.MEDIA}(${Media.ID})" +
				")"
		);
		cache.execSQL("CREATE TABLE IF NOT EXISTS ${Tables.PHASH} (" +
				"${PHash.MEDIA_ID} INTEGER PRIMARY KEY," +
				"${PHash.BYTES} BLOB," +
				"${PHash.CALC_MTIME} INTEGER," +
				"FOREIGN KEY(${PHash.MEDIA_ID}) REFERENCES ${Tables.MEDIA}(${Media.ID})" +
				")"
		);

		mediaInsert = cache.compileStatement(
			"INSERT OR REPLACE INTO ${Tables.MEDIA} (" +
					"${Media.PATH},${Media.NAME},${Media.CREATION_TIME},${Media.MODIFICATION_TIME}," +
					"${Media.MIME_TYPE},${Media.SIZE},${Media.HIDDEN},${Media.ALBUM_PATH}" +
					") VALUES (?,?,?,?,?,?,?,?)"
		)
		albumInsert = cache.compileStatement(
			"INSERT OR REPLACE INTO ${Tables.ALBUMS} (" +
					"${Albums.PATH},${Albums.NAME},${Albums.MODIFICATION_TIME},${Albums.SIZE},${Albums.MEDIA_COUNT}," +
					"${Albums.HIDDEN}) VALUES (?,?,?,?,?,?)"
		)

		cache.enableWriteAheadLogging()
		cache.execSQL("PRAGMA synchronous = NORMAL")
	}

	/**
	 * @param name the name of the database
	 * @param activity calling activity instance
	 * @return absolute path of database
	 */
	@JvmStatic
	fun getDbPath(name: String, activity: Activity): String = activity.getDatabasePath(name).absolutePath

	/**
	 * @param imageFile the imageFile to deleteű
	 * @return number of rows affected
	 */
	@JvmStatic
	fun deleteMedia(imageFile: ImageFile) = delete(Tables.MEDIA, imageFile.path)

	/**
	 * @param imageFile the imageFile containing new data
	 * @return number of rows affected
	 */
	@JvmStatic
	fun updateMedia(imageFile: ImageFile): Int
	{
		val values = ContentValues().apply {
			put(Media.PATH, imageFile.path)
			put(Media.ALBUM_PATH, imageFile.file.parent)
			put(Media.SIZE, imageFile.size)
			put(Media.MIME_TYPE, imageFile.type.ordinal)
			put(Media.MODIFICATION_TIME, imageFile.modifiedDate)
			put(Media.HIDDEN, if (imageFile.isHidden) 1 else 0)
		}
		return cache.update(Tables.MEDIA, values, "${Media.ID} = ${imageFile.id}", null)
	}

	/**
	 * @param imageFile the imageFile to check
	 * @return true if stored imageFile is out of date, false otherwise
	 */
	@JvmStatic
	fun isOutDated(imageFile: ImageFile): Boolean
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
	 * @return number of rows affected
	 */
	@JvmStatic
	fun updateOutDated(imageFile: ImageFile): Int
	{
		return if (isOutDated(imageFile)) updateMedia(imageFile)
		else -1
	}

	/**
	 * @param imageFile the imageFile to add to database
	 * @return new id (gets stored in imageFile.id too)
	 */
	@JvmStatic
	fun addMedia(imageFile: ImageFile): Long
	{
		mediaInsert.bindString(1, imageFile.path)
		mediaInsert.bindString(2, imageFile.name)
		mediaInsert.bindLong(3, imageFile.creationDate)
		mediaInsert.bindLong(4, imageFile.modifiedDate)
		mediaInsert.bindLong(5, imageFile.type.ordinal.toLong())
		mediaInsert.bindLong(6, imageFile.size)
		mediaInsert.bindLong(7, if (imageFile.isHidden) 1 else 0)
		mediaInsert.bindString(8, imageFile.file.parent)

		imageFile.id = mediaInsert.executeInsert()

		mediaInsert.clearBindings()
		return imageFile.id
	}

	/**
	 * @param album the album to delete
	 * @return number of rows affected
	 */
	@JvmStatic
	fun deleteAlbum(album: Album): Int
	{
		cache.delete(Tables.MEDIA, "${Media.ALBUM_PATH} = ?", arrayOf(album.path))
		return delete(Tables.ALBUMS, album.path)
	}

	/**
	 * @param album the album with new values
	 * @param oldPath the path that is in the cache
	 * @return number of rows affected
	 */
	@JvmStatic
	fun updateAlbum(album: Album, oldPath: String): Int
	{
		if (album.path != oldPath)
		{
			val values = ContentValues().apply {
				put(Media.ALBUM_PATH, album.path)
			}
			cache.update(Tables.MEDIA, values, "${Media.ALBUM_PATH} = ?", arrayOf(oldPath))
		}

		val values2 = ContentValues().apply {
			put(Albums.PATH, album.path)
			put(Albums.SIZE, album.size)
			put(Albums.NAME, album.name)
			put(Albums.MEDIA_COUNT, album.count)
			put(Albums.MODIFICATION_TIME, album.modifiedDate)
			put(Albums.HIDDEN, if (album.isHidden) 1 else 0)
		}
		return cache.update(Tables.ALBUMS, values2, "${Albums.PATH} = ?", arrayOf(oldPath))
	}

	/**
	 * @param album the album to add to cache
	 * @return new id (gets stored in album.id too)
	 */
	@JvmStatic
	fun addAlbum(album: Album): Long
	{
		albumInsert.bindString(1, album.path)
		albumInsert.bindString(2, album.name)
		albumInsert.bindLong(3, album.modifiedDate)
		albumInsert.bindLong(4, album.size)
		albumInsert.bindLong(5, album.count)
		albumInsert.bindLong(6, if (album.isHidden) 1 else 0)

		album.id = albumInsert.executeInsert()

		albumInsert.clearBindings()
		return album.id
	}

	private fun delete(table: String, path: String) = cache.delete(table, "${Base.PATH} like ?", arrayOf(path))
}