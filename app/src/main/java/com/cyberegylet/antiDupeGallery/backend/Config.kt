package com.cyberegylet.antiDupeGallery.backend

import android.content.Context
import android.util.Log
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Arrays
import java.util.Properties

object Config
{
	private const val TAG = "Config"
	private const val CONFIG_FILE = "config"
	private var filePath: Path? = null
	private var _properties: Properties? = null
	private val properties: Properties
		get() = _properties ?: throw RuntimeException("Config.init was not called")

	@JvmStatic
	fun init(context: Context)
	{
		if (_properties != null)
		{
			Log.i(TAG, "Already called Config.init()")
			return
		}
		filePath = Paths.get(context.filesDir.path, CONFIG_FILE)
		_properties = Properties()
		try
		{
			Files.newBufferedReader(filePath).use { reader ->
				properties.load(reader)
				val toRemove: MutableList<Any> = ArrayList()
				val props = Property.entries.toTypedArray()
				val e = properties.propertyNames()
				while (e.hasMoreElements())
				{
					val name = e.nextElement() as String
					if (Arrays.stream(props).noneMatch { p: Property -> p.name == name }) toRemove.add(name)
				}
				for (r in toRemove)
				{
					properties.remove(r)
				}
				for (p in props)
				{
					if (!properties.containsKey(p.name)) restoreDefault(p)
				}
			}
		}
		catch (ex: IOException)
		{
			Log.w(TAG, "Config: Failed to load config file; using defaults", ex)
			restoreDefaults()
			save()
		}
	}

	@JvmStatic
	fun save()
	{
		try
		{
			Files.newBufferedWriter(filePath).use { writer -> properties.store(writer, null) }
		}
		catch (ex: IOException)
		{
			Log.e(TAG, "save: Failed to save config file", ex)
		}
	}

	@JvmStatic
	fun getStringProperty(property: Property): String = properties.getProperty(property.name)

	@JvmStatic
	fun getIntProperty(property: Property) = properties.getProperty(property.name).toInt()

	@JvmStatic
	fun getBooleanProperty(property: Property) = properties.getProperty(property.name) == "1"

	@JvmStatic
	fun getArrayProperty(property: Property): Array<String> =
		properties.getProperty(property.name).split("|").dropLastWhile { it.isEmpty() }.toTypedArray()

	@JvmStatic
	fun setStringProperty(property: Property, value: String?)
	{
		properties.setProperty(property.name, value)
	}

	@JvmStatic
	fun setIntProperty(property: Property, value: Int)
	{
		properties.setProperty(property.name, value.toString())
	}

	@JvmStatic
	fun setBooleanProperty(property: Property, value: Boolean)
	{
		properties.setProperty(property.name, if (value) "1" else "0")
	}

	@JvmStatic
	fun setArrayProperty(property: Property, values: Array<String?>)
	{
		properties.setProperty(property.name, values.joinToString("|"))
	}

	@JvmStatic
	fun restoreDefaults()
	{
		properties.clear()
		for (p in Property.entries)
		{
			restoreDefault(p)
		}
	}

	@JvmStatic
	fun restoreDefault(property: Property)
	{
		when (property)
		{
			Property.SHOW_HIDDEN -> setBooleanProperty(Property.SHOW_HIDDEN, false)
			Property.PIN_LOCK -> setStringProperty(Property.PIN_LOCK, "")
			Property.ALBUM_SORT -> setStringProperty(Property.ALBUM_SORT, "00")
			Property.IMAGE_SORT -> setStringProperty(Property.IMAGE_SORT, "00")
			Property.USE_BIN -> setBooleanProperty(Property.USE_BIN, true)
			Property.TEXT_COLOR -> setStringProperty(Property.TEXT_COLOR, "#FFF")
			Property.BACKGROUND_COLOR -> setStringProperty(Property.BACKGROUND_COLOR, "#000")
			Property.ETC_COLOR -> setStringProperty(Property.ETC_COLOR, "#30AFCF")
			Property.ALBUM_COLUMN_NUMBER -> setIntProperty(Property.ALBUM_COLUMN_NUMBER, 2)
			Property.IMAGE_COLUMN_NUMBER -> setIntProperty(Property.IMAGE_COLUMN_NUMBER, 3)
			Property.ANIMATE_GIF -> setBooleanProperty(Property.ANIMATE_GIF, false)
			Property.DO_ANIMATIONS -> setBooleanProperty(Property.DO_ANIMATIONS, true)
			Property.BLOCKED_PATHS -> setArrayProperty(Property.BLOCKED_PATHS, arrayOf())
		}
	}

	enum class Property(val propertyName: String)
	{
		TEXT_COLOR("fg_color"),
		BACKGROUND_COLOR("bg_color"),
		ETC_COLOR("etc_color"),
		ALBUM_COLUMN_NUMBER("folder_col"),
		IMAGE_COLUMN_NUMBER("image_col"),
		ANIMATE_GIF("anim_gif"),
		SHOW_HIDDEN("show_hidden"),
		PIN_LOCK("pin_lock"),
		USE_BIN("bin"),
		DO_ANIMATIONS("do_anims"),
		BLOCKED_PATHS("path_block"),

		/**
		 * 1. number: is_ascending<br></br>
		 * 2. number: sort type (0: mod date, 1: create date, 2: size, 3: name)
		 */
		ALBUM_SORT("f_sort"),

		/**
		 * 1. number: is_ascending<br></br>
		 * 2. number: sort type (0: mod date, 1: create date, 2: size, 3: name)
		 */
		IMAGE_SORT("i_sort");

		override fun toString(): String = propertyName
	}
}