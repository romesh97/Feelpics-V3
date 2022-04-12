package io.millionic.feelpics.viewmodels

import android.R
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import io.millionic.feelpics.constants.LabelsConst
import io.millionic.feelpics.data.ImagesDatabase
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.models.LabelImagesModel
import io.millionic.feelpics.repository.ImageRepository
import io.millionic.feelpics.ui.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.collections.ArrayList
import android.location.Geocoder
import android.media.ExifInterface
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import io.millionic.feelpics.exif.extension.getTags
import io.millionic.feelpics.exif.util.Constants
import java.util.*





class MainViewModel(val activity: Activity): AndroidViewModel(activity.application) {

    private val repository: ImageRepository

    lateinit var inputImage: InputImage
    var imageLabeler: ImageLabeler

    var labelPercentage = MutableLiveData<Int>()
    var labelListSize = MutableLiveData<Int>()


    var label2Percentage = MutableLiveData<Int>()
    var label2ListSize = MutableLiveData<Int>()

    private val imgd = MutableLiveData<ArrayList<ImageModel>>()
    private val albums : LiveData<List<AlbumModel>>


    init {
        val imageDao = ImagesDatabase.getDatabase(activity.application).userDao()
        repository = ImageRepository(imageDao)
        albums = repository.readAllAlbum
        imgd.value = ArrayList()
        imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }


    val imgCols = listOf<String>(MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Media.DATE_ADDED).toTypedArray()
    val imgCursor = activity.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imgCols, null, null, null)!!
    var imgLength = imgCursor.count-1;

    var isFirstTimeData = false


    fun setUpAllGalleyImage(isFirstTime:Boolean){
            isFirstTimeData = isFirstTime
            labelListSize.value = imgLength
            labelPercentage.value = 0
            setFiles(1)
    }

    fun setFiles(postition:Int){
        var pos = postition
        if (imgLength > postition) {
            imgCursor.moveToPosition((imgLength - postition))
            val imgPath = imgCursor.getString(imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            var attr: BasicFileAttributes? = null
            attr = Files.readAttributes(Paths.get(imgPath), BasicFileAttributes::class.java)
            val dateBase = attr.creationTime().toString()
            val id = attr.fileKey().toString()
            val dateArr = dateBase.split("T")
            try {
                imgd.value!!.add(ImageModel(imgPath, id, dateArr[0],"noNeed"))
            } catch (e: Exception) {
            }
            attr = null
            pos++
            viewModelScope.launch {
                delay(1)
                setFiles(pos)
            }
        }


        if (imgLength == pos) {
            pos++
//            Toast.makeText(activity, "Image scanning finished & Labeling started!", Toast.LENGTH_SHORT).show()
            onSNACK(activity.window.decorView)
            val hm = activity as HomeActivity
            if (isFirstTimeData){
                setUpListLabeling(0)
                hm.setUpProgressBarFirstTime(this)
            }else{
                var isAdded = false
                readAllImages().observe(activity,{
                    if (!isAdded) {
                        dbImages.addAll(it)
                        isAdded = true
                    }
                })
                Handler().postDelayed({
                    setNeedToLabel()
                    setUpListLabelingOtherTimes(0)
                    label2Percentage.value = 0
                    hm.setUpProgressBarSecondTime(this)
                },500)

            }
        }
    }



    fun getImgData(): LiveData<ArrayList<ImageModel>>{
        return imgd
    }

    fun getAllAlbum(): LiveData<List<AlbumModel>>{
        return albums
    }

    fun setUpListLabeling(currPosition: Int) {
        var currPo = currPosition
        if (imgd.value != null) {
                if (imgd.value!!.size > currPosition){
                viewModelScope.launch(Dispatchers.IO){
                    val image = imgd.value!![currPosition]
                    addImage(image)
                    var file: File?=null
                     file = File(image.imgPath)
                    inputImage = InputImage.fromFilePath(activity, Uri.fromFile(file))
//                    file = null
                    activity.runOnUiThread(Runnable {
                        try {
                            checkGps(image);
                            val addOnFailureListener = imageLabeler.process(inputImage)
                                .addOnSuccessListener {
                                    var result = ""
                                    for (label in it) {
                                        if (label.confidence >= 0.7) {
                                            val customAllLabel = LabelsConst.customAllLabels
                                            for (cLabel in customAllLabel) {
                                                if (cLabel.mainCategory == label.text) {
                                                    val imgLab =
                                                        LabelImagesModel(
                                                            0,
                                                            false,
                                                            image.imageId,
                                                            label.text
                                                        )
                                                    addLabel(imgLab)
                                                    break
                                                } else {
                                                    if (cLabel.important.contains(label.text)) {
                                                        val imgLabMain =
                                                            LabelImagesModel(
                                                                0,
                                                                false,
                                                                image.imageId,
                                                                cLabel.mainCategory
                                                            )
                                                        addLabel(imgLabMain)
                                                        val imgLabSub =
                                                            LabelImagesModel(
                                                                0,
                                                                false,
                                                                image.imageId,
                                                                label.text
                                                            )
                                                        addLabel(imgLabSub)
                                                        break
                                                    } else if (cLabel.nonImportant.contains(label.text)) {
                                                        val imgLabMain =
                                                            LabelImagesModel(
                                                                0,
                                                                false,
                                                                image.imageId,
                                                                cLabel.mainCategory
                                                            )
                                                        addLabel(imgLabMain)
                                                        break
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    labelPercentage.value = (labelPercentage.value!! + 1)
                                    currPo++
                                    setUpListLabeling(currPo)

                                }.addOnFailureListener {
                                    labelPercentage.value = (labelPercentage.value!! + 1)
                                    currPo++
                                    setUpListLabeling(currPo)
                                }
                        }catch (e:Exception){
                            labelPercentage.value = (labelPercentage.value!! +1)
                            currPo++
                            setUpListLabeling(currPo)
                        }
                    })

                }
            }
        }
    }

    var needToLabelImages = ArrayList<ImageModel>()
    var dbImages = ArrayList<ImageModel>()

    fun setNeedToLabel() {
        needToLabelImages.addAll(imgd.value!!)
        for (img in dbImages) {
            val imgs = needToLabelImages.filter { (it.imageId == img.imageId) || it.imgPath == img.imgPath }
            needToLabelImages.removeAll(imgs)
            if (imgs.size <= 0) {
                deleteImage(img)
                deleteLabel(img.imageId)
            }
        }
        label2ListSize.value = needToLabelImages.size

    }


    fun setUpListLabelingOtherTimes(currPosition: Int) {
        var currPo = currPosition
        var isFirstFav = true
        if (needToLabelImages.size > currPosition){
            viewModelScope.launch(Dispatchers.IO){
                val image = needToLabelImages[currPosition]
                addImage(image)

                var file: File?=null
                file = File(image.imgPath)

                inputImage = InputImage.fromFilePath(activity, Uri.fromFile(file))
                file = null
                activity.runOnUiThread(Runnable {
                    try {
                        checkGps(image)
                        imageLabeler.process(inputImage)
                            .addOnSuccessListener {
                                for (label in it) {
                                    if (label.confidence >= 0.7) {
                                        val customAllLabel = LabelsConst.customAllLabels
                                        for (cLabel in customAllLabel) {
                                            if (cLabel.mainCategory == label.text) {
                                                val imgLab =
                                                    LabelImagesModel(0,
                                                        false, image.imageId, label.text)
                                                addLabel(imgLab)
                                                break
                                            } else {
                                                if (cLabel.important.contains(label.text)) {
                                                    val imgLabMain =
                                                        LabelImagesModel(
                                                            0,
                                                            false,
                                                            image.imageId,
                                                            cLabel.mainCategory
                                                        )
                                                    addLabel(imgLabMain)
                                                    val imgLabSub =
                                                        LabelImagesModel(
                                                            0,
                                                            false,
                                                            image.imageId,
                                                            label.text
                                                        )
                                                    addLabel(imgLabSub)
                                                    break
                                                } else if (cLabel.nonImportant.contains(label.text)) {
                                                    val imgLabMain =
                                                        LabelImagesModel(
                                                            0,
                                                            false,
                                                            image.imageId,
                                                            cLabel.mainCategory
                                                        )
                                                    addLabel(imgLabMain)
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                                label2Percentage.value = (label2Percentage.value!! +1)
                                currPo++
                                setUpListLabelingOtherTimes(currPo)
                            }.addOnFailureListener{
                                label2Percentage.value = (label2Percentage.value!! +1)
                                currPo++
                                setUpListLabelingOtherTimes(currPo)
                            }
                    }catch (e:Exception){
                        label2Percentage.value = (label2Percentage.value!! +1)
                        currPo++
                        setUpListLabelingOtherTimes(currPo)

                    }
                })
            }
        }
    }


    private fun checkGps(image:ImageModel) {
        val exifInterface = ExifInterface(image.imgPath)
        val map = exifInterface.getTags()
        val latitude = map[Constants.EXIF_LATITUDE]?.toDouble()
        val longitude = map[Constants.EXIF_LONGITUDE]?.toDouble()
        if (latitude != null && longitude != null){
            if (latitude != 0.0 && longitude != 0.0){
                getCompleteAddressString(latitude, longitude, image)
            }
        }

    }


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double, image: ImageModel){
        val geocoder = Geocoder(activity, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val city = returnedAddress.subAdminArea.toString()
                val country = returnedAddress.countryName.toString()
                val imgCity = LabelImagesModel(0, true, image.imageId, city)
                val imgCountry = LabelImagesModel(0, true, image.imageId, country)
                addLabel(imgCity)
                addLabel(imgCountry)
                Log.d("checkGps", "city : $city")
                Log.d("checkGps", "country : $country")
            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doUserProb(bitmap: Bitmap, image: ImageModel) {
        try {
            Log.d("smilingProb", "doUserProb begin")
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val currBitmap = BitmapFactory.decodeFile(image.imgPath)
            var isFirstFav = true
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
            val detector = FaceDetection.getClient(options)
            detector.process(inputImage)
                .addOnSuccessListener {faces ->
                    for (face in faces){
                        val smileProb = face.smilingProbability
                        if (smileProb != null) {
                            Log.d("smilingProb", "$smileProb")
                            if (smileProb >= 0.75f && currBitmap.height > currBitmap.width) {
                                if (isFirstFav) {
                                    isFirstFav = false
                                    val albumId = "default_favorite_001"
                                    val startDate = "0000/00/00"
                                    val endDate = "3000/01/01"
                                    val maxQuantity = "1000000"
                                    val albumName = "Favourites"
                                    val album = AlbumModel(
                                        albumId,
                                        albumName,
                                        startDate,
                                        endDate,
                                        maxQuantity,
                                        "",
                                        "happy",
                                        image.imgPath
                                    )
                                    addAlbum(album)
                                    val albumImage = AlbumImageModel(
                                        0,
                                        "default_favorite_001",
                                        image.imgPath
                                    )
                                    addAlbumImage(albumImage)
                                }
                            }
                        }
                    }
                }.addOnFailureListener{
                    Log.d("smilingProb", "doUserProb failed")
                }
        }catch (e:Exception){
            Log.d("smilingProb","${e.message}")
        }
    }

    fun onSNACK(view: View){
        val snackBar =
            Snackbar.make(view, "Image labeling process started, Please Hang On....", Snackbar.LENGTH_LONG)
        snackBar.setActionTextColor(Color.BLUE)
        snackBar.duration = 15000
        snackBar.setAction("Cancel") { // Call your action method here
            snackBar.dismiss()
        }
        snackBar.show()

    }

    fun readAlbumImage(albumId:String ): List<AlbumImageModel> {
        return repository.readAlbumImage(albumId)
    }

    fun readAllAlbumImage(): List<AlbumImageModel> {
        return repository.readAllAlbumImage()
    }


    fun getPercentage(): LiveData<Int>{
        return labelPercentage
    }

    fun getListSize(): LiveData<Int>{
        return labelListSize
    }
    fun getPercentage2(): LiveData<Int>{
        return label2Percentage
    }

    fun getListSize2(): LiveData<Int>{
        return label2ListSize
    }

    fun addImage(imageModel: ImageModel){
        viewModelScope.launch(Dispatchers.IO){
            repository.addImage(imageModel)
        }
    }

    fun addLabel(labelImagesModel: LabelImagesModel){
        viewModelScope.launch(Dispatchers.IO){
            repository.addLabel(labelImagesModel)
        }
    }

    fun addAlbum(albumData: AlbumModel){
        viewModelScope.launch {
            repository.addAlbum(albumData)
        }
    }

    fun addAlbumImage(albumImageModel: AlbumImageModel){
        viewModelScope.launch {
            repository.addAlbumImage(albumImageModel)
        }
    }

    fun searchImage(search:String): List<LabelImagesModel> {
        val labels = repository.findLabelImage(search)
        return labels
    }

    fun getAllLabels(): List<LabelImagesModel>{
        var labels = repository.findAllLabels()
        return labels
    }

    fun getLastImage(): ImageModel{
        val image = repository.getLastImage()
        return image

    }

    fun readAllImages():LiveData<List<ImageModel>>{
        return repository.readAllImages
    }

    fun deleteImage(imgModel: ImageModel){
        viewModelScope.launch {
            repository.deleteImage(imgModel)
        }
    }

    fun deleteLabel(imgId: String){
        viewModelScope.launch {
            repository.deleteLabel(imgId)
        }
    }

    fun deleteAlbum(album: AlbumModel){
        viewModelScope.launch {
            repository.deleteAlbum(album)
            deleteAlbumImages(album.albumId)
        }
    }

    fun deleteAlbumImage(albumImageModel: AlbumImageModel){
        viewModelScope.launch {
            repository.deleteAlbumImage(albumImageModel)
        }
    }
    fun deleteAlbumImages(albumId: String){
        viewModelScope.launch {
            repository.deleteAlbumImages(albumId)
        }
    }

    fun renameAlbum(newName: String, albumId: String){
        viewModelScope.launch {
            repository.renameAlbum(newName, albumId)
        }
    }

    var imageViewerStatus = false
    fun setImageViewStatus(){
        imageViewerStatus = true
    }
    var imageViewCurrIndex = 0


}