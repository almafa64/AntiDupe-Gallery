package com.cyberegylet.antiDupeGallery.backend;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cyberegylet.antiDupeGallery.models.Album;
import com.cyberegylet.antiDupeGallery.models.ImageFile;

public class Cache
{
	public static final String DATABASE_NAME = "data.db";
	public static final String tableDigests = "digests";
	public static final String tableAlbums = "albums";
	public static final String tableMedia = "media";
	private static SQLiteDatabase database;

	private static void testActivity()
	{
		if (database == null) throw new RuntimeException("Init() was not called on Cache");
	}

	public static void Init(Activity activity)
	{
		if (database != null) return;
		database = SQLiteDatabase.openOrCreateDatabase(activity.getDatabasePath(DATABASE_NAME), null);
		database.execSQL(
				"CREATE TABLE IF NOT EXISTS albums (" +
						"id INTEGER PRIMARY KEY," +
						"name TEXT," +
						"path TEXT," +
						"mtime INTEGER," +
						"mediaCount INTEGER," +
						"size INTEGER" +
						")"
		);
		database.execSQL(
				"CREATE TABLE IF NOT EXISTS media (" +
						"id INTEGER PRIMARY KEY," +
						"album_id INTEGER," +
						"name TEXT," +
						"path TEXT," +
						"ctime INTEGER," +
						"mtime INTEGER," +
						"size INTEGER," +
						"mimeType TEXT," +
						"chash BLOB," +
						"phash BLOB," +
						"FOREIGN KEY(album_id) REFERENCES albums(id)" +
						")"
		);
		database.execSQL("CREATE TABLE IF NOT EXISTS digests (id INTEGER, path TEXT, digest BLOB)");
	}

	public static String getDbPath(String name, Activity activity)
	{
		testActivity();
		return activity.getDatabasePath(name).getAbsolutePath();
	}

	public static SQLiteDatabase openCache()
	{
		testActivity();
		return database;
	}

	public static void deleteMedia(String path) { delete(tableMedia, path); }

	public static void addMedia(ImageFile imageFile, long albumId)
	{
		try(Cursor cursor = database.rawQuery("select count(*) from "+tableMedia+" where id = " + imageFile.getId(), null))
		{
			cursor.moveToFirst();
			if(cursor.getInt(0) > 0) return;
		}

		ContentValues values = new ContentValues();
		values.put("path", imageFile.getPath());
		values.put("name", imageFile.getName());
		values.put("ctime", imageFile.getCreationDate());
		values.put("mtime", imageFile.getModifiedDate());
		values.put("size", imageFile.getSize());
		values.put("mimeType", imageFile.getMime());
		values.put("album_id", albumId);
		values.put("id", imageFile.getId());
		insert(tableMedia, values);
	}

	public static void deleteAlbum(String path) { delete(tableAlbums, path); }

	public static void addAlbum(Album album)
	{
		try(Cursor cursor = database.rawQuery("select count(*) from "+tableAlbums+" where id = " + album.getId(), null))
		{
			cursor.moveToFirst();
			if(cursor.getInt(0) > 0) return;
		}

		ContentValues values = new ContentValues();
		values.put("path", album.getPath());
		values.put("name", album.getName());
		values.put("mtime", album.getModifiedDate());
		values.put("size", album.getSize());
		values.put("id", album.getId());
		values.put("mediaCount ", album.getCount());
		insert(tableAlbums, values);
	}

	private static void insert(String table, ContentValues values) { database.insert(table, null, values); }

	private static void delete(String table, String path)
	{
		database.delete(table, "path like ?", new String[]{ path });
	}
}
