package io.millionic.feelpics.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import io.millionic.feelpics.R
import io.millionic.feelpics.ui.albumCreateTour.screens.AlbumTour1Fragment
import io.millionic.feelpics.ui.albumCreateTour.screens.AlbumTour2Fragment
import io.millionic.feelpics.ui.onboarding.OnBoardingAdapter
import kotlinx.android.synthetic.main.activity_create_album.*

class CreateAlbumActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_album)

        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val fragmentList = arrayListOf<Fragment>(
            AlbumTour1Fragment(),
            AlbumTour2Fragment()
        )

        val adapter = OnBoardingAdapter(
            fragmentList,
            supportFragmentManager,
            lifecycle
        )

        createAlbumViewPager.adapter = adapter

    }
}