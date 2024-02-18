package com.cyberegylet.antiDupeGallery.models

import com.cyberegylet.antiDupeGallery.backend.FileManager
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class ImageFile @JvmOverloads constructor(file: File, val mime: String = "*/*", id: Long = -1) : FileEntry(file, id)
{
	val mimeEnum: FileManager.Mimes.Type = when
	{
		mime[0] == 'i' -> FileManager.Mimes.Type.IMAGE
		mime[0] == 'v' -> FileManager.Mimes.Type.VIDEO
		else -> FileManager.Mimes.Type.UNKNOWN
	}

	override fun mySetFile()
	{
		super.mySetFile()
		try
		{
			val attr = Files.readAttributes(Paths.get(file.path), BasicFileAttributes::class.java)
			size = FileManager.getSize(file)
			modifiedDate = attr.lastModifiedTime().toMillis()
			creationDate = attr.creationTime().toMillis()
		}
		catch (e: IOException)
		{
			throw RuntimeException(e)
		}
	}
}