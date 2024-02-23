package com.cyberegylet.antiDupeGallery.helpers

import android.R.drawable.ic_dialog_alert
import android.R.string.cancel
import android.R.string.ok
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

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
	 * @param permission a permission from Manifest.permission
	 * @param title id for alert dialog title
	 * @param text id for alert dialog body text
	 * @param callback callback to run on ok (true) and cancel (false)
	 * @return true if alert dialog had to be displayed, false otherwise
	 */
	fun showRationaleIfNeeded(permission: String, title: Int, text: Int, callback: ((Boolean) -> Unit)): Boolean
	{
		return if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
		{
			AlertDialog.Builder(activity).setTitle(title).setMessage(text).setIcon(ic_dialog_alert)
				.setPositiveButton(ok) { _, _ -> callback(true) }.setNegativeButton(cancel) { _, _ -> callback(false) }
				.show()
			true
		}
		else false
	}

	/**
	 * **Should not be called quickly multiple times!**
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