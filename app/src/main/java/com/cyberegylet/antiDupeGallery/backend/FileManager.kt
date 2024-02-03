package com.cyberegylet.antiDupeGallery.backend

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.backend.Config.getBooleanProperty
import java.io.File
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileManager(@JvmField val activity: Activity)
{
	@JvmField
	val context: Context = activity.applicationContext
	private val contentResolver: ContentResolver = activity.contentResolver
	private var hasFileAccess = false

	object Mimes
	{
		@JvmField
		val MIME_VIDEOS = arrayOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp", "video/avi", "video/quicktime")

		@JvmField
		val MIME_IMAGES =
			arrayOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg", "image/ico")

		@JvmStatic
		fun isImage(mimeString: String): Boolean = listOf(*MIME_IMAGES).contains(mimeString)

		@JvmStatic
		fun isVideo(mimeString: String): Boolean = listOf(*MIME_VIDEOS).contains(mimeString)

		@JvmStatic
		fun isMedia(mimeString: String): Boolean = isImage(mimeString) || isVideo(mimeString)

		enum class Type
		{
			MIME_NONE,
			MIME_IMAGE,
			MIME_VIDEO
		}
	}

	init
	{
		val hasWrite = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
		{
			ContextCompat.checkSelfPermission(
				activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED
		}
		else
		{
			true
		}

		val hasRead: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			ContextCompat.checkSelfPermission(
				activity,
				Manifest.permission.READ_MEDIA_IMAGES
			) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
				activity,
				Manifest.permission.READ_MEDIA_VIDEO
			) == PackageManager.PERMISSION_GRANTED
		}
		else
		{
			ContextCompat.checkSelfPermission(
				activity,
				Manifest.permission.READ_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED
		}

		if (hasRead && hasWrite) hasFileAccess = true
		else
		{
			val permissions: MutableList<String> = ArrayList()
			if (!hasWrite) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
			if (!hasRead)
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
				{
					permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
					permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
				}
				else permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
			}
			ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), STORAGE_REQUEST_CODE)
		}
	}

	fun hasFileAccess(): Boolean = hasFileAccess

	abstract class CursorLoopWrapper
	{
		private var idCol = 0
		private var pathCol = 0
		private var mimeCol = 0
		private lateinit var cursor: Cursor
		fun init(cursor: Cursor)
		{
			this.cursor = cursor
			idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
			pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
			mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
		}

		val id: Long
			get() = cursor.getLong(idCol)
		val path: String
			get() = cursor.getString(pathCol)
		val mime: String
			get() = cursor.getString(mimeCol)

		abstract fun run()
		fun stop() = cursor.moveToLast()
	}

	fun cursorLoop(
		wrapper: CursorLoopWrapper,
		cursorStart: Int,
		sort: String?,
		selection: String?,
		args: Array<String>?,
		uri: Uri,
		vararg queries: String
	)
	{
		contentResolver.query(uri, queries, selection, args, sort).use { cursor ->
			wrapper.init(cursor!!)
			if (!cursor.moveToPosition(cursorStart)) return
			do
			{
				wrapper.run()
			} while (cursor.moveToNext())
		}
	}

	fun cursorLoop(wrapper: CursorLoopWrapper, sort: String?, uri: Uri, vararg queries: String)
	{
		cursorLoop(wrapper, 0, sort, null, null, uri, *queries)
	}

	fun cursorLoop(wrapper: CursorLoopWrapper, sort: String?, selection: String?, uri: Uri, vararg queries: String)
	{
		cursorLoop(wrapper, 0, sort, selection, null, uri, *queries)
	}

	fun cursorLoop(
		wrapper: CursorLoopWrapper,
		selection: String?,
		args: Array<String>?,
		uri: Uri,
		vararg queries: String
	)
	{
		cursorLoop(wrapper, 0, null, selection, args, uri, *queries)
	}

	fun cursorLoop(
		wrapper: CursorLoopWrapper,
		sort: String?,
		selection: String?,
		args: Array<String>?,
		uri: Uri,
		vararg queries: String
	)
	{
		cursorLoop(wrapper, 0, sort, selection, args, uri, *queries)
	}

	fun cursorLoop(wrapper: CursorLoopWrapper, uri: Uri, vararg queries: String)
	{
		cursorLoop(wrapper, null, uri, *queries)
	}

	fun allImageAndVideoLoop(sort: String?, wrapper: CursorLoopWrapper, vararg queries: String)
	{
		cursorLoop(wrapper, sort, IMAGES_AND_VIDEOS, EXTERNAL_URI, *queries)
	}

	fun allImageAndVideoInFolderLoop(
		absoluteFolder: String, sort: String?, wrapper: CursorLoopWrapper, vararg queries: String
	)
	{
		cursorLoop(
			wrapper,
			sort,
			PATH_FILTER_IMAGES_AND_VIDEOS, arrayOf("$absoluteFolder/%"),
			EXTERNAL_URI,
			*queries
		)
	}

	fun getUriFromID(id: Int): Uri
	{
		contentResolver.query(
			MediaStore.Files.getContentUri("external", id.toLong()), arrayOf(MediaStore.MediaColumns.DATA),
			null,
			null,
			null
		).use { cursor ->
			val pathInd = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
			cursor.moveToFirst()
			return Uri.parse("file://" + cursor.getString(pathInd))
		}
	}

	fun getIDFromUri(path: Uri): Int
	{
		contentResolver.query(
			EXTERNAL_URI, arrayOf(MediaStore.MediaColumns._ID),
			MediaStore.MediaColumns.DATA + "=?", arrayOf(path.path),
			null
		).use { cursor ->
			val idInd = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
			cursor.moveToFirst()
			return cursor.getInt(idInd)
		}
	}

	fun getMimeType(id: Int): String
	{
		contentResolver.query(
			MediaStore.Files.getContentUri("external", id.toLong()), arrayOf(MediaStore.MediaColumns.MIME_TYPE),
			null,
			null,
			null
		).use { cursor ->
			val mimeInd = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
			cursor.moveToFirst()
			return cursor.getString(mimeInd)
		}
	}

	fun getMimeType(uri: Uri): String
	{
		return getMimeType(getIDFromUri(uri))
	}

	fun thumbnailIntoImageView(imageView: ImageView?, path: String?)
	{
		var options = RequestOptions().priority(Priority.LOW)
			.diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_ARGB_8888)
			.set(Downsampler.ALLOW_HARDWARE_CONFIG, true).centerCrop()

		val playGIF = getBooleanProperty(Config.Property.ANIMATE_GIF)
		options = if (!playGIF) options.dontAnimate().decode(Bitmap::class.java)
		else options.decode(Drawable::class.java)

		Glide.with(context).load(path).apply(options).transition(DrawableTransitionOptions.withCrossFade())
			.into(imageView!!)
	}

	fun moveFile(fromFile: Path, toFolder: Path): Boolean
	{
		return try
		{
			Files.createDirectories(toFolder)
			Files.move(fromFile, toFolder.resolve(fromFile.fileName), StandardCopyOption.REPLACE_EXISTING)
			true
		}
		catch (e: AccessDeniedException)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			false
		}
	}

	fun copyFile(fromFile: Path, toFolder: Path): Boolean
	{
		return try
		{
			Files.createDirectories(toFolder)
			Files.copy(fromFile, toFolder.resolve(fromFile.fileName), StandardCopyOption.REPLACE_EXISTING)
			true
		}
		catch (e: AccessDeniedException)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			false
		}
	}

	fun deleteFile(file: Path?): Boolean
	{
		return try
		{
			Files.deleteIfExists(file)
			true
		}
		catch (e: AccessDeniedException)
		{
			Toast.makeText(context, R.string.no_storage_permission, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			false
		}
	}

	fun moveAlbum(fromAlbum: Path?, toAlbum: Path): Boolean
	{
		return try
		{
			Files.newDirectoryStream(fromAlbum).use { stream ->
				for (path in stream)
				{
					if (Files.isDirectory(path)) continue
					moveFile(path, toAlbum)
				}
			}
			true
		}
		catch (e: IOException)
		{
			false
		}
	}

	fun copyAlbum(fromAlbum: Path?, toAlbum: Path): Boolean
	{
		return try
		{
			Files.newDirectoryStream(fromAlbum).use { stream ->
				for (path in stream)
				{
					if (Files.isDirectory(path)) continue
					copyFile(path, toAlbum)
				}
			}
			true
		}
		catch (e: IOException)
		{
			false
		}
	}

	fun deleteAlbum(album: Path?): Boolean
	{
		return try
		{
			Files.newDirectoryStream(album).use { stream ->
				for (path in stream)
				{
					if (Files.isDirectory(path)) continue
					deleteFile(path)
				}
			}
			true
		}
		catch (e: IOException)
		{
			false
		}
	}

	companion object
	{
		const val STORAGE_REQUEST_CODE = 1
		val EXTERNAL_URI: Uri = MediaStore.Files.getContentUri("external")
		const val IMAGES = MediaStore.MediaColumns.MIME_TYPE + " like 'image/%'"
		const val VIDEOS = MediaStore.MediaColumns.MIME_TYPE + " like 'video/%'"
		const val IMAGES_AND_VIDEOS = "$IMAGES or $VIDEOS"
		const val PATH_FILTER_IMAGES_AND_VIDEOS = "($IMAGES_AND_VIDEOS) and ${MediaStore.MediaColumns.DATA} like ?"

		@JvmStatic
		fun stringToUri(pathStr: String?): Uri = Uri.parse("file://" + Uri.encode(pathStr, "/"))

		@JvmStatic
		fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

		@JvmStatic
		fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

		@JvmStatic
		fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

		@JvmStatic
		fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority

		@JvmStatic
		fun getSize(f: File): Long
		{
			return if (!f.isDirectory) f.length() else -1
			/*long size = 0;
		for (File file : Objects.requireNonNull(f.listFiles()))
		{
			size += file.length();
		}
		return size;*/
		}
	}
}