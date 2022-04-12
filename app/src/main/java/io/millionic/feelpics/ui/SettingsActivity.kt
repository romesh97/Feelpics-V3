package io.millionic.feelpics.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import io.millionic.feelpics.R
import io.millionic.feelpics.models.AlbumImageModel
import io.millionic.feelpics.models.AlbumModel
import io.millionic.feelpics.viewmodels.MainViewModel
import io.millionic.feelpics.viewmodels.MainViewModelFactory
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.max_qty_card.*
import kotlinx.coroutines.*
import org.json.JSONArray
import java.lang.reflect.Type
import org.json.JSONObject




class SettingsActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var dialog:Dialog
    private val albumScope = CoroutineScope(Dispatchers.IO + CoroutineName("AlbumScope"))
    lateinit var viewModel: MainViewModel


    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.hide()

        maxQtyTv.text = getMaxQty().toString()

        initializeDialog()
        val viewModelFactory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOption)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

        val nightModeFlags = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                darkThemeSwitch.isChecked = true

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                darkThemeSwitch.isChecked = false
            }
        }

        darkThemeSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                setDarkMode(true)
                finishAffinity()
                startActivity(Intent(this,SplashActivity::class.java))
            }else{
                setDarkMode(false)
                finishAffinity()
                startActivity(Intent(this,SplashActivity::class.java))
            }
        }

        privatePolicesTv.setOnClickListener {
//            startActivity(Intent(this, PrivatPolicesActivity::class.java))
            val uri: Uri =
                Uri.parse("https://www.iubenda.com/privacy-policy/84509871") // missing 'http://' will cause crashed

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        maxQtyLl.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.max_qty_card,null))

            var addAlbumDialog =  builder.create()
            addAlbumDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            addAlbumDialog.setCancelable(true)
            addAlbumDialog.show()

            addAlbumDialog.maxQtySaveBtn.setOnClickListener {
                val qty =  addAlbumDialog.settingMaxQtyEt.text.toString()
                if (qty.isNotEmpty()){
                    setMaxQty(qty.toInt())
//                    Toast.makeText(this, "$qty", Toast.LENGTH_SHORT).show()
                    maxQtyTv.text = getMaxQty().toString()
                    addAlbumDialog.dismiss()
                }else{
                    Toast.makeText(this, "Max quantity not be empty!", Toast.LENGTH_SHORT).show()
                }
            }


        }

        signInBtn.setOnClickListener {
            Log.d(TAG, "onCreate: begin Google SignIN")
            showDialog(true)
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
        signOutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut()
            Toast.makeText(this, "Sign out successfully.", Toast.LENGTH_SHORT).show()
            checkUser()
        }

        backupBtn.setOnClickListener {
            showDialog(true)
            viewModel.getAllAlbum().observe(this,{albums->
                if (it != null) {
                    val albumImages = viewModel.readAllAlbumImage()
                    saveFireStore(albums, albumImages)
                }
            })
        }
    }

    private fun saveFireStore(albums: List<AlbumModel>, albumImages: List<AlbumImageModel>) {
        val db = FirebaseFirestore.getInstance()
        val user: MutableMap<String, Any> = HashMap()
        user["albums"] = Gson().toJson(albums)
        user["albumImages"] = Gson().toJson(albumImages)

        db.collection("users").document(firebaseAuth.uid!!)
            .set(user)
            .addOnSuccessListener {
                showDialog(false)
                Toast.makeText(this@SettingsActivity, "record added successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@SettingsActivity, "record failed to add ${it.message}", Toast.LENGTH_LONG).show()
                showDialog(false)
            }
    }

    private fun readFireStoreData(){
        showDialog(true)
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(firebaseAuth.uid!!)
            .get()
            .addOnCompleteListener {snap->
                if (snap.isSuccessful){
                    val albumsJson = snap.result["albums"]
                    val albumImagesJson = snap.result["albumImages"]
                    val gson = GsonBuilder().create()
                    val albums = gson.fromJson<ArrayList<AlbumModel>>(albumsJson.toString(), object :TypeToken<ArrayList<AlbumModel>>(){}.type)
                    val albumImages = gson.fromJson<ArrayList<AlbumImageModel>>(albumImagesJson.toString(), object :TypeToken<ArrayList<AlbumImageModel>>(){}.type)
                    Log.d("retrieveData", "$albums")
                    if (albums!=null){
                        for (album in albums){
                            viewModel.addAlbum(album)
                        }
                    }
                    if (albumImages != null){
                        for (albumImage in albumImages){
                            viewModel.addAlbumImage(albumImage)
                        }
                    }

                    Toast.makeText(this@SettingsActivity, "Data synced successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@SettingsActivity, "No data found", Toast.LENGTH_SHORT).show()
                }
                showDialog(false)
            }
            .addOnFailureListener {
                Toast.makeText(this@SettingsActivity, "${it.message}", Toast.LENGTH_SHORT).show()
                showDialog(false)
            }

    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null){
            signInBtn.visibility = View.GONE
            signOutBtn.visibility = View.VISIBLE
            backupBtn.visibility = View.VISIBLE
        }else{
            signInBtn.visibility = View.VISIBLE
            signOutBtn.visibility = View.GONE
            backupBtn.visibility = View.GONE
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }catch (e: Exception){
                showDialog(false)
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {authResult ->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: loggedIn")

                val firebaseUser = firebaseAuth.currentUser
                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $uid")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")
                
                if (authResult.additionalUserInfo!!.isNewUser){
                    //user is new - Account created
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account created...\n$email")
                    Toast.makeText(this@SettingsActivity, "Account created successfully.", Toast.LENGTH_SHORT).show()
                    showDialog(false)
                }else{
                    //existing user loggedIn
                    readFireStoreData()
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing user.... \n$email")
                    Toast.makeText(this@SettingsActivity, "successfully logged-in.", Toast.LENGTH_SHORT).show()
                }
                checkUser()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed due to ${e.message}")
                Toast.makeText(this@SettingsActivity, "Login Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
                showDialog(false)
            }
    }

    private fun getMaxQty(): Int{
        val sharePref = this.getSharedPreferences("maxQty", Context.MODE_PRIVATE)
        return sharePref.getInt("qty", 100)
    }

    private fun setMaxQty(maxQty: Int){
        val sharedPref = this.getSharedPreferences("maxQty", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("qty", maxQty)
        editor.apply()
    }

    private fun setDarkMode(status: Boolean){
        val sharedPref = this.getSharedPreferences("darkMode", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("status", status)
        editor.apply()
    }

    private fun initializeDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        builder.setView(inflater.inflate(R.layout.progress_data,null))
        dialog =  builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun showDialog(isVisible:Boolean){
        if (isVisible){
            dialog.show()
        }else{
            dialog.cancel()
        }
    }
}
