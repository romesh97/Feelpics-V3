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
import io.millionic.feelpics.models.PhoneAlbumModel
import io.millionic.feelpics.ui.AlbumActivity
import io.millionic.feelpics.ui.PhoneAlbumImagesActivity
import kotlinx.android.synthetic.main.album_card.view.*
import java.io.File

class AlbumRvAdapter(private var context: Context, private var imageList: ArrayList<PhoneAlbumModel>,private var activity: AlbumActivity): RecyclerView.Adapter<AlbumRvAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image = view.gridAlbumItemIv
        val more = view.album_more
        val text =  view.gridAlbumItemTv
        val cv =  view.phoneImageCv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.album_card, parent, false)
        return ImageViewHolder(view)

    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val myOptions = RequestOptions()
            .override(holder.image.width, holder.image.height)
            .centerCrop()

        Glide.with(context)
            .load(File(imageList[position].images[0].imgPath))
            .apply(myOptions)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.image)

//        holder.text.text = imageList[position].type +"( ${imageList[position].images.size} pics )"
        holder.text.text = imageList[position].type.toString()

        holder.cv.setOnClickListener{
            val intent = Intent(context, PhoneAlbumImagesActivity::class.java)
//            val args = Bundle()
//            args.putParcelableArrayList("imgList", imageList[position].images)
//            intent.putExtras(args)

            activity.app.setCurrPhoneAlbum(imageList[position].images)

            intent.putExtra("type",imageList[position].type)
            context.startActivity(intent)
        }

        holder.more.visibility = View.GONE

    }

    override fun getItemCount(): Int {

        return imageList.size

    }
}