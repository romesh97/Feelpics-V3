package io.millionic.feelpics.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import io.millionic.feelpics.R
import io.millionic.feelpics.ui.AlbumActivity
import kotlinx.android.synthetic.main.label_chipset.view.*

class ChipsetAdapter(private var albumActivity: AlbumActivity, private var addAlbumDialog: AlertDialog, private var chipsetList: ArrayList<String>): RecyclerView.Adapter<ChipsetAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val closeIv = view.chipsetCloseIv
        val chipsetTv = view.chipsetTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.label_chipset, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.chipsetTv.text = chipsetList[position]
//        notifyItemRemoved(position)
        holder.closeIv.setOnClickListener {
//            Toast.makeText(albumActivity, "Closed clicked", Toast.LENGTH_SHORT).show()
            albumActivity.setUpDialogLabelRemove(addAlbumDialog,chipsetList[position])
        }

    }

    override fun getItemCount(): Int {
        return chipsetList.size
    }

}