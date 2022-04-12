package io.millionic.feelpics.ui.onboarding


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import io.millionic.feelpics.R
import java.lang.Exception

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        Handler().postDelayed({
            if (onBoardingFinished()) {
                try {
                    findNavController().navigate(R.id.action_splashFragment_to_homeActivity)
                    requireActivity().finish()
                }catch (e: Exception){
//                    Toast.makeText(requireContext(), "dfas", Toast.LENGTH_SHORT).show()
                }

            }else{
                try {
                    findNavController().navigate(R.id.action_splashFragment_to_introViewPagerFragment)
                }catch (e: Exception){
//                    Toast.makeText(requireContext(), "dfas", Toast.LENGTH_SHORT).show()
                }

            }
        },2000)

        return view
    }

    private fun onBoardingFinished(): Boolean{
        try {
            val sharePref =
                requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
            return sharePref.getBoolean("Finished", false)
        }catch (e:Exception){
            return true
        }
    }

}