package com.cyberegylet.antiDupeGallery.helpers

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

// ToDo move requestPermissions into init

class PermissionManager(val activity: ComponentActivity)
{
	private var callback: ((Array<String>) -> Unit)? = null

	private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
		activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			val denied = mutableListOf<String>()
			permissions.forEach { if (!it.value) denied.add(it.key) }
			callback?.invoke(denied.toTypedArray())
			callback = null
		}

	/**
	 * @param callback a callback to work with permissions that have been denied
	 * @param permissions permissions to request access
	 * @return true if successfully called launch(), false otherwise
	 */
	fun requestPermissions(callback: ((Array<String>) -> Unit), vararg permissions: String): Boolean
	{
		if (this.callback != null) return false
		this.callback = callback
		requestPermissionLauncher.launch(arrayOf(*permissions))
		return true
	}
}