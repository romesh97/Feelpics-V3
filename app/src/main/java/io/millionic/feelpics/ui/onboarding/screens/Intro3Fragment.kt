package io.millionic.feelpics.ui.onboarding.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import io.millionic.feelpics.R
import kotlinx.android.synthetic.main.fragment_intro2.view.*
import kotlinx.android.synthetic.main.fragment_intro3.view.*


class Intro3Fragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intro3, container, false)


        view.finishBtn.setOnClickListener {
            findNavController().navigate(R.id.action_introViewPagerFragment_to_homeActivity)
            onBoardingFinished()
            requireActivity().finishAffinity()
        }

        return view
    }

    private fun onBoardingFinished(){
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished",true)
        editor.apply()
    }

}