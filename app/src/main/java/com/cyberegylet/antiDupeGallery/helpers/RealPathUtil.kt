package com.cyberegylet.antiDupeGallery.helpers

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

object RealPathUtil
{
	@JvmStatic
	fun getRealPath(context: Context, fileUri: Uri): String? = getRealPathFromUriApi19(context, fileUri)

	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 * @param context The context.
	 * @param uri     The Uri to query.
	 */
	@JvmStatic
	fun getRealPathFromUriApi19(context: Context, uri: Uri): String?
	{
		// DocumentProvider
		if (DocumentsContract.isDocumentUri(context, uri))
		{
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri))
			{
				val docId = DocumentsContract.getDocumentId(uri)
				val split = docId.split(":").toTypedArray()
				val type = split[0]
				if ("primary".equals(type, ignoreCase = true))
				{
					return "${Environment.getExternalStorageDirectory()}/${split[1]}"
				}

				// TODO handle non-primary volumes
			}
			else if (isDownloadsDocument(uri))
			{
				val id = DocumentsContract.getDocumentId(uri)
				val contentUri =
					ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
				return getDataColumn(context, contentUri, null, null)
			}
			else if (isMediaDocument(uri))
			{
				val docId = DocumentsContract.getDocumentId(uri)
				val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
				val type = split[0]
				var contentUri: Uri? = null
				when (type)
				{
					"image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
					"video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
					"audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
				}
				val selection = "_id=?"
				val selectionArgs = arrayOf(split[1])
				return getDataColumn(context, contentUri!!, selection, selectionArgs)
			}
		}
		else if ("content".equals(uri.scheme, ignoreCase = true))
		{
			// Return the remote address
			return when
			{
				isGooglePhotosUri(uri) -> uri.lastPathSegment
				else -> getDataColumn(context, uri, null, null)
			}
		}
		else if ("file".equals(uri.scheme, ignoreCase = true)) return uri.path
		return null
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	@JvmStatic
	fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String?
	{
		val data = MediaStore.MediaColumns.DATA
		val projection = arrayOf(data)
		context.contentResolver.query(uri, projection, selection, selectionArgs, null).use { cursor ->
			if (cursor != null && cursor.moveToFirst())
			{
				val index = cursor.getColumnIndexOrThrow(data)
				return cursor.getString(index)
			}
		}
		return null
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	@JvmStatic
	fun isExternalStorageDocument(uri: Uri): Boolean = "com.android.externalstorage.documents" == uri.authority

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	@JvmStatic
	fun isDownloadsDocument(uri: Uri): Boolean = "com.android.providers.downloads.documents" == uri.authority

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	@JvmStatic
	fun isMediaDocument(uri: Uri): Boolean = "com.android.providers.media.documents" == uri.authority

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	@JvmStatic
	fun isGooglePhotosUri(uri: Uri): Boolean = "com.google.android.apps.photos.content" == uri.authority
}