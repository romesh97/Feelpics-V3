package io.millionic.feelpics.utils

import android.app.Activity
import android.content.Context

import android.content.SharedPreferences




class MySharedPref() {
    companion object{
        fun setImageViewIndex(context:Context, index:Int){
            val sharedPref = context.getSharedPreferences("ImageViewer", Context.MODE_PRIVATE).edit()
            sharedPref.putInt("imageViewIndex", index)
            sharedPref.apply()
        }

        fun getImageViewIndex(context:Context):Int{
            val sharedPref = context.getSharedPreferences("ImageViewer", Context.MODE_PRIVATE)
            return sharedPref.getInt("imageViewIndex",0)
        }
    }

}