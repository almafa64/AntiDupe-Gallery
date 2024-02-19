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
		if ((indexImage?.modifiedDate ?: 0) < imageFile.modifiedDate) indexImage = imageFile
		size += FileManager.getSize(imageFile.file)
		count++
	}

	override fun mySetFile()
	{
		super.mySetFile()
		modifiedDate = file.lastModified()

		if (isHidden) return

		var curDir = file
		do
		{
			if (curDir.listFiles { _, name -> name.lowercase() == ".nomedia" }?.size == 1)
			{
				isHidden = true
				return
			}
			curDir = curDir.parentFile!!
		} while (curDir.path != "/storage")
	}

	companion object
	{
		private var countId: Long = 0
	}
}