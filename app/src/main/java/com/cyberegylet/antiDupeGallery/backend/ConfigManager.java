package com.cyberegylet.antiDupeGallery.backend;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cyberegylet.antiDupeGallery.backend.ConfigManager.Config.*;

public class ConfigManager
{
	public interface OnConfigChangeListener
	{
		void OnChange(Config config, String value);
	}

	private static File configFile;
	private static final Properties configs = new Properties();
	private static final ArrayList<OnConfigChangeListener> listeners = new ArrayList<>(5);
	private static int changedConfigFlags = 0;

	public enum Config
	{
		TEXT_COLOR("fg_color"),
		BACKGROUND_COLOR("bg_color"),
		ETC_COLOR("etc_color"),
		FOLDER_COLUMN_NUMBER("folder_col"),
		IMAGE_COLUMN_NUMBER("image_col"),
		ANIMATE_GIF("anim_gif"),
		SHOW_HIDDEN("show_hidden"),
		PIN_LOCk("pin_lock"),
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

		private final String data;

		Config(String name)
		{
			data = name;
		}

		@NonNull
		@Override
		public String toString() { return data; }
	}

	public static void init(Context context)
	{
		//ConfigManager.listener = listener;
		configFile = new File(context.getFilesDir(), "config");

		if (configFile.exists()) reloadConfigs();
		else resetConfigs();
	}

	public static String getConfig(@NonNull Config config) { return configs.getProperty(config.toString()); }

	public static boolean getBooleanConfig(@NonNull Config config) { return getConfig(config).equals("1"); }

	public static int getIntConfig(@NonNull Config config) { return Integer.parseInt(getConfig(config)); }

	public static void setConfig(@NonNull Config config, @NonNull String data)
	{
		configs.setProperty(config.toString(), data);
		changedConfigFlags |= 1 << config.ordinal();
	}

	public static void setBooleanConfig(@NonNull Config config, boolean data) { setConfig(config, data ? "1" : "0"); }

	public static void setIntConfig(@NonNull Config config, int data) { setConfig(config, String.valueOf(data)); }

	public static void saveConfigs()
	{
		try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath()))
		{
			configs.store(writer, null);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		runListeners();
	}

	public static void reloadConfigs()
	{
		try (BufferedReader reader = Files.newBufferedReader(configFile.toPath()))
		{
			configs.clear();
			configs.load(reader);
			if (configs.size() == 0) resetConfigs();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		runListeners();
	}

	public static void resetConfigs()
	{
		configs.clear();
		configs.putAll(Stream.of(
				new AbstractMap.SimpleEntry<>(SHOW_HIDDEN.toString(), "0"),
				new AbstractMap.SimpleEntry<>(PIN_LOCk.toString(), ""),
				new AbstractMap.SimpleEntry<>(FOLDER_SORT.toString(), "13"),
				new AbstractMap.SimpleEntry<>(IMAGE_SORT.toString(), "00"),
				new AbstractMap.SimpleEntry<>(USE_BIN.toString(), "1"),
				new AbstractMap.SimpleEntry<>(TEXT_COLOR.toString(), "#FFF"),
				new AbstractMap.SimpleEntry<>(BACKGROUND_COLOR.toString(), "#000"),
				new AbstractMap.SimpleEntry<>(ETC_COLOR.toString(), "#30AFCF"),
				new AbstractMap.SimpleEntry<>(FOLDER_COLUMN_NUMBER.toString(), "2"),
				new AbstractMap.SimpleEntry<>(IMAGE_COLUMN_NUMBER.toString(), "3"),
				new AbstractMap.SimpleEntry<>(ANIMATE_GIF.toString(), "0")
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

		changedConfigFlags = Arrays.stream(values()).mapToInt(c -> 1 << c.ordinal()).sum();
		saveConfigs();
	}

	private static void runListeners()
	{
		for (Config c : Config.values())
		{
			if ((1 << c.ordinal() & changedConfigFlags) != 0)
			{
				listeners.forEach(l -> l.OnChange(c, configs.getProperty(c.toString())));
			}
		}
	}

	public static void list() { configs.list(System.out); }

	public static void addListener(@NonNull OnConfigChangeListener listener) { listeners.add(listener); }

	public static void removeListener(@NonNull OnConfigChangeListener listener) { listeners.remove(listener); }
}