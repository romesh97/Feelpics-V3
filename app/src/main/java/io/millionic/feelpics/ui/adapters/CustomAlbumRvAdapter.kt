package io.millionic.feelpics.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.ui.AlbumActivity
import io.millionic.feelpics.ui.CustomImageAlbumActivity
import kotlinx.android.synthetic.main.album_card.view.*
import kotlinx.android.synthetic.main.rename_dialog.*
import java.io.File

class CustomAlbumRvAdapter(private var context: Context, private var imageList: List<AlbumModel>,private var activity: AlbumActivity): RecyclerView.Adapter<CustomAlbumRvAdapter.ImageViewHolder>(){
    class ImageViewHolder(view:View): RecyclerView.ViewHolder(view) {
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
            .load(File(imageList[position].thumbnail))
            .apply(myOptions)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(holder.image)

        holder.text.text = imageList[position].albumName
        
        holder.cv.setOnClickListener{
            val intent = Intent(context, CustomImageAlbumActivity::class.java)
            intent.putExtra("albumId",imageList[position].albumId)
            intent.putExtra("albumName",imageList[position].albumName)
            context.startActivity(intent)
        }
        
        holder.more.setOnClickListener {
            showAlbumMenu(it, imageList[position])
        }

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun showAlbumMenu(view: View, album: AlbumModel){
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.album_menu)
        popup.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.delete ->{
                    delteAlbum(album)
                    true
                }
                R.id.rename -> {
                    if (album.albumName.equals("Favourites")) {
                        Toast.makeText(
                            context,
                            "You can't do rename on favorite section",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        showRenameDialog(album)
                    }
                    true
                }
                else -> true
            }
        }
        popup.show()
        val pop = PopupMenu::class.java.getDeclaredField("mPopup")
        pop.isAccessible = true
        val menu = pop.get(popup)
        menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
            .invoke(menu, true)

    }

    private fun showRenameDialog(album: AlbumModel) {
        val builder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.rename_dialog,null))

        var rnameDialog =  builder.create()
        rnameDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        rnameDialog.setCancelable(true)
        rnameDialog.show()

        
        rnameDialog.renameAlbumBtn.setOnClickListener {
            val name = rnameDialog.albumRenameEt.text.toString()
            if (name.isNotEmpty()){
                album.albumName = name
                activity.renameAlbum(album)
                rnameDialog.dismiss()
            }else{
                Toast.makeText(context, "Please fill the rename!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun delteAlbum(album: AlbumModel) {
        activity.deleteAlbum(album)
    }

}