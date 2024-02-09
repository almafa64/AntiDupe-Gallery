package com.cyberegylet.antiDupeGallery.helpers

import android.os.Build
import android.os.Bundle

object Extender{
	/**
	 * Replacement for deprecated Bundle.get(key)
	 */
	fun Bundle.getObject(key: String): Any?
	{
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getParcelable(key, Object::class.java)
		else
		{
			@Suppress("DEPRECATION")
			get(key)
		}
	}
}