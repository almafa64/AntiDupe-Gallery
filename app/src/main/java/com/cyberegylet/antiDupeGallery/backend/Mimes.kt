package com.cyberegylet.antiDupeGallery.backend

import java.io.File

object Mimes
{
	@JvmField
	val MIME_VIDEOS = arrayOf("video/mpeg", "video/mp4", "video/webm", "video/3gpp", "video/avi", "video/quicktime")

	@JvmField
	val MIME_IMAGES =
		arrayOf("image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp", "image/svg", "image/ico")

	@JvmField
	val PHOTO_EXTENSIONS = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif")

	@JvmField
	val VIDEO_EXTENSIONS = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")

	@JvmStatic
	fun isImage(path: String) = PHOTO_EXTENSIONS.any { path.endsWith(it, true) }

	@JvmStatic
	fun isVideo(path: String) = VIDEO_EXTENSIONS.any { path.endsWith(it, true) }

	@JvmStatic
	fun getMimeType(file: File) = getMimeType(file.path)

	// https://github.com/SimpleMobileTools/Simple-Commons/blob/37078b151c41bec912b9c9b34f33500775f63814/commons/src/main/kotlin/com/simplemobiletools/commons/extensions/String.kt#L324
	@JvmStatic
	fun getMimeType(path: String): String? = typesMap[FileManager.getFileExtension(path).lowercase()]

	@JvmStatic
	fun getMimeEnumType(path: String): Type
	{
		return when
		{
			isImage(path) -> Type.IMAGE
			isVideo(path) -> Type.VIDEO
			else -> Type.UNKNOWN
		}
	}

	val typesMap = HashMap<String, String>().apply {
		put("3g2", "video/3gpp2")
		put("3gp", "video/3gpp")
		put("3gp2", "video/3gpp2")
		put("3gpp", "video/3gpp")
		put("art", "image/x-jg")
		put("asf", "video/x-ms-asf")
		put("asr", "video/x-ms-asf")
		put("asx", "video/x-ms-asf")
		put("avi", "video/x-msvideo")
		put("axv", "video/annodex")
		put("bmp", "image/bmp")
		put("cmx", "image/x-cmx")
		put("cod", "image/cis-cod")
		put("dib", "image/bmp")
		put("dif", "video/x-dv")
		put("divx", "video/divx")
		put("dng", "image/x-adobe-dng")
		put("dv", "video/x-dv")
		put("flv", "video/x-flv")
		put("gif", "image/gif")
		put("ico", "image/x-icon")
		put("ief", "image/ief")
		put("ivf", "video/x-ivf")
		put("jfif", "image/pjpeg")
		put("jpe", "image/jpeg")
		put("jpeg", "image/jpeg")
		put("jpg", "image/jpeg")
		put("lsf", "video/x-la-asf")
		put("lsx", "video/x-la-asf")
		put("m1v", "video/mpeg")
		put("m2t", "video/vnd.dlna.mpeg-tts")
		put("m2ts", "video/vnd.dlna.mpeg-tts")
		put("m2v", "video/mpeg")
		put("m4v", "video/x-m4v")
		put("mac", "image/x-macpaint")
		put("mkv", "video/x-matroska")
		put("mod", "video/mpeg")
		put("mov", "video/quicktime")
		put("movie", "video/x-sgi-movie")
		put("mp2", "video/mpeg")
		put("mp2v", "video/mpeg")
		put("mp4", "video/mp4")
		put("mp4v", "video/mp4")
		put("mpa", "video/mpeg")
		put("mpe", "video/mpeg")
		put("mpeg", "video/mpeg")
		put("mpg", "video/mpeg")
		put("mpv2", "video/mpeg")
		put("mqv", "video/quicktime")
		put("mts", "video/vnd.dlna.mpeg-tts")
		put("nsc", "video/x-ms-asf")
		put("ogv", "video/ogg")
		put("pbm", "image/x-portable-bitmap")
		put("pct", "image/pict")
		put("pgm", "image/x-portable-graymap")
		put("pic", "image/pict")
		put("pict", "image/pict")
		put("png", "image/png")
		put("pnm", "image/x-portable-anymap")
		put("pnt", "image/x-macpaint")
		put("pntg", "image/x-macpaint")
		put("pnz", "image/png")
		put("ppm", "image/x-portable-pixmap")
		put("qt", "video/quicktime")
		put("qti", "image/x-quicktime")
		put("qtif", "image/x-quicktime")
		put("ras", "image/x-cmu-raster")
		put("rf", "image/vnd.rn-realflash")
		put("rgb", "image/x-rgb")
		put("svg", "image/svg+xml")
		put("tif", "image/tiff")
		put("tiff", "image/tiff")
		put("ts", "video/vnd.dlna.mpeg-tts")
		put("tts", "video/vnd.dlna.mpeg-tts")
		put("vbk", "video/mpeg")
		put("wbmp", "image/vnd.wap.wbmp")
		put("wdp", "image/vnd.ms-photo")
		put("webm", "video/webm")
		put("webp", "image/webp")
		put("wm", "video/x-ms-wm")
		put("wmp", "video/x-ms-wmp")
		put("wmv", "video/x-ms-wmv")
		put("wmx", "video/x-ms-wmx")
		put("wvx", "video/x-ms-wvx")
		put("xbm", "image/x-xbitmap")
		put("xpm", "image/x-xpixmap")
		put("xwd", "image/x-xwindowdump")
	}

	enum class Type
	{
		UNKNOWN,
		IMAGE,
		VIDEO
	}
}