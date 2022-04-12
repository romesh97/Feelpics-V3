package io.millionic.feelpics.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.millionic.feelpics.R
import kotlinx.android.synthetic.main.activity_settings.*
import java.lang.Exception

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        val dMode = getDarkMode()
        if (!getIsAppFirstTime()) {
            if (dMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }else{
            val nightModeFlags = resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    setDarkMode(true)

                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    setDarkMode(false)
                }
            }
            setIsAppFirstTime()
        }

        setIsRunningTime()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
            Handler().postDelayed({
                doPermissionFor11()
            },2100)

        }else{
            appPermissionBelow11()
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun doPermissionFor11() {
        if (Environment.isExternalStorageManager()){
            checkPermissionDexter11()
        }else{
            appPermissionFor11()
        }
    }

    private fun appPermissionFor11() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.addCategory("android.intent.category.DEFAULT")
            intent.setData(Uri.parse(String.format("package:%s", applicationContext.packageName)))
            startActivityForResult(intent, 100)
        }catch (e:Exception){
            val intent = Intent()
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivityForResult(intent, 100)
        }
    }

    private fun checkPermissionDexter11()
    {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.areAllPermissionsGranted()) {
                        finishAffinity()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                    AlertDialog.Builder(this@SplashActivity).setTitle(R.string.app_name)
                        .setMessage(R.string.permission_rationale_message)
                        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                            token.cancelPermissionRequest()
                        }
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            token.continuePermissionRequest()
                        }
                        .setOnDismissListener({ token.cancelPermissionRequest() })
                        .show()
                }
            })
            .check()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100){
            if (Environment.isExternalStorageManager()) {
                doPermissionFor11()
            }else{
                finishAffinity()
            }
        }
//        if (resultCode == RESULT_OK){
//            if (requestCode == 100) {
//                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
//                    if (Environment.isExternalStorageManager()) {
//                        doPermissionFor11()
//                    }
//                }
//            }else{
//                finishAffinity()
//            }
//        }
    }

    private fun appPermissionBelow11(){

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.areAllPermissionsGranted()) {
                        finishAffinity()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                    AlertDialog.Builder(this@SplashActivity).setTitle(R.string.app_name)
                        .setMessage(R.string.permission_rationale_message)
                        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                            dialog.dismiss()
                            token.cancelPermissionRequest()
                        }
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                            token.continuePermissionRequest()
                        }
                        .setOnDismissListener({ token.cancelPermissionRequest() })
                        .show()
                }
            })
            .check()
    }
    private fun setIsRunningTime(){
        val sharedPref = this.getSharedPreferences("isRunningProcess", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("isRunning",false)
        editor.apply()
    }

    private fun getDarkMode(): Boolean{
        val sharePref = this.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
        return sharePref.getBoolean("status", false)
    }

    private fun setDarkMode(status: Boolean){
        val sharedPref = this.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("status", status)
        editor.apply()
    }

    private fun setIsAppFirstTime(){
        val sharedPref = this.getSharedPreferences("isAppFirstTime", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("status",false)
        editor.apply()
    }

    private fun getIsAppFirstTime(): Boolean{
        val sharePref = this.getSharedPreferences("isAppFirstTime", Context.MODE_PRIVATE)
        return sharePref.getBoolean("status", true)
    }




}