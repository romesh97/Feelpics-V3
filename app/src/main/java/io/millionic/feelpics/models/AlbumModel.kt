package io.millionic.feelpics.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "album_table")
data class AlbumModel(
    @PrimaryKey(autoGenerate = false)
    val albumId:String,
    var albumName:String,
    val startDate: String,
    val endDate: String,
    val quantity:String,
    val place: String,
    val Labels: String,
    var thumbnail: String
):Parcelable
