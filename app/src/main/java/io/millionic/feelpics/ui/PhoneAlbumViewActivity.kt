package io.millionic.feelpics.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import io.millionic.feelpics.R
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.PhoneAlbumViewerAdapter
import io.millionic.feelpics.utils.MySharedPref
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.android.synthetic.main.activity_phone_album_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

class PhoneAlbumViewActivity : AppCompatActivity() {

    lateinit var imgRvAdapter: PhoneAlbumViewerAdapter
    private var mainMenu: Menu? = null
    lateinit var imgList:ArrayList<ImageModel>
    var currCamPosition = 0
    lateinit var amanager: AudioManager

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_album_view)


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
            imgList = app.getCurrPhoneAlbum()
        }catch (e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imgRvAdapter = PhoneAlbumViewerAdapter(this)
        imgRvAdapter.setContentList(imgList)

        phoneAlbumImageVp.adapter = imgRvAdapter
        phoneAlbumImageVp.setCurrentItem(currCamPosition, true)

        camera_view_album.start()
        camera_view_album.facing = 1
        camera_view_album.playSoundEffect(R.raw.no_sound)
        camera_view_album.cameraProperties
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
        phoneAlbumImageVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currCamPosition = position
                MySharedPref.setImageViewIndex(this@PhoneAlbumViewActivity, currCamPosition)
                super.onPageSelected(position)
            }
        })
    }


    suspend fun setUpCamera(viewModel: MainViewModel){
        try {
            delay(800)
            val currBitmap = BitmapFactory.decodeFile(imgList[currCamPosition].imgPath)
            camera_view_album.isSoundEffectsEnabled = false

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            var orientation = "landscape"
            try {
                val image: String = File(imgList[currCamPosition].imgPath).getAbsolutePath()
                BitmapFactory.decodeFile(image, options)
                val imageHeight = options.outHeight
                val imageWidth = options.outWidth
                if (imageHeight > imageWidth) {
                    orientation = "portrait"
                }
            } catch (e: Exception) {
                //Do nothing
            }

            val image1 = ImageView(this)
            image1.layoutParams =
                ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            image1.setImageBitmap(currBitmap)

            val i = image1.drawable

            Log.d("smilingProb", "h = ${i.intrinsicHeight} w =${i.intrinsicWidth} value = ${currBitmap.height/(currBitmap.width * 1.0)}")
            if (currBitmap.height > currBitmap.width){
                camera_view_album.captureImage {cameraKitImage->
                    Log.d("smilingProb", "captured")
                    if (cameraKitImage!=null){
                        var bitmap = cameraKitImage.bitmap
                        bitmap = Bitmap.createScaledBitmap(bitmap, camera_view_album.width, camera_view_album.height, false)
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
            val imgPath = imgList[phoneAlbumImageVp.currentItem].imgPath
            val share = Intent(Intent.ACTION_SEND)
            share.type = "image/jpeg"
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(imgPath) ) // or Uri.fromFile(file)
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