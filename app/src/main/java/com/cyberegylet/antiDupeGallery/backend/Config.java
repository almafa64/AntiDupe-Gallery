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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
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
			List<Object> toRemove = new ArrayList<>();
			Property[] props = Property.values();
			for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); )
			{
				String name = (String) e.nextElement();
				if (Arrays.stream(props).noneMatch(p -> p.name.equals(name))) toRemove.add(name);
			}
			for (Object e : toRemove)
			{
				properties.remove(e);
			}
			for (Property p : props)
			{
				if (!properties.containsKey(p.name)) restoreDefault(p);
			}
		}
		catch (IOException ex)
		{
			Log.e(TAG, "Config: Failed to load config file; using defaults", ex);
			restoreDefaults();
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
		properties.clear();
		for (Property p : Property.values())
		{
			restoreDefault(p);
		}
	}

	public static void restoreDefault(@NonNull Property property)
	{
		checkInstance();
		switch (property)
		{
			case SHOW_HIDDEN:
				setBooleanProperty(Property.SHOW_HIDDEN, false);
				break;
			case PIN_LOCK:
				setStringProperty(Property.PIN_LOCK, "");
				break;
			case FOLDER_SORT:
				setStringProperty(Property.FOLDER_SORT, "13");
				break;
			case IMAGE_SORT:
				setStringProperty(Property.IMAGE_SORT, "00");
				break;
			case USE_BIN:
				setBooleanProperty(Property.USE_BIN, true);
				break;
			case TEXT_COLOR:
				setStringProperty(Property.TEXT_COLOR, "#FFF");
				break;
			case BACKGROUND_COLOR:
				setStringProperty(Property.BACKGROUND_COLOR, "#000");
				break;
			case ETC_COLOR:
				setStringProperty(Property.ETC_COLOR, "#30AFCF");
				break;
			case FOLDER_COLUMN_NUMBER:
				setIntProperty(Property.FOLDER_COLUMN_NUMBER, 2);
				break;
			case IMAGE_COLUMN_NUMBER:
				setIntProperty(Property.IMAGE_COLUMN_NUMBER, 3);
				break;
			case ANIMATE_GIF:
				setBooleanProperty(Property.ANIMATE_GIF, false);
				break;
			case DO_ANIMATIONS:
				setBooleanProperty(Property.DO_ANIMATIONS, true);
				break;
		}
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
		DO_ANIMATIONS("do_anims"),

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

	private static void dump()
	{
		properties.list(System.out);
	}
}
