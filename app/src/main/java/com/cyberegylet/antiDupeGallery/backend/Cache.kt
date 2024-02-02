package com.cyberegylet.antiDupeGallery.backend

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cyberegylet.antiDupeGallery.models.Album
import com.cyberegylet.antiDupeGallery.models.ImageFile

object Cache
{
	const val DATABASE_NAME = "data.db"
	const val tableDigests = "digests"
	const val tableAlbums = "albums"
	const val tableMedia = "media"
	private var _database: SQLiteDatabase? = null

	@JvmStatic
	val cache
		get() = _database!!

	@JvmStatic
	fun init(activity: Activity)
	{
		if (_database != null) return
		_database = SQLiteDatabase.openOrCreateDatabase(activity.getDatabasePath(DATABASE_NAME), null)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS albums (" +  //	"id INTEGER," +
					"name TEXT," +
					"path TEXT PRIMARY KEY, " +
					"mtime INTEGER," +
					"mediaCount INTEGER," +
					"size INTEGER" +
					")"
		)
		cache.execSQL(
			"CREATE TABLE IF NOT EXISTS media (" +
					"id INTEGER PRIMARY KEY," +
					"album_path TEXT," +
					"name TEXT," +
					"path TEXT," +
					"ctime INTEGER," +
					"mtime INTEGER," +
					"size INTEGER," +
					"mimeType TEXT," +
					"chash BLOB," +
					"phash BLOB," +
					"FOREIGN KEY(album_path) REFERENCES albums(path)" +
					")"
		)
		cache.execSQL("CREATE TABLE IF NOT EXISTS digests (id INTEGER, path TEXT, digest BLOB)")
	}

	@JvmStatic
	fun getDbPath(name: String?, activity: Activity): String = activity.getDatabasePath(name).absolutePath

	@JvmStatic
	fun deleteMedia(path: String) = delete(tableMedia, path)

	@JvmStatic
	fun updateMedia(imageFile: ImageFile, oldPath: String)
	{
		val values = ContentValues()
		values.put("path", imageFile.path)
		values.put("album_path", imageFile.file!!.parent)
		cache.update(tableMedia, values, "path = ?", arrayOf(oldPath))
	}

	@JvmStatic
	fun addMedia(imageFile: ImageFile, albumPath: String?)
	{
		cache.rawQuery(
			"select count(*) from $tableMedia where id = ${imageFile.id}",
			null
		).use { cursor ->
			cursor.moveToFirst()
			if (cursor.getInt(0) > 0) return
		}
		val values = ContentValues()
		values.put("path", imageFile.path)
		values.put("name", imageFile.name)
		values.put("ctime", imageFile.creationDate)
		values.put("mtime", imageFile.modifiedDate)
		values.put("size", imageFile.size)
		values.put("mimeType", imageFile.mime)
		values.put("album_path", albumPath)
		values.put("id", imageFile.id)
		insert(tableMedia, values)
	}

	@JvmStatic
	fun deleteAlbum(album: Album)
	{
		cache.delete(tableMedia, "album_path = " + album.path, null)
		delete(tableAlbums, album.path)
	}

	@JvmStatic
	fun updateAlbum(album: Album, oldPath: String)
	{
		val values = ContentValues()
		values.put("album_path", album.path)
		cache.update(tableMedia, values, "album_path = ?", arrayOf(oldPath))
		val values2 = ContentValues()
		values2.put("path", album.path)
		cache.update(tableAlbums, values2, "path = ?", arrayOf(oldPath))
	}

	@JvmStatic
	fun addAlbum(album: Album)
	{
		cache.rawQuery(
			"select count(*) from $tableAlbums where path = ?", arrayOf(album.path)
		).use { cursor ->
			cursor.moveToFirst()
			if (cursor.getInt(0) > 0) return
		}
		val values = ContentValues()
		values.put("path", album.path)
		values.put("name", album.name)
		values.put("mtime", album.modifiedDate)
		values.put("size", album.size)
		//values.put("id", album.getId());
		values.put("mediaCount ", album.count)
		insert(tableAlbums, values)
	}

	private fun insert(table: String, values: ContentValues) = cache.insert(table, null, values)

	private fun delete(table: String, path: String) = cache.delete(table, "path like ?", arrayOf(path))
}