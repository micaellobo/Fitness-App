package com.example.fitnessmoblie

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fitnessmoblie.databinding.ActivityAuthenticationBinding
import com.example.fitnessmoblie.ui.authentication.LoginFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.widget.TextView
import android.widget.Toast

class Authentication : AppCompatActivity() {

    private val binding by lazy { ActivityAuthenticationBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("email", currentUser.email)
            startActivity(intent)
            finish()
        } else {
            supportFragmentManager.beginTransaction()
                .add(binding.frameLayout.id, LoginFragment())
                .commit()
        }
    }


}