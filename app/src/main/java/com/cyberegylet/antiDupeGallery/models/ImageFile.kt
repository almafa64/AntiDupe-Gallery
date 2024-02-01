package com.cyberegylet.antiDupeGallery.models

import com.cyberegylet.antiDupeGallery.backend.FileManager
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class ImageFile @JvmOverloads constructor(file: File, val mime: String = "*/*", id: Long = -1) : FileEntry(file, id)
{
	var creationDate: Long = 0
		private set

	override fun mySetFile(file: File)
	{
		super.mySetFile(file)
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