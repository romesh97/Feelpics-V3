package io.millionic.feelpics.ui.albumCreateTour.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import io.millionic.feelpics.R
import kotlinx.android.synthetic.main.fragment_album_tour1.view.*

class AlbumTour1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_album_tour1, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.createAlbumViewPager)

        view.albumTour1Btn.setOnClickListener {
            viewPager?.currentItem = 1
        }

        return view
    }

}