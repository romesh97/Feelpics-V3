package io.millionic.feelpics.others

import android.app.Application
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.ImageModel

class MyAppInstance : Application() {

    private var imageDatas = ArrayList<ImageModel>()
    private var imageHomeDatas = ArrayList<ImageModel>()

    fun setImagesData(images : ArrayList<ImageModel>){
        imageDatas = images
    }
    fun setHomeImagesData(images : ArrayList<ImageModel>){
        imageHomeDatas = images
    }

    fun getImages():ArrayList<ImageModel>{
        return imageDatas
    }

    fun getHomeImages():ArrayList<ImageModel>{
        return imageHomeDatas
    }

    private var currPhoneAlbum = ArrayList<ImageModel>()

    fun setCurrPhoneAlbum(images : ArrayList<ImageModel>){
        currPhoneAlbum = images
    }

    fun getCurrPhoneAlbum():ArrayList<ImageModel>{
        return currPhoneAlbum
    }

    private var currCustomAlbum = ArrayList<AlbumImageModel>()

    fun setCurrCustomAlbum(images : ArrayList<AlbumImageModel>){
        currCustomAlbum = images
    }

    fun getCurrCustomAlbum():ArrayList<AlbumImageModel>{
        return currCustomAlbum
    }




}