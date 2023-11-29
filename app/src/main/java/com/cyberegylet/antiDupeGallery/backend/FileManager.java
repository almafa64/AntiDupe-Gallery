package com.cyberegylet.antiDupeGallery.backend;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager
{
	public static final int STORAGE_REQUEST_CODE = 1;
	public static final Uri EXTERNAL_URI = MediaStore.Files.getContentUri("external");
	public static final String IMAGES_AND_VIDEOS = MediaStore.MediaColumns.MIME_TYPE + " like 'image/%' or " + MediaStore.MediaColumns.MIME_TYPE + " like 'video/%'";
	public static final String PATH_FILTER_IMAGES_AND_VIDEOS = "(" + IMAGES_AND_VIDEOS + ") and " + MediaStore.MediaColumns.DATA + " like ?";

	public final Context context;
	public final Activity activity;
	private final ContentResolver contentResolver;

	private boolean hasReadAccess = false;

	public static class Mimes
	{
		public static final String[] MIME_VIDEOS = new String[]{ "video/quicktime", "video/mpeg", "video/mp4", "video/3gpp", "video/webm",
				"video/avi" };
		public static final String[] MIME_IMAGES = new String[]{ "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
				"image/ico", "image/svg" };

		public enum Type
		{
			MIME_NONE,
			MIME_IMAGE,
			MIME_VIDEO,
		}

		public static boolean isImage(String mime_string) { return Arrays.asList(MIME_IMAGES).contains(mime_string); }

		public static boolean isVideo(String mime_string) { return Arrays.asList(MIME_VIDEOS).contains(mime_string); }

		public static boolean isMedia(String mime_string) { return isImage(mime_string) || isVideo(mime_string); }
	}

	public FileManager(Activity activity)
	{
		this.activity = activity;
		context = activity.getApplicationContext();
		contentResolver = context.getContentResolver();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			if (ContextCompat.checkSelfPermission(context,
					Manifest.permission.READ_MEDIA_IMAGES
			) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(context,
					Manifest.permission.READ_MEDIA_VIDEO
			) == PackageManager.PERMISSION_DENIED)
			{
				String[] permissions = new String[]{ Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO };
				ActivityCompat.requestPermissions(activity, permissions, STORAGE_REQUEST_CODE);
			}
			else
			{
				hasReadAccess = true;
			}
		}
		else
		{
			if (ContextCompat.checkSelfPermission(context,
					android.Manifest.permission.READ_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_DENIED)
			{
				String[] permissions = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };
				ActivityCompat.requestPermissions(activity, permissions, STORAGE_REQUEST_CODE);
			}
			else
			{
				hasReadAccess = true;
			}
		}
	}

	public boolean hasReadAccess() { return hasReadAccess; }

	public abstract static class CursorLoopWrapper
	{
		private int id_col, path_col, mime_col;
		private Cursor cursor;

		private void init(Cursor cursor)
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

	public void cursorLoop(
			CursorLoopWrapper wrapper, int cursorStart, String sort, String selection, String[] args, Uri uri, String... queries
	)
	{
		try (Cursor cursor = contentResolver.query(uri, queries, selection, args, sort))
		{
			assert cursor != null;
			wrapper.init(cursor);

			if (!cursor.moveToPosition(cursorStart))
			{
				return;
			}

			do
			{
				wrapper.run();
			} while (cursor.moveToNext());
		}
	}

	public void cursorLoop(CursorLoopWrapper wrapper, String sort, Uri uri, String... queries)
	{
		cursorLoop(wrapper, 0, sort, null, null, uri, queries);
	}

	public void cursorLoop(CursorLoopWrapper wrapper, String sort, String selection, Uri uri, String... queries)
	{
		cursorLoop(wrapper, 0, sort, selection, null, uri, queries);
	}

	public void cursorLoop(CursorLoopWrapper wrapper, String selection, String[] args, Uri uri, String... queries)
	{
		cursorLoop(wrapper, 0, null, selection, args, uri, queries);
	}

	public void cursorLoop(CursorLoopWrapper wrapper, String sort, String selection, String[] args, Uri uri, String... queries)
	{
		cursorLoop(wrapper, 0, sort, selection, args, uri, queries);
	}

	public void cursorLoop(CursorLoopWrapper wrapper, Uri uri, String... queries) { cursorLoop(wrapper, null, uri, queries); }

	public void allImageAndVideoLoop(String sort, CursorLoopWrapper wrapper, String... queries)
	{
		cursorLoop(wrapper, sort, IMAGES_AND_VIDEOS, EXTERNAL_URI, queries);
	}

	public void allImageAndVideoInFolderLoop(String absoluteFolder, String sort, CursorLoopWrapper wrapper, String... queries)
	{
		cursorLoop(wrapper, sort, PATH_FILTER_IMAGES_AND_VIDEOS, new String[]{ absoluteFolder + "/%" }, EXTERNAL_URI, queries);
	}

	private List<Integer> getAllIDs(Uri uri)
	{
		List<Integer> ids = new ArrayList<>();
		cursorLoop(new CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				ids.add(getID());
			}
		}, uri, MediaStore.MediaColumns._ID);
		return ids;
	}

	private List<String> getAllPaths(Uri uri)
	{
		List<String> paths = new ArrayList<>();
		cursorLoop(new CursorLoopWrapper()
		{
			@Override
			public void run()
			{
				paths.add(getPath());
			}
		}, uri, MediaStore.MediaColumns.DATA);
		return paths;
	}

	public List<Integer> getAllImageIDs() { return getAllIDs(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllVideoIDs() { return getAllIDs(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllAudioIDs() { return getAllIDs(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }

	public List<Integer> getAllFileIDs() { return getAllIDs(EXTERNAL_URI); }

	public List<String> getAllImagePaths() { return getAllPaths(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllVideoPaths() { return getAllPaths(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllAudioPaths() { return getAllPaths(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllFilePaths() { return getAllPaths(EXTERNAL_URI); }

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
		try (Cursor cursor = contentResolver.query(EXTERNAL_URI,
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

	public static Uri stringToUri(String pathStr) {
		Path path = Paths.get(pathStr);
		if (!path.isAbsolute()) {
			throw new RuntimeException("FileManager.stringToUri requires an absolute path");
		}
		final StringBuilder encodedPathBuilder = new StringBuilder();
		for (Path segment : path) {
			encodedPathBuilder.append("/").append(Uri.encode(segment.toString()));
		}
		return Uri.parse("file://" + encodedPathBuilder);
	}

	public static String uriToString(Uri uri) { return uri.getPath(); }

	public void thumbnailIntoImageView(ImageView imageView, Uri uri)
	{
		RequestOptions options = new RequestOptions().priority(Priority.LOW).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
				.format(DecodeFormat.PREFER_ARGB_8888).set(Downsampler.ALLOW_HARDWARE_CONFIG, true);

		boolean playGIF = ConfigManager.getBooleanConfig(ConfigManager.Config.ANIMATE_GIF);
		if (!playGIF) options = options.dontAnimate().decode(Bitmap.class);
		else options = options.decode(Drawable.class);

		Glide.with(context).load(uri).apply(options).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
	}
}
