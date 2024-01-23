package com.cyberegylet.antiDupeGallery.backend;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

public class Cache
{
	public static final String DATABASE_NAME = "data.db";
	public static final String tableDigests = "digests";
	private static SQLiteDatabase database;

	private static void testActivity()
	{
		if (database == null) throw new RuntimeException("Init() was not called on Cache");
	}

	public static void Init(Activity activity)
	{
		if(database != null) return;
		database = SQLiteDatabase.openOrCreateDatabase(activity.getDatabasePath(DATABASE_NAME), null);
		database.execSQL(
				"CREATE TABLE IF NOT EXISTS albums (" +
						"id INTEGER PRIMARY KEY," +
						"name TEXT," +
						"path TEXT," +
						"ctime INTEGER," +
						"mtime INTEGER," +
						"mediaCount INTEGER," +
						"size INTEGER" +
						")"
		);
		database.execSQL(
				"CREATE TABLE IF NOT EXISTS media (" +
						"id INTEGER," +
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
}
