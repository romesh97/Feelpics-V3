package io.millionic.feelpics.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.millionic.feelpics.R
import io.millionic.feelpics.ui.onboarding.screens.Intro1Fragment
import io.millionic.feelpics.ui.onboarding.screens.Intro2Fragment
import io.millionic.feelpics.ui.onboarding.screens.Intro3Fragment
import kotlinx.android.synthetic.main.fragment_intro_view_pager.view.*

class IntroViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intro_view_pager, container, false)

        val fragmentList = arrayListOf<Fragment>(
            Intro1Fragment(),
            Intro2Fragment(),
            Intro3Fragment(),
        )

        val adapter = OnBoardingAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        view.introViewPager.adapter = adapter

        return view
    }

}