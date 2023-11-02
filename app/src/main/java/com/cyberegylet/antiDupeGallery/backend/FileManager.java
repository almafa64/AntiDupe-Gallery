package com.cyberegylet.antiDupeGallery.backend;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager
{
	public final Context context;
	private final ContentResolver contentResolver;

	public static final int STORAGE_REQUEST_CODE = 1;
	public static final Uri externalUri = MediaStore.Files.getContentUri("external");

	private boolean has_read_access = false;

	public static class Mimes
	{
		public static final String[] MIME_ALL = new String[]{ "video/quicktime", "video/mpeg", "video/mp4", "video/3gpp", "video/webm",
				"video/avi", "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/ico", "image/svg", "audio/3gpp",
				"audio/midi", "audio/mpeg", "audio/x-wav" };
		public static final String[] MIME_VIDEOS = new String[]{ "video/quicktime", "video/mpeg", "video/mp4", "video/3gpp", "video/webm",
				"video/avi" };
		public static final String[] MIME_IMAGES = new String[]{ "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
				"image/ico", "image/svg" };
		public static final String[] MIME_AUDIOS = new String[]{ "audio/3gpp", "audio/midi", "audio/mpeg", "audio/x-wav" };

		public enum Type
		{
			MIME_NONE,
			MIME_IMAGE,
			MIME_VIDEO,
			MIME_AUDIO,
		}

		public static boolean isImage(String mime_string) { return Arrays.asList(MIME_IMAGES).contains(mime_string); }

		public static boolean isAudio(String mime_string) { return Arrays.asList(MIME_AUDIOS).contains(mime_string); }

		public static boolean isVideo(String mime_string) { return Arrays.asList(MIME_VIDEOS).contains(mime_string); }

		public static boolean isMedia(String mime_string) { return Arrays.asList(MIME_ALL).contains(mime_string); }
	}

	public FileManager(Activity activity)
	{
		context = activity.getApplicationContext();
		contentResolver = context.getContentResolver();

		if (ContextCompat.checkSelfPermission(context,
				android.Manifest.permission.READ_EXTERNAL_STORAGE
		) == PackageManager.PERMISSION_DENIED)
		{
			String[] request;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				request = new String[] {
						Manifest.permission.READ_MEDIA_IMAGES,
						Manifest.permission.READ_MEDIA_VIDEO,
						Manifest.permission.READ_MEDIA_AUDIO
				};
			}
			else
			{
				request = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
			}

			ActivityCompat.requestPermissions(activity, request, STORAGE_REQUEST_CODE);
		}
		else has_read_access = true;
	}

	public boolean hasReadAccess() { return has_read_access; }

	public abstract static class CursorLoopWrapper
	{
		private int id_col, path_col, mime_col;
		private Cursor cursor;

		private void innit(Cursor cursor)
		{
			this.cursor = cursor;
			id_col = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
			path_col = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
			mime_col = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
		}

		public int getID() { return cursor.getInt(id_col); }

		public String getPath() { return cursor.getString(path_col); }

		public String getMime() { return cursor.getString(mime_col); }

		public abstract void run();

		public void stop() { cursor.moveToLast(); }
	}

	public void CursorLoop(CursorLoopWrapper wrapper, int cursorStart, String sort, String selection, Uri uri, String... queries)
	{
		try (Cursor cursor = contentResolver.query(uri, queries, selection, null, sort))
		{
			assert cursor != null;
			wrapper.innit(cursor);
			if (!cursor.moveToPosition(cursorStart)) return;
			do
			{
				wrapper.run();
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

	public void allImageAndVideoLoop(String sort, CursorLoopWrapper wrapper, String... queries)
	{
		String selection = MediaStore.Files.FileColumns.MIME_TYPE + " like 'image/%' or " + MediaStore.Files.FileColumns.MIME_TYPE + " like 'video/%'";
		CursorLoop(wrapper, sort, selection, externalUri, queries);
	}

	public void allImageAndVideoInFolderLoop(String absoluteFolder, String sort, CursorLoopWrapper wrapper, String... queries)
	{
		String selection = "(" + MediaStore.Files.FileColumns.MIME_TYPE + " like 'image/%' or " + MediaStore.Files.FileColumns.MIME_TYPE + " like 'video/%') and " + MediaStore.MediaColumns.DATA + " like '" + absoluteFolder + "/%'";
		CursorLoop(wrapper, sort, selection, externalUri, queries);
	}

	private List<Integer> getAllID(Uri uri)
	{
		List<Integer> ids = new ArrayList<>();
		CursorLoop(new CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				ids.add(getID());
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
			public void run()
			{
				paths.add(getPath());
			}
		}, uri, MediaStore.MediaColumns.DATA);
		return paths;
	}

	public List<Integer> getAllImageID() { return getAllID(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllVideoID() { return getAllID(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllAudioID() { return getAllID(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllFileID() { return getAllID(externalUri); }

	public List<String> getAllImagePath() { return getAllPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllVideoPath() { return getAllPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllAudioPath() { return getAllPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllFilePath() { return getAllPath(externalUri); }

	public Uri getUriFromID(int id)
	{
		try (Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external", id),
				new String[]{ MediaStore.MediaColumns.DATA },
				null,
				null,
				null
		))
		{
			assert cursor != null;
			int path_ind = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
			cursor.moveToFirst();
			return Uri.parse("file://" + cursor.getString(path_ind));
		}
	}

	public int getIDFromUri(Uri path)
	{
		try (Cursor cursor = contentResolver.query(externalUri,
				new String[]{ MediaStore.MediaColumns._ID },
				MediaStore.MediaColumns.DATA + "=?",
				new String[]{ path.getPath() },
				null
		))
		{
			assert cursor != null;
			int id_ind = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
			cursor.moveToFirst();
			return cursor.getInt(id_ind);
		}
	}

	public String getMimeType(int id)
	{
		try (Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external", id),
				new String[]{ MediaStore.MediaColumns.MIME_TYPE },
				null,
				null,
				null
		))
		{
			assert cursor != null;
			int mime_ind = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE);
			cursor.moveToFirst();
			return cursor.getString(mime_ind);
		}
	}

	public String getMimeType(Uri uri) { return getMimeType(getIDFromUri(uri)); }

	public Uri stringToUri(String path) { return Uri.parse("file://" + path); }

	/*public Bitmap getThumbnail(String path, int id, boolean isImage)
	{
		Bitmap bitmap;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
		{
			try
			{
				//bitmap = context.getContentResolver().loadThumbnail(data.uri, new Size(255, 255), null);
				bitmap = contentResolver.loadThumbnail(ContentUris.withAppendedId(externalUri, id),
						new Size(384, 512), null);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			if (isImage)
			{
				bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
				if (bitmap == null)
				{
					bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 384, 512);
				}
			}
			else
			{
				bitmap = MediaStore.Video.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, null);
				if (bitmap == null)
				{
					assert path != null;
					bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
					if (bitmap == null)
					{
						bitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), 384, 512);
					}
				}
			}
		}
		return bitmap;
	}*/

	public void thumbnailIntoImageView(ImageView imageView, Uri uri)
	{
		Glide.with(context).load(uri).set(Downsampler.ALLOW_HARDWARE_CONFIG, true).transition(DrawableTransitionOptions.withCrossFade())
				.into(imageView);
	}
}
