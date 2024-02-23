package com.cyberegylet.antiDupeGallery.models

import com.cyberegylet.antiDupeGallery.backend.FileManager
import com.cyberegylet.antiDupeGallery.backend.Mimes
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class ImageFile @JvmOverloads constructor(file: File, val type: Mimes.Type, id: Long = -1) : FileEntry(file, id)
{
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