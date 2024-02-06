package com.cyberegylet.antiDupeGallery.helpers.activities

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

class ActivityManager(@JvmField val activity: Activity)
{
	/**
	 * Makes a custom popup dialog
	 * @param layoutId the id of the layout from R.layout for the dialog
	 * @param listener listener to catch dismiss event
	 */
	@JvmOverloads
	fun makePopupWindow(
		layoutId: Int,
		listener: PopupWindow.OnDismissListener? = null
	): PopupWindow = makePopupWindow(activity, layoutId, listener)

	/**
	 * @param newActivity the activity to switch to
	 * @param params ActivityParams to pass to newActivity
	 * @param reqCode the code to pass to onActivityResult, if -1 then it won't be called. Default is -1.
	 */
	@JvmOverloads
	fun switchActivity(newActivity: Class<out Activity>, reqCode: Int = -1, vararg params: ActivityParameter<*>)
	{
		switchActivity(activity, newActivity, reqCode, *params)
	}

	/**
	 * Closes this Activity and returns back to parent Activity (which called switchActivity()).
	 * @param params ActivityParameters to pass back to parent Activity
	 */
	fun goBack(vararg params: ActivityParameter<*>) = goBack(activity, *params)

	/**
	 * Gets data from passed ActivityParameter.
	 * @param name name of the ActivityParameter
	 */
	fun getParam(name: String): Any? = getParam(activity, name)

	/**
	 * Gets list type data from passed ActivityParameter.
	 * @param name name of the ActivityParameter
	 */
	fun <T : Parcelable> getListParam(name: String): ArrayList<T>? = getListParam(activity, name)

	companion object
	{
		/**
		 * Makes a custom popup dialog
		 * @param activity calling Activity
		 * @param layoutId the id of the layout from R.layout for the dialog
		 * @param listener listener to catch dismiss event
		 */
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

		/**
		 * @param activity calling Activity
		 * @param newActivity the activity to switch to
		 * @param params ActivityParams to pass to newActivity
		 * @param reqCode the code to pass to onActivityResult, if -1 then it won't be called. Default is -1.
		 */
		@JvmStatic
		@JvmOverloads
		fun switchActivity(
			activity: Activity, newActivity: Class<out Activity>, reqCode: Int = -1, vararg params: ActivityParameter<*>
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

		/**
		 * Closes this Activity and returns back to parent Activity (which called switchActivity()).
		 * @param activity calling Activity
		 * @param activity calling Activity
		 * @param params ActivityParameters to pass back to parent Activity
		 */
		@JvmStatic
		fun goBack(activity: Activity, vararg params: ActivityParameter<*>)
		{
			val i = putParams(Intent(), *params)
			activity.setResult(Activity.RESULT_OK, i)
			activity.finish()
		}

		/**
		 * Gets data from passed ActivityParameter.
		 * @param activity calling Activity
		 * @param name name of the ActivityParameter
		 */
		@JvmStatic
		fun getParam(activity: Activity, name: String): Any? = activity.intent.extras?.get(name)

		/**
		 * Gets list type data from passed ActivityParameter.
		 * @param activity calling Activity
		 * @param name name of the ActivityParameter
		 */
		@JvmStatic
		fun <T : Parcelable> getListParam(activity: Activity, name: String): ArrayList<T>? =
			activity.intent.getParcelableArrayListExtra(name)

		/**
		 * Applies a dim effect to a ViewGroup
		 * @param parent the ViewGroup where dim will be applied
		 * @param dimAmount a float from 0 to 1
		 */
		@JvmStatic
		fun applyDim(parent: ViewGroup, dimAmount: Float)
		{
			val dim: Drawable = ColorDrawable(Color.BLACK)
			dim.setBounds(0, 0, parent.width, parent.height)
			dim.alpha = (255 * dimAmount).toInt()
			parent.overlay.add(dim)
		}

		/**
		 * Clears a dim effect from a ViewGroup
		 * @param parent the ViewGroup where dim will be cleared
		 */
		@JvmStatic
		fun clearDim(parent: ViewGroup) = parent.overlay.clear()
	}
}