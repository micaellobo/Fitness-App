package com.example.fitnessmoblie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.fitnessmoblie.databinding.ActivityMainBinding
import com.example.fitnessmoblie.database.WorkoutViewModel
import com.example.fitnessmoblie.ui.home.HomeFragment
import com.example.fitnessmoblie.ui.map.MapLocationFragment
import com.example.fitnessmoblie.ui.user.UserProfileFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private val firebaseAuth by lazy { Firebase.auth }
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val powerManager by lazy { getSystemService(POWER_SERVICE) as PowerManager }
    private val workoutViewModel by lazy { ViewModelProvider(this)[WorkoutViewModel::class.java] }
    private lateinit var imageProfile: CircleImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (powerManager.isPowerSaveMode)
            Toast.makeText(this, "Funcionalidades limitadas, modo poupança energia ativo", Toast.LENGTH_LONG).show()

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home ->
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, HomeFragment())
                        .addToBackStack(null)
                        .commit()
                R.id.nav_mapa -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, MapLocationFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_dados_utilizador -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, UserProfileFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.nav_settings -> {
                    Toast.makeText(applicationContext, "Clicked Settings", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_exit -> {
                    signOut()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        val findViewById = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tv_email_id)
        imageProfile = binding.navView.getHeaderView(0).findViewById<CircleImageView>(R.id.imgProfileMenu)
        val linearInfoUser = binding.navView.getHeaderView(0)


        linearInfoUser.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, UserProfileFragment())
                .addToBackStack(null)
                .commit()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        val email = intent.getStringExtra("email")

        email?.let { workoutViewModel.setCurrentUserEmail(it) }

        downloadImage()
        findViewById.text = email

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, HomeFragment())
            .commit()
    }

    fun downloadImage() {
        val id = firebaseAuth.currentUser?.uid
        if (id == null) {
            signOut()
        } else {
            val url =
                "https://firebasestorage.googleapis.com/v0/b/fitnesscmu-66c4f.appspot.com/o/images%2F$id.jpg%20?alt=media&token=6fb19564-53f7-4b4c-9edf-db7f8391b08d"
            Glide.with(applicationContext)
                .asBitmap()
                .signature(ObjectKey(System.currentTimeMillis()))
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.photocercle)
                .into(imageProfile)


            Log.i("URL_PHOTO_MAIN", url)

        }
    }

    private fun signOut() {
        firebaseAuth.signOut()
        val intent = Intent(this, Authentication::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}