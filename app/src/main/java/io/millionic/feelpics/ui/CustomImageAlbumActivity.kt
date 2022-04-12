package io.millionic.feelpics.ui

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.ui.adapters.CustomAlbumImageRvAdapter
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_custom_image_album.*

class CustomImageAlbumActivity : AppCompatActivity() {

    private var mainMenu: Menu? = null
    lateinit var customAlbumImageRvAdapter: CustomAlbumImageRvAdapter

    lateinit var albumId:String
    lateinit var albumName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_image_album)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.primaryColor)))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        albumId = intent.getStringExtra("albumId")!!
        albumName = intent.getStringExtra("albumName")!!

        supportActionBar?.title = albumName

        val viewModelFactory = MainViewModelFactory(this)
        var viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        val allImage = viewModel.readAlbumImage(albumId)

        customAlbumRv.layoutManager = GridLayoutManager(this, 4)
        customAlbumImageRvAdapter = CustomAlbumImageRvAdapter(this, makeSingleImageList(allImage), viewModel){ show ->showDeleteMenu(show)}
        customAlbumRv.adapter = customAlbumImageRvAdapter




    }

    fun makeSingleImageList(imageList:List<AlbumImageModel>): ArrayList<AlbumImageModel>{
        val albumImageArr = ArrayList<AlbumImageModel>()
        if (imageList.size > 0) {
            albumImageArr.add(imageList[0])
        }
        for (albImg in imageList){
            var isAvailable = false
            for(albArr in albumImageArr){
                if (albArr.imagePath == albImg.imagePath){
                    isAvailable = true
                    break
                }
            }
            if (!isAvailable){
                albumImageArr.add(albImg)
            }
        }
        return albumImageArr

    }

    fun showDeleteMenu(show: Boolean){
        mainMenu?.findItem(R.id.mDelete)?.isVisible = show
        mainMenu?.findItem(R.id.mAdd)?.isVisible = !show
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }else if(item.itemId == R.id.mDelete){
            deleteImage()
        }else if(item.itemId == R.id.mAdd){
            val addIntent = Intent(this, AddImageActivity::class.java)
            addIntent.putExtra("albumName", albumName)
            addIntent.putExtra("albumId", albumId)
            startActivity(addIntent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteImage() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Do you want to delte the items?")
        alertDialog.setPositiveButton("Delete"){_,_ ->
            customAlbumImageRvAdapter.deleteSelectedItem()
            Toast.makeText(this, "Selected items are deleted successfully!", Toast.LENGTH_SHORT).show()
            showDeleteMenu(false)
        }
        alertDialog.setNegativeButton("Cancel"){_,_ ->
        }

        alertDialog.show()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.delete_menu, mainMenu)
        showDeleteMenu(false)
        return super.onCreateOptionsMenu(menu)
    }

}