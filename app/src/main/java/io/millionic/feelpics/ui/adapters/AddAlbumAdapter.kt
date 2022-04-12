package io.millionic.feelpics.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.grid_image_item.view.*
import java.io.File

class AddAlbumAdapterGalleryRvAdapter(private var context: Context, private var imageList: ArrayList<ImageModel>, private val albumId:String,private val vm: MainViewModel, private val showMenuDelete:(Boolean)->Unit): RecyclerView.Adapter<AddAlbumAdapterGalleryRvAdapter.ImageViewHolder>() {

    private var isEnable = false
    private val itemSelectedList = mutableListOf<AlbumImageModel>()

    val imgCustomList = setUpAMtoCm()

    class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
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
            .load(File(imageList[position].imgPath))
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
                selectItem(holder, imgCustomList[position], imgCustomList[position].albumImage)
                holder.image.alpha = 0.3f
            }
        }

        holder.image.setOnLongClickListener {
            selectItem(holder, imgCustomList[position], imgCustomList[position].albumImage)
            holder.image.alpha = 0.3f
            true
        }


    }

    override fun getItemCount(): Int {
        return imageList.size
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

    private fun setUpAMtoCm(): ArrayList<CustomDataModel> {
        var arr = ArrayList<CustomDataModel>()
        for (image in imageList){
            val iv = AlbumImageModel(0, albumId, image.imgPath)
            arr.add(CustomDataModel(iv, false))
        }
        return arr
    }

    fun addAlbumImage(){
        if (itemSelectedList.isNotEmpty()) {

            for (item in imgCustomList){
                if (item.selected){
                    vm.addAlbumImage(item.albumImage)
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

data class CustomAlbumImageModel(
    var albumImage: AlbumImageModel,
    var selected: Boolean
)