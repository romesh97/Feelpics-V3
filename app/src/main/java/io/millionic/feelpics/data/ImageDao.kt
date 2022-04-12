package io.millionic.feelpics.data

import androidx.lifecycle.LiveData
import androidx.room.*
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.models.LabelImagesModel

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addImage(imageModel: ImageModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addLabelImage(labelImagesModel: LabelImagesModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addAlbum(albumModel: AlbumModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addAlbumImage(albumImageModel: AlbumImageModel)

    @Delete
    fun deleteAlbumImage(albumImageModel: AlbumImageModel)

    @Delete
    fun deleteImage(imageModel: ImageModel)

    @Delete
    fun deleteAlbum(album: AlbumModel)

    @Query("DELETE FROM label_images_table WHERE imageId = :imageId")
    fun deleteLabel(imageId: String)

    @Query("DELETE FROM album_image_table WHERE albumId = :albumId")
    fun deleteAlbumImages(albumId: String)

    @Query("SELECT * FROM images_table ORDER BY imageId ASC")
    fun readAllImages(): LiveData<List<ImageModel>>

    @Query("SELECT * FROM album_table")
    fun readAllAlbum(): LiveData<List<AlbumModel>>

    @Query("SELECT * FROM album_image_table")
    fun readAllAlbumImage(): List<AlbumImageModel>

    @Query("SELECT * FROM album_image_table WHERE albumId=:albumId")
    fun readAlbumImage(albumId: String): List<AlbumImageModel>

    @Query("SELECT * FROM label_images_table WHERE imageLabel LIKE :search || '%'")
    fun findLabelImages(search: String): List<LabelImagesModel>

    @Query("SELECT * FROM label_images_table")
    fun findAllLabels(): List<LabelImagesModel>

    @Query("SELECT * FROM images_table WHERE imageId=:imgId")
    fun getImageUsingId(imgId: String): ImageModel

    @Query("SELECT * FROM images_table ORDER BY imageId ASC LIMIT 1")
    fun getLastImage(): ImageModel

    @Query("UPDATE album_table SET albumName = :newName where albumId = :albumId")
    fun renameAlbum(newName:String, albumId:String)

}