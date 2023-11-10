package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Folder
{
	public final List<ImageFile> images = new ArrayList<>();
	public final Uri path;
	@NotNull
	public final String name;

	@NonNull
	public String getName()
	{
		return name;
	}

	public Folder(Uri path)
	{
		this.path = path;
		this.name = Objects.requireNonNull(path.getLastPathSegment());
	}

	public Folder(Folder folder, boolean copyImages)
	{
		this.name = folder.name;
		this.path = folder.path;
		if (copyImages) images.addAll(folder.images);
	}

	public Folder(Folder folder) { this(folder, false); }
}