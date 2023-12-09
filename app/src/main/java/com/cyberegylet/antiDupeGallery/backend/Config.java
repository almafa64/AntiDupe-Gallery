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
import java.util.Properties;

public final class Config
{
	private static final String TAG = "Config";
	private static final String CONFIG_FILE = "config";

	private static Config INSTANCE;

	private final Path filePath;
	private final ArrayList<MutationListener> mutationListeners;
	private final Properties properties;

	public static void init(@NonNull Context context)
	{
		if (INSTANCE != null)
		{
			throw new RuntimeException("Config.init can only be called once");
		}
		INSTANCE = new Config(context);
	}

	public static void save()
	{
		if (INSTANCE == null)
		{
			return;
		}

		try (BufferedWriter writer = Files.newBufferedWriter(INSTANCE.filePath))
		{
			INSTANCE.properties.store(writer, null);
			Log.i(TAG, "save: Saved config to " + INSTANCE.filePath.toString());
			dump();
		}
		catch (IOException ex)
		{
			Log.e(TAG, "save: Failed to save config file", ex);
		}
	}

	public static void attachMutationListener(@NonNull MutationListener listener)
	{
		checkInstance();
		INSTANCE.mutationListeners.add(listener);
	}

	public static String getStringProperty(@NonNull Property property)
	{
		checkInstance();
		return generalGetPropertyValue(property);
	}

	public static int getIntProperty(@NonNull Property property)
	{
		checkInstance();
		return Integer.parseInt(generalGetPropertyValue(property));
	}

	public static boolean getBooleanProperty(@NonNull Property property)
	{
		checkInstance();
		return getIntProperty(property) != 0;
	}

	public static void setStringProperty(@NonNull Property property, String value)
	{
		checkInstance();
		generalSetPropertyValue(property, value);
	}

	public static void setIntProperty(@NonNull Property property, int value)
	{
		checkInstance();
		generalSetPropertyValue(property, String.valueOf(value));
	}

	public static void setBooleanProperty(@NonNull Property property, boolean value)
	{
		checkInstance();
		setIntProperty(property, value ? 1 : 0);
	}

	public static void restoreDefaults() {
		checkInstance();
		INSTANCE.loadDefaults();
	}

	public interface MutationListener
	{
		void onChange(@NonNull Property property, String newValue);
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
		if (INSTANCE == null)
		{
			throw new RuntimeException("Config.init was not called");
		}
	}

	private static void invokeMutationListeners(@NonNull Property property, String value)
	{
		INSTANCE.mutationListeners.forEach(listener -> {
			listener.onChange(property, value);
		});
	}

	private static String generalGetPropertyValue(@NonNull Property property)
	{
		return INSTANCE.properties.getProperty(property.name);
	}

	private static void generalSetPropertyValue(@NonNull Property property, String value)
	{
		invokeMutationListeners(property, value);
		INSTANCE.properties.setProperty(property.name, value);
	}

	private void loadDefaults()
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

	private static void dump() {
		INSTANCE.properties.list(System.out);
	}

	private Config(@NonNull Context context)
	{
		this.mutationListeners = new ArrayList<>();
		this.filePath = Paths.get(context.getFilesDir().getPath(), CONFIG_FILE);
		this.properties = new Properties();
		try (BufferedReader reader = Files.newBufferedReader(this.filePath)) {
			this.properties.load(reader);
		} catch (IOException ex) {
			Log.e(TAG, "Config: Failed to load config file; using defaults", ex);
			this.loadDefaults();
		}
	}
}
