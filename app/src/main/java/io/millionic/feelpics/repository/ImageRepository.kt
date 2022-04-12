package io.millionic.feelpics.repository

import androidx.lifecycle.LiveData
import io.millionic.feelpics.data.ImageDao
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.models.LabelImagesModel

class ImageRepository(private val imagesDao: ImageDao) {

    val readAllImages: LiveData<List<ImageModel>> = imagesDao.readAllImages()

    val readAllAlbum: LiveData<List<AlbumModel>> = imagesDao.readAllAlbum()

    fun readAlbumImage(albumId: String): List<AlbumImageModel>{
        return imagesDao.readAlbumImage(albumId)
    }

    fun readAllAlbumImage(): List<AlbumImageModel>{
        return imagesDao.readAllAlbumImage()
    }

    fun findLabelImage(search: String): List<LabelImagesModel> {
        return imagesDao.findLabelImages(search)
    }
    fun findAllLabels(): List<LabelImagesModel>{
        return imagesDao.findAllLabels()
    }
    fun getImagesUsingId(imgId: String): ImageModel{
        return imagesDao.getImageUsingId(imgId)
    }
    fun getLastImage(): ImageModel{
        return imagesDao.getLastImage()
    }

    fun renameAlbum(newName:String, albumId: String){
        imagesDao.renameAlbum(newName, albumId)
    }

    suspend fun addImage(imageModel: ImageModel){
        imagesDao.addImage(imageModel)
    }

    suspend fun addLabel(labelImagesModel: LabelImagesModel){
        imagesDao.addLabelImage(labelImagesModel)
    }

    fun addAlbum(albumModel: AlbumModel){
        imagesDao.addAlbum(albumModel)
    }

    fun addAlbumImage(albumImageModel: AlbumImageModel){
        imagesDao.addAlbumImage(albumImageModel)
    }

    suspend fun deleteImage(imageModel: ImageModel){
        imagesDao.deleteImage(imageModel)
    }

    fun deleteAlbumImage(albumImageModel: AlbumImageModel){
        imagesDao.deleteAlbumImage(albumImageModel)
    }
    fun deleteAlbumImages(albumId: String){
        imagesDao.deleteAlbumImages(albumId)
    }

    suspend fun deleteLabel(imageId: String){
        imagesDao.deleteLabel(imageId)
    }

    fun deleteAlbum(album: AlbumModel){
        imagesDao.deleteAlbum(album)
    }



}