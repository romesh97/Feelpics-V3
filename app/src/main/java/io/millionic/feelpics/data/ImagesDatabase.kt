package io.millionic.feelpics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.models.LabelImagesModel

@Database(entities = [ImageModel::class, LabelImagesModel::class, AlbumModel::class, AlbumImageModel::class], version = 1, exportSchema = false)
abstract class ImagesDatabase: RoomDatabase() {

    abstract fun userDao(): ImageDao

    companion object{
        @Volatile
        private var INSTANCE: ImagesDatabase?= null

        fun getDatabase(context: Context): ImagesDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ImagesDatabase::class.java,
                    "images_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }

    }

}