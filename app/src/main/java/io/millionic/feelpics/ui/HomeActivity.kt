package io.millionic.feelpics.ui

import android.content.Context
import android.content.Intent
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.millionic.feelpics.R
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.ui.adapters.GalleryRvAdapter
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import io.millionic.feelpics.others.MyAppInstance


class HomeActivity : AppCompatActivity() {

    private val scope1 = CoroutineScope(Dispatchers.IO + CoroutineName("lastImg"))
    private val searchImageScope = CoroutineScope(Dispatchers.IO + CoroutineName("SearchImageScope"))

    var searchActive = false

    var totalImgSize = 1
    var sizeLabel = 0

    var imageList: ArrayList<ImageModel> = ArrayList()
    private lateinit var rvAdapter: GalleryRvAdapter

    var imgP:String?=null
    lateinit var app: MyAppInstance


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)


        supportActionBar?.hide()

        app = applicationContext.getApplicationContext() as MyAppInstance


        val viewModelFactory = MainViewModelFactory(this)
        var viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        val isFTime = isFirstTime()
        viewModel.setUpAllGalleyImage(isFTime)
        setIsFirstTime()


        homeGalleryImagesRv.layoutManager = GridLayoutManager(this, 4)
        homeGalleryImagesRv.setHasFixedSize(true)

        scope1.launch {
            try {
                val img = viewModel.getLastImage()
                imgP = img.imgPath
            }catch (e: Exception){
                Log.d("startError",e.message.toString())
            }
            
        }

        viewModel.getImgData().observe(this, {
            try {
                imageList = it
                setUpRecyclerViewAdapter(imageList)
                app.setImagesData(it)
                app.setCurrPhoneAlbum(imageList)
            }catch (e: Exception){
            }

        })


        Handler().postDelayed({
            setUpSearch(viewModel)
        },10000)
        setUpSearch(viewModel)

        albumFloatingBtn.setOnClickListener {
            val intent = Intent(this, AlbumActivity::class.java)
            startActivity(intent )
        }

        settingsIv.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        homeGallerySearchDDEt.setDropDownHeight(1000)

    }

    private fun setUpRecyclerViewAdapter(imageList: ArrayList<ImageModel>) {
        rvAdapter = GalleryRvAdapter(this, imageList)
        app.setCurrPhoneAlbum(imageList)
        homeGalleryImagesRv.adapter = rvAdapter
        rvAdapter.notifyDataSetChanged()
    }

    private fun setUpSearch(viewModel: MainViewModel) {
        val imageLabels = ArrayList<String>()

        searchImageScope.launch {
            var allLabel = viewModel.getAllLabels()
            for ( label in allLabel){
                var isAvailable = false
                for (arrLabel in imageLabels){
                    if (label.imageLabel.equals(arrLabel)){
                        isAvailable = true
                        break
                    }
                }
                if (!isAvailable){
                    imageLabels.add(label.imageLabel)
                }
            }
            imageLabels.sort()
        }

        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item,imageLabels)
        homeGallerySearchDDEt.setAdapter(arrayAdapter)

        var isEmpty = true

        homeGallerySearchDDEt.addTextChangedListener {
            if (it.toString().isEmpty()){
                setUpRecyclerViewAdapter(imageList)
                isEmpty = true
                searchActive = false
            }else{
                Log.d("searchOnBording", it.toString())
                isEmpty = false
            }
        }

        homeGallerySearchDDEt.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_SEARCH && !isEmpty ){
                    setUpRecyclerBySearch(viewModel)
                    searchActive = true
                    homeGallerySearchDDEt.hideKeyboard()

                }
                return false
            }

        })

        homeGallerySearchTl.setEndIconOnClickListener{
            if (!isEmpty) {
                setUpRecyclerBySearch(viewModel)
                searchActive = true
                homeGallerySearchDDEt.hideKeyboard()
            }
        }

    }

    private fun setUpRecyclerBySearch(viewModel: MainViewModel) {
        val searchData = homeGallerySearchDDEt.text.toString()
        val searchImageIds:ArrayList<String> = ArrayList()
        searchImageScope.launch {
            val searchDbResult = viewModel.searchImage(searchData)
            for (data in searchDbResult){
                searchImageIds.add(data.imageId)
            }
        }
        var imageArray = ArrayList<ImageModel>()

        Handler().postDelayed({
            for (img in imageList){
                for (sImg in searchImageIds){
                    if (img.imageId.equals(sImg)){
                        Log.d("HomeImageIds",searchImageIds.size.toString())
                        imageArray.add(img)
                        break
                    }
                }
            }
            setUpRecyclerViewAdapter(imageArray)
        },200)

    }

    private fun isFirstTime(): Boolean{
        val sharedPref = this.getSharedPreferences("checkingLabel", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("isFirstTime", true)
    }

    private fun setIsFirstTime(){
        val sharedPref = this.getSharedPreferences("checkingLabel", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("isFirstTime",false)
        editor.apply()
    }

    fun setUpProgressBarFirstTime(viewModel: MainViewModel){
        viewModel.getListSize().observe(this, {
            totalImgSize = it
            viewModel.getPercentage().observe(this, {
                val currVal = it + 1
                val pro = (currVal*100/totalImgSize)
                labelingProgressBar.max = 100
                labelingProgressBar.setProgress(pro)
                if (pro >= 100){
                    labelingProgressBar.visibility = View.GONE
                }
            })
        })
    }
    fun setUpProgressBarSecondTime(viewModel: MainViewModel){
        labelingProgressBar.max = 100
        viewModel.getListSize2().observe(this, {
            totalImgSize = it
//            Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
        })
        viewModel.getPercentage2().observe(this, {
            val currVal = it + 1
            if(totalImgSize != 0) {
                val pro = (currVal * 100 / totalImgSize)
                labelingProgressBar.setProgress(pro)
                if (pro >= 100) {
                    labelingProgressBar.visibility = View.GONE
                }
            }else{
                labelingProgressBar.progress = 100
                labelingProgressBar.visibility = View.GONE
            }
        })
    }

    override fun onBackPressed() {
        if (searchActive){
            setUpRecyclerViewAdapter(imageList)
            homeGallerySearchDDEt.setText("")
        }else{
            finish()
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}