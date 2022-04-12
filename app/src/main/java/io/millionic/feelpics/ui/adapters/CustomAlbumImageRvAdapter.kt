package io.millionic.feelpics.ui.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.others.MyAppInstance
import io.millionic.feelpics.ui.AlbumImageVeiwerActivity
import io.millionic.feelpics.ui.ImageViewerActivity
import io.millionic.feelpics.utils.MySharedPref
import io.millionic.feelpics.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.grid_image_item.view.*
import java.io.File

class CustomAlbumImageRvAdapter(private var context: Context, private var imageList: ArrayList<AlbumImageModel>,private val vm: MainViewModel,private val showMenuDelete:(Boolean)->Unit): RecyclerView.Adapter<CustomAlbumImageRvAdapter.ImageViewHolder>() {

    private var isEnable = false
    private val itemSelectedList = mutableListOf<AlbumImageModel>()

    val imgCustomList = setUpAMtoCm()

    class ImageViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val image = view.gridItemIv
        val selectedIv = view.selectedIv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val myOptions = RequestOptions()
            .override(holder.image.width, holder.image.height)
            .centerCrop()

        Glide.with(context)
            .load(File(imgCustomList[position].albumImage.imagePath))
            .apply(myOptions)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.image)

        if(imgCustomList[position].selected){
            holder.image.alpha = 0.3f
            holder.selectedIv.visibility = View.VISIBLE
        }else{
            holder.image.alpha = 1f
            holder.selectedIv.visibility = View.GONE
        }

        holder.image.setOnClickListener {
            if (itemSelectedList.contains(imgCustomList[position].albumImage)) {
                itemSelectedList.remove(imgCustomList[position].albumImage)
                holder.selectedIv.visibility = View.GONE
                imgCustomList[position].selected = false
                if (itemSelectedList.isEmpty()){
                    showMenuDelete(false)
                    isEnable = false
                }
                holder.image.alpha = 1f
            }else if (isEnable){
                selectItem(holder, imgCustomList[position], imageList[position])
                holder.image.alpha = 0.3f
            }else{
                val intent = Intent(context, AlbumImageVeiwerActivity::class.java)
                val app = context.applicationContext as MyAppInstance
                app.setCurrCustomAlbum(imageList)
                intent.putExtra("currPosition", position)
                MySharedPref.setImageViewIndex(context, position)
                context.startActivity(intent)
            }
        }

        holder.image.setOnLongClickListener {
            selectItem(holder, imgCustomList[position], imageList[position])
            holder.image.alpha = 0.5f
            true
        }
    }

    private fun selectItem(
        holder: ImageViewHolder,
        albumImageModel: CustomDataModel,
        position: AlbumImageModel
    ) {
        isEnable = true
        itemSelectedList.add(position)
        albumImageModel.selected = true
        showMenuDelete(true)
        holder.selectedIv.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return imgCustomList.size
    }

    fun setUpAMtoCm(): ArrayList<CustomDataModel> {
        var arr = ArrayList<CustomDataModel>()
        for (image in imageList){
            arr.add(CustomDataModel(image, false))
        }
        return arr
    }

    fun deleteSelectedItem(){
        if (itemSelectedList.isNotEmpty()) {

            for (item in imgCustomList){
                if (item.selected){
                    vm.deleteAlbumImage(item.albumImage)
                }
            }
            imgCustomList.removeAll { item ->
                item.selected

            }
            isEnable = false
            itemSelectedList.clear()
        }
        notifyDataSetChanged()
    }

}


data class CustomDataModel(
    var albumImage: AlbumImageModel,
    var selected: Boolean
)