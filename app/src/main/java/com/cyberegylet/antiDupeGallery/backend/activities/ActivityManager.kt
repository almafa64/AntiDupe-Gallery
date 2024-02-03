package com.cyberegylet.antiDupeGallery.backend.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.PopupWindow

class ActivityManager(val activity: Activity)
{
	@JvmOverloads
	fun makePopupWindow(
		layoutId: Int,
		listener: PopupWindow.OnDismissListener? = null
	): PopupWindow = makePopupWindow(activity, layoutId, listener)

	fun switchActivity(newActivity: Class<out Activity>, vararg params: ActivityParameter<*>)
	{
		switchActivity(activity, newActivity, -1, *params)
	}

	fun switchActivity(newActivity: Class<out Activity>, reqCode: Int, vararg params: ActivityParameter<*>)
	{
		switchActivity(activity, newActivity, reqCode, *params)
	}

	fun goBack(vararg params: ActivityParameter<*>) = goBack(activity, *params)

	fun getParam(name: String): Any? = getParam(activity, name)

	fun <T : Parcelable> getListParam(name: String): ArrayList<T>? = getListParam(activity, name)

	companion object
	{
		@JvmStatic
		@JvmOverloads
		fun makePopupWindow(
			activity: Activity,
			layoutId: Int,
			listener: PopupWindow.OnDismissListener? = null
		): PopupWindow
		{
			val root = activity.window.decorView as ViewGroup
			val popup = activity.layoutInflater.inflate(layoutId, root, false) as ViewGroup
			val window = PopupWindow(
				popup,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				true
			)
			applyDim(root, 0.5f)
			window.showAtLocation(root, Gravity.CENTER, 0, 0)
			window.setOnDismissListener {
				listener?.onDismiss()
				clearDim(root)
			}
			return window
		}

		@JvmStatic
		fun switchActivity(
			activity: Activity, newActivity: Class<out Activity>, vararg params: ActivityParameter<*>
		)
		{
			switchActivity(activity, newActivity, -1, *params)
		}

		@JvmStatic
		fun switchActivity(
			activity: Activity, newActivity: Class<out Activity>, reqCode: Int, vararg params: ActivityParameter<*>
		)
		{
			val intent = putParams(Intent(activity, newActivity), *params)
			if (reqCode != -1) activity.startActivityForResult(intent, reqCode) else activity.startActivity(intent)
		}

		private fun putParams(intent: Intent, vararg params: ActivityParameter<*>): Intent
		{
			for (param in params)
			{
				when (param.type)
				{
					ActivityParameter.Type.INT -> intent.putExtra(param.name, param.data as Int)
					ActivityParameter.Type.STRING -> intent.putExtra(param.name, param.data as String)
					ActivityParameter.Type.STRING_ARR -> intent.putExtra(param.name, param.data as Array<String>)
					ActivityParameter.Type.BOOL -> intent.putExtra(param.name, param.data as Boolean)
					ActivityParameter.Type.URI -> intent.putExtra(param.name, param.data as Uri)
					ActivityParameter.Type.PARCELABLE -> intent.putParcelableArrayListExtra(
						param.name,
						param.data as ArrayList<out Parcelable>
					)
				}
			}
			return intent
		}

		@JvmStatic
		fun goBack(activity: Activity, vararg params: ActivityParameter<*>)
		{
			val i = putParams(Intent(), *params)
			activity.setResult(Activity.RESULT_OK, i)
			activity.finish()
		}

		@JvmStatic
		fun getParam(activity: Activity, name: String): Any? = activity.intent.extras?.get(name)

		@JvmStatic
		fun <T : Parcelable> getListParam(activity: Activity, name: String): ArrayList<T>? =
			activity.intent.getParcelableArrayListExtra(name)

		@JvmStatic
		fun applyDim(parent: ViewGroup, dimAmount: Float)
		{
			val dim: Drawable = ColorDrawable(Color.BLACK)
			dim.setBounds(0, 0, parent.width, parent.height)
			dim.alpha = (255 * dimAmount).toInt()
			parent.overlay.add(dim)
		}

		@JvmStatic
		fun clearDim(parent: ViewGroup) = parent.overlay.clear()
	}
}