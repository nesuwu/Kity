package com.nesu.kity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileItem(
    val id: String,
    val name: String,
    val size: String,
    val fileUrl: String,
    val thumbnailUrl: String?
) : Parcelable