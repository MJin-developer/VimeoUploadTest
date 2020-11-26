package com.hellobiz.vimeotest

import android.net.Uri
import android.provider.MediaStore

enum class MediaStoreFileType(
    val externalContentUri: Uri,
    val mimeType: String,
    val pathByDCIM: String
) {
    IMAGE(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*", "/image"),
    AUDIO(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*", "/audio"),
    VIDEO(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*", "/video");
}