package com.cyberegylet.antiDupeGallery.helpers

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.util.Date

object Utils
{
	@JvmStatic
	fun sToDate(s: Long): String = msToDate(s * 1000)

	@JvmStatic
	fun msToDate(ms: Long): String = DateFormat.getDateTimeInstance().format(Date(ms))

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

	@JvmStatic
	private fun doubleString(d: Double): String
	{
		return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP).toPlainString() + ""
	}
}