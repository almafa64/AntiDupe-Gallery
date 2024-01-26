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
						//	"id INTEGER," +
						"name TEXT," +
						"path TEXT PRIMARY KEY, " +
						"mtime INTEGER," +
						"mediaCount INTEGER," +
						"size INTEGER" +
						")"
		);
		database.execSQL(
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

	public static void updateMedia(ImageFile imageFile, String oldPath)
	{
		ContentValues values = new ContentValues();
		values.put("path", imageFile.getPath());
		values.put("album_path", imageFile.getFile().getParent());
		database.update(tableMedia, values, "path = ?", new String[]{ oldPath });
	}

	public static void addMedia(ImageFile imageFile, String albumPath)
	{
		try (Cursor cursor = database.rawQuery(
				"select count(*) from " + tableMedia + " where id = " + imageFile.getId(),
				null
		))
		{
			cursor.moveToFirst();
			if (cursor.getInt(0) > 0) return;
		}

		ContentValues values = new ContentValues();
		values.put("path", imageFile.getPath());
		values.put("name", imageFile.getName());
		values.put("ctime", imageFile.getCreationDate());
		values.put("mtime", imageFile.getModifiedDate());
		values.put("size", imageFile.getSize());
		values.put("mimeType", imageFile.getMime());
		values.put("album_path", albumPath);
		values.put("id", imageFile.getId());
		insert(tableMedia, values);
	}

	public static void deleteAlbum(Album album)
	{
		database.delete(tableMedia, "album_path = " + album.getPath(), null);
		delete(tableAlbums, album.getPath());
	}

	public static void updateAlbum(Album album, String oldPath)
	{
		ContentValues values = new ContentValues();
		values.put("album_path", album.getPath());
		database.update(tableMedia, values, "album_path = ?", new String[]{ oldPath });

		ContentValues values2 = new ContentValues();
		values2.put("path", album.getPath());
		database.update(tableAlbums, values2, "path = ?", new String[]{ oldPath });
	}

	public static void addAlbum(Album album)
	{
		try (Cursor cursor = database.rawQuery(
				"select count(*) from " + tableAlbums + " where path = ?",
				new String[]{ album.getPath() }
		))
		{
			cursor.moveToFirst();
			if (cursor.getInt(0) > 0) return;
		}

		ContentValues values = new ContentValues();
		values.put("path", album.getPath());
		values.put("name", album.getName());
		values.put("mtime", album.getModifiedDate());
		values.put("size", album.getSize());
		//values.put("id", album.getId());
		values.put("mediaCount ", album.getCount());
		insert(tableAlbums, values);
	}

	private static void insert(String table, ContentValues values) { database.insert(table, null, values); }

	private static void delete(String table, String path)
	{
		database.delete(table, "path like ?", new String[]{ path });
	}
}
