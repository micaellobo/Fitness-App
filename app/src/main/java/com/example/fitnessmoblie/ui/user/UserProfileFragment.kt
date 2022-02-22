package com.example.fitnessmoblie.ui.user

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.fitnessmoblie.Authentication
import com.example.fitnessmoblie.MainActivity
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.database.FitnessDb
import com.example.fitnessmoblie.databinding.FragmentUserProfileBinding
import com.example.fitnessmoblie.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class UserProfileFragment : Fragment() {


    private val binding by lazy { FragmentUserProfileBinding.inflate(layoutInflater) }
    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val db by lazy { Firebase.firestore }

    private lateinit var user: User
    private var isEditMode = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Sessao invalida", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({
                firebaseAuth.signOut()
                val intent = Intent(requireContext(), Authentication::class.java)
                startActivity(intent)
                requireActivity().finish()
            }, 2000)
        } else {
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.i("URL_PHOTO", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            this.user = user
                            setupValues()
                        }
                    } else {
                        Log.i("URL_PHOTO", "Current data: null")
                    }
                }
            downloadImage(uid)

        }

        binding.imgProfile.setOnClickListener { openImageChoose() }

        binding.btnEditSaveChanges.setOnClickListener { setupButtons() }

        binding.btnSave.setOnClickListener { FitnessDb.executors.execute { saveChanges() } }

        return binding.root
    }

    private fun downloadImage(userId: String) {
        val url =
            "https://firebasestorage.googleapis.com/v0/b/fitnesscmu-66c4f.appspot.com/o/images%2F$userId.jpg%20?alt=media&token=6fb19564-53f7-4b4c-9edf-db7f8391b08d"

        Glide.with(requireContext())
            .asBitmap()
            .signature(ObjectKey(System.currentTimeMillis()))
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.photocercle)
            .into(binding.imgProfile)

//        val mainActivity = requireActivity() as MainActivity
//        mainActivity.downloadImage()
        Log.i("URL_PHOTO_PROFILE", url)
    }


    private fun setupButtons() {
        when {
            isEditMode -> {
                goViewMode()
            }
            else -> {
                goEditMode()
            }
        }
    }

    private fun saveChanges() {
        val age = binding.edAge.text.toString().toInt()
        val height = binding.edHeight.text.toString().toInt()
        val name = binding.edName.text.toString()
//        val password = binding.edPassword.text.toString()
        val weight = binding.edWeight.text.toString().toInt()

        db.collection("users").document(firebaseAuth.currentUser!!.uid)
            .update(
                "age", age, "height", height,
                "name", name, "weight", weight
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Perfil alterado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao alterar o perfil", Toast.LENGTH_SHORT).show()
            }
    }


    fun goEditMode() {
        isEditMode = true
        binding.btnSave.isEnabled = true

        binding.btnEditSaveChanges.text = "Cancelar"

        binding.edAge.isEnabled = true
        binding.edHeight.isEnabled = true
        binding.edName.isEnabled = true
//        binding.edPassword.isEnabled = true
        binding.edWeight.isEnabled = true
    }

    fun goViewMode() {
        isEditMode = false
        binding.btnSave.isEnabled = false

        binding.btnEditSaveChanges.text = "Editar"

        binding.edAge.isEnabled = false
        binding.edHeight.isEnabled = false
        binding.edName.isEnabled = false
//        binding.edPassword.isEnabled = false
        binding.edWeight.isEnabled = false
    }

    private fun setupValues() {
        binding.edAge.setText(user.age.toString())
        binding.edHeight.setText(user.height.toString())
        binding.edEmail.setText(user.email)
        binding.edName.setText(user.name)
        binding.tvName.text = user.name
//        binding.edPassword.setText()
        binding.edWeight.setText(user.weight.toString())
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val imageUri = result.data?.data

            if (imageUri != null) {
                Glide.with(requireContext())
                    .asBitmap()
                    .signature(ObjectKey(System.currentTimeMillis()))
                    .load(imageUri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.photocercle)
                    .into(binding.imgProfile)

                Handler(Looper.getMainLooper()).postDelayed({
                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.downloadImage()
                }, 2000)
                uploadImage(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        val uid = firebaseAuth.uid

        if (uid == null) {
            Toast.makeText(requireContext(), "Sessao invalida", Toast.LENGTH_LONG).show()
            Handler(Looper.getMainLooper()).postDelayed({
                firebaseAuth.signOut()
                val intent = Intent(requireContext(), Authentication::class.java)
                startActivity(intent)
                requireActivity().finish()
            }, 2000)
        } else {
            val riversRef: StorageReference = firebaseStorage.reference.child("images/$uid.jpg ")

            riversRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot -> }
        }
    }


    private fun openImageChoose() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            UserProfileFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}