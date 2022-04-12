package io.millionic.feelpics.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "album_image_table")
data class AlbumImageModel(
    @PrimaryKey(autoGenerate = true)
    val albumImageId:Int,
    val albumId: String,
    val imagePath: String
): Parcelable