package com.cyberegylet.antiDupeGallery.helpers

import android.os.Build
import android.os.Bundle
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.Date

object Utils
{
	/**
	 * Converts seconds to locale Date format string
	 * @param s seconds
	 * @return locale Date format
	 */
	@JvmStatic
	fun sToDate(s: Long): String = msToDate(s * 1000)

	/**
	 * Converts milliseconds to locale Date format string
	 * @param ms milliseconds
	 * @return locale Date format
	 */
	@JvmStatic
	fun msToDate(ms: Long): String = DateFormat.getDateTimeInstance().format(Date(ms))

	/**
	 * Converts bytes to human readable format (e.g.: 420KB, 69MB)
	 * @param size bytes
	 * @return human readable format
	 */
	@JvmStatic
	fun getByteStringFromSize(size: Long): String
	{
		val kb = 1024.0
		return when
		{
			size >= kb * kb * kb * kb -> doubleString(size / (kb * kb * kb * kb)) + " TB"
			size >= kb * kb * kb -> doubleString(size / (kb * kb * kb)) + " GB"
			size >= kb * kb -> doubleString(size / (kb * kb)) + " MB"
			size >= kb -> doubleString(size / kb) + " KB"
			else -> doubleString(size.toDouble()) + " B"
		}
	}

	/**
	 * Converts Double to String rounded to precision
	 * @param d double value
	 * @param precision can be negative and positive
	 * @return rounded Double converted to String
	 */
	@JvmStatic
	@JvmOverloads
	fun doubleString(d: Double, precision: Int = 2): String
	{
		return BigDecimal.valueOf(d).setScale(precision, RoundingMode.HALF_UP).toPlainString()
	}
}

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