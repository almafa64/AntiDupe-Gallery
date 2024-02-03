package com.cyberegylet.antiDupeGallery.models

import java.io.File

abstract class FileEntry
{
	private var _file: File? = null
	var file: File
		get() = _file!!
		set(value)
		{
			_file = value
			mySetFile()
		}

	lateinit var name: String
		protected set
	var size: Long = 0
		protected set
	var modifiedDate: Long = 0
		protected set
	var isHidden = false
		protected set
	var id: Long = 0
		protected set

	@JvmOverloads
	constructor(file: File, id: Long = -1)
	{
		this.file = file
		this.id = id
	}

	protected constructor()

	val path: String
		get() = file.path

	protected open fun mySetFile()
	{
		name = file.name
		isHidden = file.path.contains("/.")
	}
}