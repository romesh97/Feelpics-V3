package io.millionic.feelpics.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.AudioManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.AlbumImageViewPagerAdapter
import io.millionic.feelpics.utils.MySharedPref
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_album_image_veiwer.*
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

class AlbumImageVeiwerActivity : AppCompatActivity() {

    lateinit var imgRvAdapter: AlbumImageViewPagerAdapter
    private var mainMenu: Menu? = null
    lateinit var imgList:ArrayList<AlbumImageModel>
    lateinit var currentImageModel:ImageModel;
    var currCamPosition = 0

    lateinit var amanager: AudioManager

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album_image_veiwer)

        val viewModelFactory = MainViewModelFactory(this)
        var viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        amanager = getSystemService(AUDIO_SERVICE) as AudioManager
        GlobalScope.launch {
            muteMicEverySecond()
        }


        val currPosition = MySharedPref.getImageViewIndex(this)

        val app = applicationContext.getApplicationContext() as MyAppInstance
        imgList = app.getCurrCustomAlbum()

        val file = Files.readAttributes(Paths.get(imgList[currCamPosition].imagePath), BasicFileAttributes::class.java)
        val dateBase = file.creationTime().toString()
        val dateArr = dateBase.split("T")
        val id = file.fileKey().toString()
        currentImageModel = ImageModel(imgList[currCamPosition].imagePath, id, dateArr[0], "noNeed")

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imgRvAdapter = AlbumImageViewPagerAdapter(this)
        imgRvAdapter.setContentList(imgList)


        albumImageVp.adapter = imgRvAdapter
        albumImageVp.setCurrentItem(currPosition, true)

        camera_view_custom.start()
        camera_view_custom.facing = 1
        camera_view_custom.playSoundEffect(R.raw.no_sound)
        camera_view_custom.cameraProperties

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
        albumImageVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currCamPosition = position
                MySharedPref.setImageViewIndex(this@AlbumImageVeiwerActivity, currCamPosition)
                val file = Files.readAttributes(Paths.get(imgList[currCamPosition].imagePath), BasicFileAttributes::class.java)
                val dateBase = file.creationTime().toString()
                val dateArr = dateBase.split("T")
                val id = file.fileKey().toString()
                currentImageModel = ImageModel(imgList[currCamPosition].imagePath, id, dateArr[0], "noNeed")

            }
        })
    }

    suspend fun setUpCamera(viewModel: MainViewModel){
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
        try {
            delay(800)
            val currBitmap = BitmapFactory.decodeFile(imgList[currCamPosition].imagePath)

            camera_view_custom.isSoundEffectsEnabled = false

            if (currBitmap.height > currBitmap.width){
                camera_view_custom.captureImage {cameraKitImage->
                    Log.d("smilingProb", "captured")
                    if (cameraKitImage!=null){
                        var bitmap = cameraKitImage.bitmap
                        bitmap = Bitmap.createScaledBitmap(bitmap, camera_view_custom.width, camera_view_custom.height, false)

                        viewModel.doUserProb(bitmap, currentImageModel)
                    }
                    GlobalScope.launch {
                        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
                        setUpCamera(viewModel)
                    }
                }
            }else{
                amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
                setUpCamera(viewModel)
            }

        }catch (e: Exception){
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)
            setUpCamera(viewModel)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }else if(item.itemId == R.id.image_share){
            val imgPath = imgList[albumImageVp.currentItem].imagePath
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            share.putExtra(Intent.EXTRA_STREAM,Uri.parse(imgPath) ) // or Uri.fromFile(file)
            share.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            share.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            startActivity(Intent.createChooser(share, "Share Image"))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.view_pager_menu, mainMenu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false)

    }
}