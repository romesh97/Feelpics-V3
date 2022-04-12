package io.millionic.feelpics.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import io.millionic.feelpics.R
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.GalleryImageViewerAdapter
import kotlinx.android.synthetic.main.activity_image_viewer.*
import java.lang.Exception
import kotlin.collections.ArrayList
import androidx.viewpager2.widget.ViewPager2
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.AudioManager
import android.media.ExifInterface
import io.millionic.feelpics.exif.domain.ExifField
import io.millionic.feelpics.exif.domain.ExifTagsContainer
import io.millionic.feelpics.exif.domain.Type
import io.millionic.feelpics.exif.extension.getTags
import io.millionic.feelpics.exif.util.Constants
import io.millionic.feelpics.utils.MySharedPref
import java.io.File
import java.lang.StringBuilder
import java.nio.file.Files
import java.util.*

class ImageViewerActivity : AppCompatActivity() {

    lateinit var imgRvAdapter: GalleryImageViewerAdapter
    private var mainMenu: Menu? = null
    lateinit var imgList: ArrayList<ImageModel>
    var currCamPosition = 0
    lateinit var amanager: AudioManager

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val viewModelFactory = MainViewModelFactory(this)
        var viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        amanager = getSystemService(AUDIO_SERVICE) as AudioManager
        GlobalScope.launch {
            muteMicEverySecond()
        }

        val currPosition = MySharedPref.getImageViewIndex(this)
        currCamPosition = currPosition

        imgList = ArrayList<ImageModel>()

        try {
            val app = applicationContext.getApplicationContext() as MyAppInstance
            imgList = app.getHomeImages()
        }catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imgRvAdapter = GalleryImageViewerAdapter(this)
        imgRvAdapter.setContentList(imgList)

        galleryImageVp.adapter = imgRvAdapter
        galleryImageVp.setCurrentItem(currCamPosition, true)

        camera_view.start()
        camera_view.facing = 1
        camera_view.playSoundEffect(R.raw.no_sound)
        camera_view.cameraProperties
        GlobalScope.launch {
            setUpCamera(viewModel)
        }
        vpPositionChecker()

    }

    private suspend fun muteMicEverySecond() {
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
        delay(200)
        muteMicEverySecond()
    }


    fun vpPositionChecker(){
        galleryImageVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currCamPosition = position
                MySharedPref.setImageViewIndex(this@ImageViewerActivity, currCamPosition)
                super.onPageSelected(position)
            }
        })
    }

    suspend fun setUpCamera(viewModel: MainViewModel){
        try {
            delay(800)
            val currBitmap = BitmapFactory.decodeFile(imgList[currCamPosition].imgPath)
            camera_view.isSoundEffectsEnabled = false
            if (currBitmap.height > currBitmap.width){
                camera_view.captureImage {cameraKitImage->
                    Log.d("smilingProb", "captured")
                    if (cameraKitImage!=null){
                        var bitmap = cameraKitImage.bitmap
                        bitmap = Bitmap.createScaledBitmap(bitmap, camera_view.width, camera_view.height, false)
                        viewModel.doUserProb(bitmap, imgList[currCamPosition])
                    }
                    GlobalScope.launch {
                        setUpCamera(viewModel)
                    }
                }
            }else{
                setUpCamera(viewModel)
            }

        }catch (e:Exception){
            setUpCamera(viewModel)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }else if (item.itemId == R.id.image_share){
            val imgPath = imgList[galleryImageVp.currentItem].imgPath
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgPath) ) // or Uri.fromFile(file)
            share.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            startActivity(Intent.createChooser(share, "Share Image"))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.view_pager_menu, mainMenu)
        return super.onCreateOptionsMenu(menu)
    }
}