package com.cyberegylet.antiDupeGallery.backend;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class Config
{
	private static final String TAG = "Config";
	private static final String CONFIG_FILE = "config";

	private static Path filePath;
	private static Properties properties;

	public static void init(@NonNull Context context)
	{
		if (properties != null)
		{
			Log.e(TAG, "Already called Config.init()");
			return;
		}

		filePath = Paths.get(context.getFilesDir().getPath(), CONFIG_FILE);
		properties = new Properties();
		try (BufferedReader reader = Files.newBufferedReader(filePath))
		{
			properties.load(reader);
		}
		catch (IOException ex)
		{
			Log.e(TAG, "Config: Failed to load config file; using defaults", ex);
			loadDefaults();
			save();
		}
	}

	public static void save()
	{
		checkInstance();
		try (BufferedWriter writer = Files.newBufferedWriter(filePath))
		{
			properties.store(writer, null);
			Log.i(TAG, "save: Saved config to " + filePath.toString());
			dump();
		}
		catch (IOException ex)
		{
			Log.e(TAG, "save: Failed to save config file", ex);
		}
	}

	public static String getStringProperty(@NonNull Property property)
	{
		checkInstance();
		return properties.getProperty(property.name);
	}

	public static int getIntProperty(@NonNull Property property)
	{
		checkInstance();
		return Integer.parseInt(properties.getProperty(property.name));
	}

	public static boolean getBooleanProperty(@NonNull Property property)
	{
		checkInstance();
		return properties.getProperty(property.name).equals("1");
	}

	public static void setStringProperty(@NonNull Property property, String value)
	{
		checkInstance();
		properties.setProperty(property.name, value);
	}

	public static void setIntProperty(@NonNull Property property, int value)
	{
		checkInstance();
		properties.setProperty(property.name, String.valueOf(value));
	}

	public static void setBooleanProperty(@NonNull Property property, boolean value)
	{
		checkInstance();
		properties.setProperty(property.name, value ? "1" : "0");
	}

	public static void restoreDefaults()
	{
		checkInstance();
		loadDefaults();
	}

	public enum Property
	{
		TEXT_COLOR("fg_color"),
		BACKGROUND_COLOR("bg_color"),
		ETC_COLOR("etc_color"),
		FOLDER_COLUMN_NUMBER("folder_col"),
		IMAGE_COLUMN_NUMBER("image_col"),
		ANIMATE_GIF("anim_gif"),
		SHOW_HIDDEN("show_hidden"),
		PIN_LOCK("pin_lock"),
		USE_BIN("bin"),

		/**
		 * 1. number: is_ascending<br>
		 * 2. number: sort type (0: mod date, 1: create date, 2: size, 3: name)
		 */
		FOLDER_SORT("f_sort"),
		/**
		 * 1. number: is_ascending<br>
		 * 2. number: sort type (0: mod date, 1: create date, 2: size, 3: name)
		 */
		IMAGE_SORT("i_sort");

		private final String name;

		Property(String name)
		{
			this.name = name;
		}

		@NonNull
		@Override
		public String toString() { return name; }
	}

	private static void checkInstance()
	{
		if (properties == null) throw new RuntimeException("Config.init was not called");
	}

	private static void loadDefaults()
	{
		setBooleanProperty(Property.SHOW_HIDDEN, false);
		setStringProperty(Property.PIN_LOCK, "");
		setStringProperty(Property.FOLDER_SORT, "13");
		setStringProperty(Property.IMAGE_SORT, "00");
		setBooleanProperty(Property.USE_BIN, true);
		setStringProperty(Property.TEXT_COLOR, "#FFF");
		setStringProperty(Property.BACKGROUND_COLOR, "#000");
		setStringProperty(Property.ETC_COLOR, "#30AFCF");
		setIntProperty(Property.FOLDER_COLUMN_NUMBER, 2);
		setIntProperty(Property.IMAGE_COLUMN_NUMBER, 3);
		setBooleanProperty(Property.ANIMATE_GIF, false);
	}

	private static void dump()
	{
		properties.list(System.out);
	}
}
