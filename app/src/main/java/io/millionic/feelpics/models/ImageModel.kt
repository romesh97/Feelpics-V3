package io.millionic.feelpics.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "images_table")
data class ImageModel(
    val imgPath: String,
    @PrimaryKey(autoGenerate = false)
    val imageId: String,
    val date: String,
    val time: String,
): Parcelable

