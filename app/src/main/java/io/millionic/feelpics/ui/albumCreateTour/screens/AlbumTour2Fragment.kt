package io.millionic.feelpics.ui.albumCreateTour.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.millionic.feelpics.R
import kotlinx.android.synthetic.main.fragment_album_tour2.view.*


class AlbumTour2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_album_tour2, container, false)

        view.albumTour2Btn.setOnClickListener {
            requireActivity().finish()
        }

        return view
    }
}