package io.millionic.feelpics.ui

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import io.millionic.feelpics.R
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.PhoneAlbumRvAdapter
import kotlinx.android.synthetic.main.activity_phone_album_images.*

class PhoneAlbumImagesActivity : AppCompatActivity() {

    private lateinit var rvAdapter: PhoneAlbumRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_album_images)

        val type = this.intent.extras?.getString("type")!!

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setTitle(type)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val app = applicationContext.getApplicationContext() as MyAppInstance
        val imgList = app.getCurrPhoneAlbum()

        rvAdapter = PhoneAlbumRvAdapter(this, imgList)

        phoneAlbumImagesRv.layoutManager = GridLayoutManager(this, 4)
        phoneAlbumImagesRv.adapter = rvAdapter



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}