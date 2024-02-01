package com.cyberegylet.antiDupeGallery.models

import com.cyberegylet.antiDupeGallery.backend.FileManager
import java.io.File

class Album : FileEntry
{
	var indexImage: ImageFile? = null
		private set
	var count: Long = 0
		private set

	constructor(file: File) : super(file, countId)
	{
		countId++
	}

	constructor(folder: Album, copyImages: Boolean)
	{
		name = folder.name
		file = folder.file
		modifiedDate = folder.modifiedDate
		id = folder.id
		if (copyImages)
		{
			indexImage = folder.indexImage
			size = folder.size
			count = folder.count
		}
		else
		{
			size = 0
			count = 0
		}
	}

	constructor(path: String) : this(File(path))

	fun addImage(imageFile: ImageFile)
	{
		if (count == 0L) indexImage = imageFile
		size += FileManager.getSize(imageFile.file)
		count++
	}

	override fun mySetFile(file: File)
	{
		super.mySetFile(file)
		modifiedDate = file.lastModified()
	}

	companion object
	{
		private var countId: Long = 0
	}
}