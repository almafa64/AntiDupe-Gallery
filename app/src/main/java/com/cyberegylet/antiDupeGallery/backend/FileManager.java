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
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.cyberegylet.antiDupeGallery.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileManager
{
	public static final int STORAGE_REQUEST_CODE = 1;
	public static final Uri EXTERNAL_URI = MediaStore.Files.getContentUri("external");
	public static final String IMAGES = MediaStore.MediaColumns.MIME_TYPE + " like 'image/%'";
	public static final String VIDEOS = MediaStore.MediaColumns.MIME_TYPE + " like 'video/%'";
	public static final String IMAGES_AND_VIDEOS = IMAGES + " or " + VIDEOS;
	public static final String PATH_FILTER_IMAGES_AND_VIDEOS = "(" + IMAGES_AND_VIDEOS + ") and " + MediaStore.MediaColumns.DATA + " like ?";

	public final Context context;
	public final Activity activity;
	private final ContentResolver contentResolver;

	private boolean hasFileAccess = false;

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
		contentResolver = activity.getContentResolver();

		boolean hasRead;
		boolean hasWrite = true;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			hasRead = (ContextCompat.checkSelfPermission(activity,
					Manifest.permission.READ_MEDIA_IMAGES
			) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
					Manifest.permission.READ_MEDIA_VIDEO
			) == PackageManager.PERMISSION_GRANTED);
		}
		else
		{
			hasRead = ContextCompat.checkSelfPermission(activity,
					android.Manifest.permission.READ_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED;
		}

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
		{
			hasWrite = ContextCompat.checkSelfPermission(activity,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED;
		}

		if (hasRead && hasWrite) hasFileAccess = true;
		else
		{
			List<String> permissions = new ArrayList<>();
			if (!hasWrite) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (!hasRead)
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				{
					permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
					permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
				}
				else permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			}
			ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), STORAGE_REQUEST_CODE);
		}
	}

	public boolean hasFileAccess() { return hasFileAccess; }

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

		public long getID() { return cursor.getLong(id_col); }

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

	public void allImageLoop(String sort, CursorLoopWrapper wrapper, String... queries)
	{
		cursorLoop(wrapper, sort, IMAGES, EXTERNAL_URI, queries);
	}

	public void allVideoLoop(String sort, CursorLoopWrapper wrapper, String... queries)
	{
		cursorLoop(wrapper, sort, VIDEOS, EXTERNAL_URI, queries);
	}

	public void allImageAndVideoInFolderLoop(String absoluteFolder, String sort, CursorLoopWrapper wrapper, String... queries)
	{
		cursorLoop(wrapper, sort, PATH_FILTER_IMAGES_AND_VIDEOS, new String[]{ absoluteFolder + "/%" }, EXTERNAL_URI, queries);
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

	public List<String> getAllImagePaths() { return getAllPaths(MediaStore.Images.Media.EXTERNAL_CONTENT_URI); }

	public List<String> getAllVideoPaths() { return getAllPaths(MediaStore.Video.Media.EXTERNAL_CONTENT_URI); }

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

	public static Uri stringToUri(String pathStr) { return Uri.parse("file://" + Uri.encode(pathStr, "/")); }

	public static String uriToString(Uri uri) { return uri.getPath(); }

	public static boolean isExternalStorageDocument(Uri uri) { return "com.android.externalstorage.documents".equals(uri.getAuthority()); }

	public static boolean isDownloadsDocument(Uri uri) { return "com.android.providers.downloads.documents".equals(uri.getAuthority()); }

	public static boolean isMediaDocument(Uri uri) { return "com.android.providers.media.documents".equals(uri.getAuthority()); }

	public static boolean isGooglePhotosUri(Uri uri) { return "com.google.android.apps.photos.content".equals(uri.getAuthority()); }

	public void thumbnailIntoImageView(ImageView imageView, String path)
	{
		RequestOptions options = new RequestOptions().priority(Priority.LOW).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
				.format(DecodeFormat.PREFER_ARGB_8888).set(Downsampler.ALLOW_HARDWARE_CONFIG, true).centerCrop();

		boolean playGIF = Config.getBooleanProperty(Config.Property.ANIMATE_GIF);
		if (!playGIF) options = options.dontAnimate().decode(Bitmap.class);
		else options = options.decode(Drawable.class);

		Glide.with(context).load(path).apply(options).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
	}

	public boolean moveFile(Path fromFile, Path toFolder)
	{
		try
		{
			Files.createDirectories(toFolder);
			Files.move(fromFile, toFolder.resolve(fromFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (AccessDeniedException e)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public boolean copyFile(Path fromFile, Path toFolder)
	{
		try
		{
			Files.createDirectories(toFolder);
			Files.copy(fromFile, toFolder.resolve(fromFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (AccessDeniedException e)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public boolean deleteFile(Path file)
	{
		try
		{
			Files.deleteIfExists(file);
			return true;
		}
		catch (AccessDeniedException e)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show();
			return false;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public boolean moveFolder(Path fromFolder, Path toFolder)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fromFolder))
		{
			for (Path path : stream)
			{
				if (Files.isDirectory(path)) continue;
				moveFile(path, toFolder);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public boolean copyFolder(Path fromFolder, Path toFolder)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(fromFolder))
		{
			for (Path path : stream)
			{
				if (Files.isDirectory(path)) continue;
				copyFile(path, toFolder);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public boolean deleteFolder(Path folder)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder))
		{
			for (Path path : stream)
			{
				if (Files.isDirectory(path)) continue;
				deleteFile(path);
			}
		}
		catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public static long getSize(File f)
	{
		if(!f.isDirectory()) return f.length();
		long size = 0;
		for(File file : Objects.requireNonNull(f.listFiles()))
		{
			size += file.length();
		}
		return size;
	}
}
