package io.millionic.feelpics.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.models.PhoneAlbumModel
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.adapters.AlbumRvAdapter
import io.millionic.feelpics.ui.adapters.ChipsetAdapter
import io.millionic.feelpics.ui.adapters.CustomAlbumRvAdapter
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_album.*
import kotlinx.android.synthetic.main.album_name_layout.*
import kotlinx.android.synthetic.main.custom_album_alert.*
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AlbumActivity : AppCompatActivity() {

    private val albumScope = CoroutineScope(Dispatchers.IO + CoroutineName("AlbumScope"))
    private val albumImageScope = CoroutineScope(Dispatchers.IO + CoroutineName("AlbumImageScope"))

    var labelCurrList = ArrayList<String>()
    var chipsetLabelList = ArrayList<String>()

    var imgList = ArrayList<ImageModel>()
    var albumsData = ArrayList<AlbumModel>()
    val phoneAlbumList = ArrayList<PhoneAlbumModel>()

    val searchArray = ArrayList<String>()

    lateinit var viewModel: MainViewModel

    lateinit var app: MyAppInstance

    var searchActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        supportActionBar?.hide()

        app = applicationContext.getApplicationContext() as MyAppInstance
        imgList.clear()
        imgList = app.getImages()


        val viewModelFactory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        yourAlbumRv.layoutManager = GridLayoutManager(this, 3)

        viewModel.getAllAlbum().observe(this, {
            albumsData.clear()
            albumsData.addAll(it)
            Handler().postDelayed({
                val customAlbumRvAdapter = CustomAlbumRvAdapter(this, albumsData, this)
                yourAlbumRv.adapter = customAlbumRvAdapter
                customAlbumRvAdapter.notifyDataSetChanged()

                setUpSearchList()
                val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item,searchArray)
                albumSearchDDEt.setAdapter(arrayAdapter)
                setUpSearch()

            },500)

        })


        val allLabels: ArrayList<String> = ArrayList()

        albumScope.launch {
            var allLabel = viewModel.getAllLabels()
            for ( label in allLabel){
                var isAvailable = false
                for (arrLabel in allLabels){
                    if (label.imageLabel.equals(arrLabel)){
                        isAvailable = true
                        break
                    }
                }
                if (!isAvailable && !label.isLocation){
                    allLabels.add(label.imageLabel)
                }
            }
        }

        Handler().postDelayed({
            labelCurrList.addAll(allLabels.filterNotNull())
        },500)

        yourAlbumAddIv.setOnClickListener {
            setUpAddAlbum(allLabels)
        }


        //Below the code for phone album part

        for (img in imgList){
            val location = img.imgPath.split("/")
            val type = location[location.size-2]

            if (phoneAlbumList.size == 0){
                val firstAlbData = ArrayList<ImageModel>()
                firstAlbData.add(img)
                phoneAlbumList.add(PhoneAlbumModel(type, firstAlbData))
            }else{
                var isAdded = false
                for (pAT in phoneAlbumList){
                    if (pAT.type == type){
                        pAT.images.add(img)
                        isAdded = true
                        break
                    }
                }
                if (!isAdded){
                    val firstAlbData = ArrayList<ImageModel>()
                    firstAlbData.add(img)
                    phoneAlbumList.add(PhoneAlbumModel(type, firstAlbData))
                }
            }
        }

        val phoneRvAdapter = AlbumRvAdapter(this, phoneAlbumList, this)

        phoneAlbumRv.layoutManager = GridLayoutManager(this, 3)
        phoneAlbumRv.setHasFixedSize(true)
        phoneAlbumRv.adapter = phoneRvAdapter

    }

    private fun setUpAddAlbum(allLabels: ArrayList<String>) {

        if (getTourStatus()){
            setTourStatus()
            val tourIntent = Intent(this, CreateAlbumActivity::class.java)
            startActivity(tourIntent)

        }
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_album_alert,null))

        var addAlbumDialog =  builder.create()
        addAlbumDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        addAlbumDialog.setCancelable(false)
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        addAlbumDialog.show()

        addAlbumDialog.byLabelEt.setDropDownHeight(400)
        labelCurrList.sort()
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, labelCurrList)
        addAlbumDialog.byLabelEt.setAdapter(arrayAdapter)
        addAlbumDialog.byLabelEt.setOnClickListener {
            addAlbumDialog.byLabelEt.hideKeyboard()
        }

        val chipsetRv = addAlbumDialog.labelChipsetRv
        chipsetRv.layoutManager = GridLayoutManager(this, 2,GridLayoutManager.HORIZONTAL, false)

        addAlbumDialog.labelAddBtn.setOnClickListener {
            labelCurrList.sort()
            val label = addAlbumDialog.byLabelEt.text.toString()
            if (!label.isEmpty()){
                chipsetLabelList.add(label)
                labelCurrList.sort()
                var chipsetAdapter = ChipsetAdapter(this, addAlbumDialog,chipsetLabelList)
                chipsetRv.adapter = chipsetAdapter
                chipsetAdapter.notifyDataSetChanged()
                addAlbumDialog.byLabelEt.setText("")
                if (labelCurrList.contains(label)) {
                    labelCurrList.remove(label)
                }
                labelCurrList.sort()
                val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, labelCurrList)
                arrayAdapter.notifyDataSetChanged()
                addAlbumDialog.byLabelEt.setAdapter(arrayAdapter)

            }else{
                Toast.makeText(this, "Label not be empty!", Toast.LENGTH_SHORT).show()
            }
            labelCurrList.sort()
        }

        addAlbumDialog.startDateEt.setOnClickListener {
            pickDateTime(addAlbumDialog, true)
        }
        addAlbumDialog.endDateEt.setOnClickListener {
            pickDateTime(addAlbumDialog)
        }

        addAlbumDialog.crateAlbumCloseIv.setOnClickListener {
            chipsetLabelList = ArrayList<String>()
            labelCurrList.clear()
            labelCurrList.addAll(allLabels.filterNotNull())
            labelCurrList.sort()
            addAlbumDialog.dismiss()
        }
        addAlbumDialog.nextAlbumBtn.setOnClickListener {
            if(!setUpCreateAlbum(addAlbumDialog)) {
                var labelsString = ""
                for (ls in chipsetLabelList){
                    labelsString += ls
                    if (ls != chipsetLabelList[chipsetLabelList.size -1]) {
                        labelsString += ","
                    }
                }

                addAlbumDB(labelsString, addAlbumDialog, viewModel)

                chipsetLabelList = ArrayList<String>()
                labelCurrList.clear()
                labelCurrList.addAll(allLabels.filterNotNull())
                labelCurrList.sort()
                addAlbumDialog.dismiss()
            }

        }
    }

    private fun setTourStatus() {
        val sharedPref = getSharedPreferences("createAlbumTour", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("status",false)
        editor.apply()
    }

    private fun getTourStatus(): Boolean {
        val sharePref = this.getSharedPreferences("createAlbumTour", Context.MODE_PRIVATE)
        return sharePref.getBoolean("status", true)
    }

    private fun addAlbumDB(
        labelsString: String,
        addAlbumDialog: AlertDialog,
        viewModel: MainViewModel
    ) {
        val startDateEt = addAlbumDialog.startDateEt.text.toString()
        val endDateEt = addAlbumDialog.endDateEt.text.toString()
        val maxQtyEt = getMaxQty().toString()
        val albumId =  System.currentTimeMillis().toString()

        val album = AlbumModel(albumId,"", startDateEt, endDateEt, maxQtyEt, "", labelsString, "")
        showAlbumNameDialog(labelsString, album)
    }

    private fun showAlbumNameDialog(labelsString: String, album: AlbumModel) {
        val labels = labelsString.split(",")
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.album_name_layout,null))

        var addAlbumNameDialog =  builder.create()
        addAlbumNameDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        addAlbumNameDialog.setCancelable(false)
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        addAlbumNameDialog.show()
        addAlbumNameDialog.createAlbumBtn.setOnClickListener {
            val albumNameEt = addAlbumNameDialog.albumNameEt
            if (TextUtils.isEmpty(albumNameEt.text.toString())){
                albumNameEt.setError("Please enter album name")
            }else{
                album.albumName = albumNameEt.text.toString()
                addAlbumImages(labels, viewModel, album);
                addAlbumNameDialog.dismiss()
            }
        }
        addAlbumNameDialog.crateAlbumNameCloseIv.setOnClickListener {
            addAlbumNameDialog.dismiss()
        }


    }

    private fun addAlbumImages(labels: List<String>, viewModel: MainViewModel, album: AlbumModel) {
        var currQty  = 0
        for(label in labels){
            albumImageScope.launch {
                if (currQty <=( album.quantity.toInt()+1)) {
                    val labelImgs = viewModel.searchImage(label)
                    this@AlbumActivity.runOnUiThread(Runnable {
                        Handler().postDelayed({
                            for (labelImg in labelImgs) {
                                currQty++
                                if (currQty < (album.quantity.toInt()+1)) {
                                    val addImg = imageIdToImage(labelImg.imageId)
                                    val albumImage = AlbumImageModel(0, album.albumId, addImg.imgPath)

                                    val csdf = SimpleDateFormat("yyyy-MM-dd")
                                    val sdf = SimpleDateFormat("yyyy/MM/dd")
                                    val imgDate = csdf.parse(addImg.date)
                                    val startDate = sdf.parse(album.startDate)
                                    val endDate = sdf.parse(album.endDate)

                                    if((imgDate.after(startDate) && imgDate.before(endDate)) || imgDate.equals(startDate) || imgDate.equals(endDate)) {

                                    viewModel.addAlbumImage(albumImage)
                                    if (currQty == 1) {
                                        Handler().postDelayed({
                                            val newAlbum = AlbumModel(
                                                album.albumId,
                                                album.albumName,
                                                album.startDate,
                                                album.endDate,
                                                album.quantity,
                                                album.place,
                                                album.Labels,
                                                thumbnail = imageIdToImage(labelImg.imageId).imgPath
                                            )
                                            viewModel.addAlbum(newAlbum)
                                        },200)

                                    }
                                }else{
                                    break
                                }
                                }
                            }
                        },300)
                    })

                }

            }
        }
        this@AlbumActivity.runOnUiThread(Runnable {
            Handler().postDelayed({
                  if(currQty < 1){
                      viewModel.addAlbum(album)
                  }
            },1000)
        })


    }

    private fun setUpCreateAlbum(addAlbumDialog: AlertDialog): Boolean {
        val startDateEt = addAlbumDialog.startDateEt
        val endDateEt = addAlbumDialog.endDateEt

        if (TextUtils.isEmpty(startDateEt.text.toString())){
            startDateEt.setError("Please choose start date")
            return true
        }else if (TextUtils.isEmpty(endDateEt.text.toString())){
            endDateEt.setError("Please choose end date")
            return true
        }else if (chipsetLabelList.size <= 0){
            Toast.makeText(this, "Please choose or enter at least one label!", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    fun setUpDialogLabelRemove(addAlbumDialog: AlertDialog, rLabel: String) {
        if (!labelCurrList.contains(rLabel)) {
            labelCurrList.add(rLabel)
        }
        if (chipsetLabelList.contains(rLabel)) {
            chipsetLabelList.remove(rLabel)
        }
        labelCurrList.sort()
        var chipsetAdapter = ChipsetAdapter(this, addAlbumDialog,chipsetLabelList)
        addAlbumDialog.labelChipsetRv.adapter = chipsetAdapter
    }


    private fun pickDateTime(addAlbumDialog: AlertDialog, isStart: Boolean = false) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            var monthI = (month+1)
            var monthS = (monthI).toString()
            var dayS = day.toString()
            if (monthI < 10){
                 monthS = "0${monthI}"
            }
            if (day < 10){
                dayS = "0${day}"
            }
            val date = "${year}/${monthS}/${dayS}"
            if (isStart) {
                addAlbumDialog.startDateEt.setText(date)
            }else{
                addAlbumDialog.endDateEt.setText(date)
            }
        }, startYear, startMonth, startDay).show()
    }

    fun imageIdToImage(imageId:String): ImageModel{
        var image= ImageModel("","","","")
        for (img in imgList){
            if (img.imageId == imageId){
                image = img
                return img
                break
            }
        }
        return image
    }

    fun deleteAlbum(album: AlbumModel){
        viewModel.deleteAlbum(album)
    }

    fun renameAlbum(album: AlbumModel){
        viewModel.renameAlbum(album.albumName, album.albumId)
    }

    fun setUpSearchList(){
        albumsData
        imgList
//        Handler().postDelayed({
            for (phone in phoneAlbumList){
                searchArray.add(phone.type)
            }
            for (album in albumsData){
                searchArray.add(album.albumName)
            }
            searchArray.reverse()
//        },1500)

    }

    private fun setUpSearch() {
        var isEmpty = true
        var sData = ""

        albumSearchDDEt.addTextChangedListener {
            isEmpty = it.toString().isEmpty()
            sData = it.toString()

            if (isEmpty){
                phoneSearchSetupRv(phoneAlbumList)
                customSearchSetupRv(albumsData)
                searchActive = false
            }
        }

        albumSearchDDEt.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_SEARCH && !isEmpty ){
                    phoneSearchSetupRv(setUpSearchPhoneAlbum(sData))
                    customSearchSetupRv(setUpSearchYourAlbum(sData))
                    searchActive = true
                    albumSearchDDEt.hideKeyboard()
                }
                return false
            }

        })

        albumSearchTI.setEndIconOnClickListener{
            if (!isEmpty) {
                phoneSearchSetupRv(setUpSearchPhoneAlbum(sData))
                customSearchSetupRv(setUpSearchYourAlbum(sData))
                albumSearchDDEt.hideKeyboard()
                searchActive = true
            }
        }
    }

    private fun phoneSearchSetupRv(upSearchPhoneAlbum: ArrayList<PhoneAlbumModel>) {
        val phoneRvAdapter = AlbumRvAdapter(this, upSearchPhoneAlbum, this)
        phoneAlbumRv.adapter = phoneRvAdapter
    }

    private fun customSearchSetupRv(upSearchYourAlbum: ArrayList<AlbumModel>) {
        val customAlbumRvAdapter = CustomAlbumRvAdapter(this, upSearchYourAlbum, this)
        yourAlbumRv.adapter = customAlbumRvAdapter
    }

    private fun setUpSearchYourAlbum(sData: String): ArrayList<AlbumModel> {
        val yAlbum = ArrayList<AlbumModel>()
        for(album in albumsData){
            val sd = sData.toLowerCase()
            val s = album.albumName.toLowerCase()
            if (s.contains(sd)){
                yAlbum.add(album)
            }
        }
        return yAlbum
    }

    private fun setUpSearchPhoneAlbum(sData: String): ArrayList<PhoneAlbumModel> {
        val pAlbum = ArrayList<PhoneAlbumModel>()
        for (album in phoneAlbumList){
            val sd = sData.toLowerCase()
            val s = album.type.toLowerCase()
            if (s.contains(sd)){
                pAlbum.add(album)
            }
        }
        return pAlbum
    }


    override fun onBackPressed() {
        if (searchActive){
            phoneSearchSetupRv(phoneAlbumList)
            customSearchSetupRv(albumsData)
            albumSearchDDEt.setText("")
        }else{
            finish()
        }
    }

    private fun getMaxQty(): Int{
        val sharePref = this.getSharedPreferences("maxQty", Context.MODE_PRIVATE)
        return sharePref.getInt("qty", 100)
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}