package com.cyberegylet.antiDupeGallery.backend;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cyberegylet.antiDupeGallery.backend.ConfigManager.Config.*;

public class ConfigManager
{
	/*public interface OnConfigLoadListener
	{
		void OnLoad();
	}*/

	private static File configFile;
	private static final Properties configs = new Properties();
	//private static OnConfigLoadListener listener;

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
		else
		{
			configs.putAll(Stream.of(
					new AbstractMap.SimpleEntry<>(SHOW_HIDDEN, "0"),
					new AbstractMap.SimpleEntry<>(PIN_LOCk, ""),
					new AbstractMap.SimpleEntry<>(FOLDER_SORT, "13"),
					new AbstractMap.SimpleEntry<>(IMAGE_SORT, "00"),
					new AbstractMap.SimpleEntry<>(USE_BIN, "1"),
					new AbstractMap.SimpleEntry<>(TEXT_COLOR, "#FFF"),
					new AbstractMap.SimpleEntry<>(BACKGROUND_COLOR, "#000"),
					new AbstractMap.SimpleEntry<>(ETC_COLOR, "#30AFCF"),
					new AbstractMap.SimpleEntry<>(FOLDER_COLUMN_NUMBER, "2"),
					new AbstractMap.SimpleEntry<>(IMAGE_COLUMN_NUMBER, "3"),
					new AbstractMap.SimpleEntry<>(ANIMATE_GIF, "1")
			).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

			saveConfigs();
		}
	}

	public static String getConfig(@NonNull Config config) { return configs.getProperty(config.toString()); }

	public static void setConfig(@NonNull Config config, @Nullable String data)
	{
		//ToDo make data check

		if (data == null) configs.remove(config);
		else configs.setProperty(config.toString(), data);
	}

	public static void removeConfig(@NonNull Config config) { setConfig(config, null); }

	public static void saveConfigs()
	{
		try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath()))
		{
			configs.store(writer, null);
			//listener.OnLoad();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void reloadConfigs()
	{
		try (BufferedReader reader = Files.newBufferedReader(configFile.toPath()))
		{
			configs.clear();
			configs.load(reader);
			//listener.OnLoad();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	//ToDo make wrapper functions?
}
