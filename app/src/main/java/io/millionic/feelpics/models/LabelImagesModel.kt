package io.millionic.feelpics.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "label_images_table",
)
data class LabelImagesModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var isLocation: Boolean,
    val imageId: String,
    var imageLabel: String,
): Parcelable