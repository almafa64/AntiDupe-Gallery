package com.cyberegylet.antiDupeGallery;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManager
{
	private final Context context;
	private final Activity activity;
	private final ContentResolver contentResolver;

	public static final int STORAGE_REQUEST_CODE = 1;

	private boolean has_read_access = false;

	public FileManager(Activity activity)
	{
		context = activity.getApplicationContext();
		contentResolver = context.getContentResolver();
		this.activity = activity;

		if (ContextCompat.checkSelfPermission(context,
				android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
		{
			ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, STORAGE_REQUEST_CODE);
		}
		else has_read_access = true;
	}

	public boolean hasReadAccess() { return has_read_access; }

	public abstract static class CursorLoopWrapper
	{
		private int id_col, path_col;

		public int getIdCol() { return id_col; }

		public int getPathCol() { return path_col; }

		public abstract void run(Cursor cursor);
	}

	public void CursorLoop(CursorLoopWrapper wrapper, int cursorStart, String sort, String selection, Uri uri, String... queries)
	{
		try (Cursor cursor = contentResolver.query(uri, queries, selection, null, sort))
		{
			assert cursor != null;
			wrapper.id_col = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
			wrapper.path_col = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			if (!cursor.moveToPosition(cursorStart)) return;
			do
			{
				wrapper.run(cursor);
			} while (cursor.moveToNext());
		}
	}

	public void CursorLoop(CursorLoopWrapper wrapper, String sort, Uri uri, String... queries)
	{
		CursorLoop(wrapper, 0, sort, null, uri, queries);
	}

	public void CursorLoop(CursorLoopWrapper wrapper, String sort, String selection, Uri uri, String... queries)
	{
		CursorLoop(wrapper, 0, sort, selection, uri, queries);
	}

	public void CursorLoop(CursorLoopWrapper wrapper, Uri uri, String... queries) { CursorLoop(wrapper, null, uri, queries); }

	private List<Integer> getAllID(Uri uri)
	{
		List<Integer> ids = new ArrayList<>();
		CursorLoop(new CursorLoopWrapper()
		{
			@Override
			public void run(Cursor cursor)
			{
				ids.add(cursor.getInt(getIdCol()));
			}
		}, uri, MediaStore.MediaColumns._ID);
		return ids;
	}

	private List<String> getAllPath(Uri uri)
	{
		List<String> paths = new ArrayList<>();
		CursorLoop(new CursorLoopWrapper()
		{
			@Override
			public void run(Cursor cursor)
			{
				paths.add(cursor.getString(getPathCol()));
			}
		}, uri, MediaStore.MediaColumns.DATA);
		return paths;
	}

	public List<Integer> getAllImageID() { return getAllID(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllVideoID() { return getAllID(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllAudioID() { return getAllID(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }
	public List<Integer> getAllFileID() { return getAllID(MediaStore.Files.getContentUri("external")); }

	public List<String> getAllImagePath() { return getAllPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllVideoPath() { return getAllPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllAudioPath() { return getAllPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }
	public List<String> getAllFilePath() { return getAllPath(MediaStore.Files.getContentUri("external")); }

	public Uri getUriFromID(int id)
	{
		try (Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external", id),
				new String[]{ MediaStore.MediaColumns.DATA }, null, null, null))
		{
			assert cursor != null;
			int path_ind = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			cursor.moveToFirst();
			return Uri.fromFile(new File(cursor.getString(path_ind)));
		}
	}

	public int getIDFromUri(Uri path)
	{
		try (Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"), new String[]{ MediaStore.MediaColumns._ID },
				MediaStore.MediaColumns.DATA + "=?", new String[]{ path.getPath() }, null))
		{
			assert cursor != null;
			int id_ind = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
			cursor.moveToFirst();
			return cursor.getInt(id_ind);
		}
	}
}
