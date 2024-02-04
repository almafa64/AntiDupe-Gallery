package com.cyberegylet.antiDupeGallery.helpers

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class PermissionManager(val activity: ComponentActivity)
{
	private var callback: ((Array<String>) -> Unit)? = null

	private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
		activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
			val notPermitted = mutableListOf<String>()
			permissions.forEach { if (!it.value) notPermitted.add(it.key) }
			callback?.invoke(notPermitted.toTypedArray())
			callback = null
		}

	@JvmOverloads
	fun requestPermissions(vararg permissions: String, callback: ((Array<String>) -> Unit)? = null): Boolean
	{
		if (this.callback != null) return false
		this.callback = callback
		requestPermissionLauncher.launch(arrayOf(*permissions))
		return true
	}
}