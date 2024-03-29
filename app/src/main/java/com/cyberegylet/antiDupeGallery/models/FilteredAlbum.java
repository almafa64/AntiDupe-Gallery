package com.cyberegylet.antiDupeGallery.models;

import com.cyberegylet.antiDupeGallery.backend.Mimes;

import java.io.File;

public class FilteredAlbum
{
	private ImageFile indexImage;

	private String name;
	private long size;
	private long count;
	private String digestHex;

	public FilteredAlbum(File indexFile, String name, int count, String digestHex)
	{
		setData(indexFile, name, count, digestHex);
	}

	public void setData(File indexFile, String name, int count, String digestHex)
	{
		if (indexFile != null) this.indexImage = new ImageFile(indexFile, Mimes.Type.UNKNOWN);
		if (name != null) this.name = name;
		this.count = count;
		this.size = 0; //ToDo calculate size
		if (digestHex != null) this.digestHex = digestHex;
	}

	public ImageFile getIndexImage() { return indexImage; }

	public String getName() { return name; }

	public long getSize() { return size; }

	public long getCount() { return count; }

	public String getDigestHex() { return digestHex; }
}