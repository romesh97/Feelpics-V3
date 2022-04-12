package io.millionic.feelpics.ui


import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.millionic.feelpics.R
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.AddAlbumAdapterGalleryRvAdapter
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_add_image.*

class AddImageActivity : AppCompatActivity() {
    private var mainMenu: Menu? = null

    lateinit var albumId:String
    lateinit var albumName:String
    lateinit var adapter:AddAlbumAdapterGalleryRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_image)

        albumId = intent.getStringExtra("albumId")!!
        albumName = intent.getStringExtra("albumName")!!

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Images"



        val viewModelFactory = MainViewModelFactory(this)
        var viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        val app = this.applicationContext as MyAppInstance
        val imgList = app.getImages()

        adapter = AddAlbumAdapterGalleryRvAdapter(this, imgList,albumId, viewModel){show ->showDeleteMenu(show)}

        addImagesRv.layoutManager = GridLayoutManager(this, 3)
        addImagesRv.adapter = adapter




    }

    fun showDeleteMenu(show: Boolean){
        mainMenu?.findItem(R.id.mChecked)?.isVisible = show
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            val intent = Intent(this, CustomImageAlbumActivity::class.java)
            intent.putExtra("albumId",albumId)
            intent.putExtra("albumName",albumName)
            startActivity(intent)
            finish()
        }else if(item.itemId == R.id.mChecked){
            adapter.addAlbumImage()
            val intent = Intent(this, CustomImageAlbumActivity::class.java)
            intent.putExtra("albumId",albumId)
            intent.putExtra("albumName",albumName)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.add_image_menu, mainMenu)
        showDeleteMenu(false)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        val intent = Intent(this, CustomImageAlbumActivity::class.java)
        intent.putExtra("albumId",albumId)
        intent.putExtra("albumName",albumName)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}