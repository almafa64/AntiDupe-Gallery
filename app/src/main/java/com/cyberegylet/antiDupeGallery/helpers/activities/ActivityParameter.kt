package com.cyberegylet.antiDupeGallery.helpers.activities

import android.net.Uri

class ActivityParameter<T : Any?> private constructor(val name: String, val data: Any, val type: Type)
{
	enum class Type
	{
		INT,
		STRING,
		STRING_ARR,
		BOOL,
		URI,
		PARCELABLE
	}

	constructor(name: String, data: Boolean) : this(name, data, Type.BOOL)
	constructor(name: String, data: Int) : this(name, data, Type.INT)
	constructor(name: String, data: String) : this(name, data, Type.STRING)
	constructor(name: String, data: Array<String>) : this(name, data, Type.STRING_ARR)
	constructor(name: String, data: Uri) : this(name, data, Type.URI)
	constructor(name: String, data: ArrayList<T>) : this(name, data, Type.PARCELABLE)
}