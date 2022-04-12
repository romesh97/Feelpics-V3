package io.millionic.feelpics.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.millionic.feelpics.R
import io.millionic.feelpics.models.ImageModel
import io.millionic.feelpics.ui.PhoneAlbumViewActivity
import io.millionic.feelpics.utils.MySharedPref
import kotlinx.android.synthetic.main.grid_image_item.view.*
import java.io.File
import java.lang.Exception

class PhoneAlbumRvAdapter(private var context: Context, private var imageList: ArrayList<ImageModel>): RecyclerView.Adapter<PhoneAlbumRvAdapter.ImageViewHolder>() {
    class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.gridItemIv
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


        holder.image.setOnClickListener {
            val intent = Intent(context, PhoneAlbumViewActivity::class.java)
            intent.putExtra("currPosition", position)
            MySharedPref.setImageViewIndex(context, position)
            try {
                context.startActivity(intent)
            }catch (e: Exception){
                Log.d("NavError", e.message.toString())
            }
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}