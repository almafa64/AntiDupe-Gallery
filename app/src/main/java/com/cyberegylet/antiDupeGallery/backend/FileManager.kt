package com.cyberegylet.antiDupeGallery.backend

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.cyberegylet.antiDupeGallery.R
import com.cyberegylet.antiDupeGallery.backend.Config.getBooleanProperty
import com.cyberegylet.antiDupeGallery.helpers.PermissionManager
import com.cyberegylet.antiDupeGallery.models.Album
import java.io.File
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileManager(@JvmField val activity: ComponentActivity)
{
	@JvmField
	val context: Context = activity.applicationContext
	private val contentResolver: ContentResolver = activity.contentResolver

	fun requestStoragePermissions(requestCallback: ((Array<String>?) -> Unit)?)
	{
		val hasWrite = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
		{
			ContextCompat.checkSelfPermission(
				activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			) == PackageManager.PERMISSION_GRANTED
		}
		else true

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

		if (hasRead && hasWrite) requestCallback?.invoke(null)
		else
		{
			val permissions: ArrayList<String> = ArrayList()
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

			val permissionManager = PermissionManager(activity)
			permissionManager.requestPermissions({ requestCallback?.invoke(it) }, *permissions.toTypedArray())
		}
	}

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
		vararg queries: String,
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
		vararg queries: String,
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
		vararg queries: String,
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
		absoluteFolder: String, sort: String?, wrapper: CursorLoopWrapper, vararg queries: String,
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

	fun getNoMediaFolders(): ArrayList<String>
	{
		val folders = ArrayList<String>()

		val wrapper = object : CursorLoopWrapper()
		{
			override fun run()
			{
				val noMediaFile = File(path)
				if (noMediaFile.exists() && noMediaFile.name == MediaStore.MEDIA_IGNORE_FILENAME)
				{
					folders.add(noMediaFile.parent!!)
				}
			}
		}
		cursorLoop(
			wrapper,
			"${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? AND ${MediaStore.MediaColumns.TITLE} LIKE ?",
			arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(), MediaStore.MEDIA_IGNORE_FILENAME),
			EXTERNAL_URI,
			MediaStore.MediaColumns.DATA
		)

		return folders
	}

	fun getFolders(): HashSet<String>
	{
		// https://github.com/SimpleMobileTools/Simple-Gallery/blob/master/app/src/main/kotlin/com/simplemobiletools/gallery/pro/helpers/MediaFetcher.kt#L84
		val folders = HashSet<String>()

		val args = ArrayList<String>()
		val sb = StringBuilder()

		for (a in Mimes.PHOTO_EXTENSIONS)
		{
			sb.append("${MediaStore.MediaColumns.DATA} LIKE ? OR ")
			args.add("%$a")
		}

		for (a in Mimes.VIDEO_EXTENSIONS)
		{
			sb.append("${MediaStore.MediaColumns.DATA} LIKE ? OR ")
			args.add("%$a")
		}

		val selection = sb.trimEnd().removeSuffix("OR").toString()

		val wrapper = object : CursorLoopWrapper()
		{
			override fun run()
			{
				folders.add(getParentPath(path))
			}
		}
		cursorLoop(wrapper, selection, args.toTypedArray(), EXTERNAL_URI, MediaStore.MediaColumns.DATA)

		return folders
	}

	fun getAlbums(): ArrayList<Album>
	{
		val albums = ArrayList<Album>()
		val wrapper = object : CursorLoopWrapper()
		{
			override fun run()
			{
				val f = File(path)
				if (!f.isDirectory) return
				albums.add(Album(f))
			}
		}
		cursorLoop(wrapper, EXTERNAL_URI, MediaStore.MediaColumns.DATA)
		return albums
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
			return Uri.parse("file://${cursor.getString(pathInd)}")
		}
	}

	fun getIDFromUri(path: Uri): Int
	{
		contentResolver.query(
			EXTERNAL_URI, arrayOf(MediaStore.MediaColumns._ID),
			"${MediaStore.MediaColumns.DATA}=?", arrayOf(path.path),
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

	fun getMimeType(uri: Uri): String = getMimeType(getIDFromUri(uri))

	fun thumbnailIntoImageView(imageView: ImageView, path: String)
	{
		var options = RequestOptions().priority(Priority.LOW)
			.diskCacheStrategy(DiskCacheStrategy.RESOURCE).format(DecodeFormat.PREFER_ARGB_8888)
			.set(Downsampler.ALLOW_HARDWARE_CONFIG, true).centerCrop()

		val playGIF = getBooleanProperty(Config.Property.ANIMATE_GIF)
		options = when
		{
			Mimes.isWebp(path) -> options.decode(WebpDrawable::class.java)
			!playGIF -> options.dontAnimate().decode(Bitmap::class.java)
			else -> options.decode(Drawable::class.java)
		}

		Glide.with(context).load(path).apply(options).transition(DrawableTransitionOptions.withCrossFade())
			.into(imageView)
	}

	/**
	 * @param type 0 -> move, 1 -> copy, else -> delete
	 */
	private fun updateMediaStore(fromFile: Path, toFile: Path?, type: Int = -1)
	{
		val list: Array<String> =
			when (type)
			{
				0 -> arrayOf(fromFile.toString(), toFile!!.toString())
				1 -> arrayOf(toFile!!.toString())
				else -> arrayOf(fromFile.toString())
			}
		MediaScannerConnection.scanFile(context, list, null) { path, uri ->
			Log.i(TAG, "scanned path: $path, uri: $uri")
		}
	}

	fun moveFile(fromFile: Path, toFolder: Path): Boolean
	{
		val toFile: Path = toFolder.resolve(fromFile.fileName)
		return try
		{
			Files.createDirectories(toFolder)
			Files.move(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING)
			updateMediaStore(fromFile, toFile, 0)
			true
		}
		catch (e: AccessDeniedException)
		{
			Log.e(TAG, "access denied: $toFile\n$e")
			Toast.makeText(context, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			Log.e(TAG, "io error: $toFile\n$e")
			false
		}
	}

	fun copyFile(fromFile: Path, toFolder: Path): Boolean
	{
		val toFile: Path = toFolder.resolve(fromFile.fileName)
		return try
		{
			Files.createDirectories(toFolder)
			Log.w("app", "dir good")
			Files.copy(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING)
			Log.w("app", "copy good")
			updateMediaStore(fromFile, toFile, 1)
			true
		}
		catch (e: AccessDeniedException)
		{
			Log.e(TAG, "access denied: $toFile\n$e")
			Toast.makeText(context, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			Log.e(TAG, "io error: $toFile\n$e")
			false
		}
	}

	fun deleteFile(file: Path): Boolean
	{
		return try
		{
			Files.deleteIfExists(file)
			updateMediaStore(file, null)
			true
		}
		catch (e: AccessDeniedException)
		{
			Log.e(TAG, "access denied: $file\n$e")
			Toast.makeText(context, R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
			false
		}
		catch (e: IOException)
		{
			Log.e(TAG, "io error: $file\n$e")
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
			Log.e(TAG, "io error: $toAlbum\n$e")
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
			Log.e(TAG, "io error: $toAlbum\n$e")
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
			Log.e(TAG, "io error: $album\n$e")
			false
		}
	}

	companion object
	{
		@JvmField
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
		fun isRawDownloadsDocument(uri: Uri): Boolean =
			uri.toString().contains("com.android.providers.downloads.documents/document/raw")

		@JvmStatic
		fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

		@JvmStatic
		fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority

		/**
		 * @return returns the size of the file. -1 if f is Directory.
		 */
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

		@JvmStatic
		fun getFileExtension(path: String) = path.substring(path.lastIndexOf(".") + 1)

		@JvmStatic
		fun getParentPath(path: String) = path.take(path.lastIndexOf('/'))

		const val TAG = "FileManager"
	}
}