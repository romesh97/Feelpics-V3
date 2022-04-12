package io.millionic.feelpics.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.millionic.feelpics.R
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.ui.PhoneAlbumViewActivity
import kotlinx.android.synthetic.main.list_image.view.*

class PhoneAlbumViewerAdapter(private val context: PhoneAlbumViewActivity): RecyclerView.Adapter<PhoneAlbumViewerAdapter.MyViewHolder>(){

    lateinit var imgList: ArrayList<ImageModel>

    fun setContentList(imgList: ArrayList<ImageModel>){
        this.imgList = imgList
        notifyDataSetChanged()
    }

    class MyViewHolder(view: View) :RecyclerView.ViewHolder(view) {
        var image = view.listItemIv
        var positionTv = view.imagePositionTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_image, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context)
            .load(imgList[position].imgPath)
            .into(holder.image)
//        holder.image.setImage(ImageSource.uri(imgList[position].imgPath))
        Log.d("smilingProb", "h = ${holder.image.minimumHeight} w =${holder.image.minimumWidth}")
        holder.positionTv.text = "${position+1}/${imgList.size}"
        context.supportActionBar?.title = pathToTitle(imgList[position].imgPath)
    }

    fun pathToTitle(path: String):String{
        val splitedPath = path.split("/")
        val splitExtenstion = splitedPath[(splitedPath.size-1)].split(".")
        return splitExtenstion[(0)]
    }

    override fun getItemCount(): Int {
        return imgList.size
    }
}