package com.cyberegylet.antiDupeGallery.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FilteredAlbum
{
	private final ImageFile indexImage;
	@NotNull
	private final String name;
	private final long size;
	private final long count;
	private final String digestHex;

	public FilteredAlbum(File indexFile, @NonNull String name, int count, String digestHex)
	{
		indexImage = new ImageFile(indexFile);
		this.name = name;
		this.count = count;
		this.size = 0;
		this.digestHex = digestHex;
	}

	public ImageFile getIndexImage() { return indexImage; }

	@NonNull
	public String getName() { return name; }

	public long getSize() { return size; }

	public long getCount() { return count; }

	public String getDigestHex() { return digestHex; }
}