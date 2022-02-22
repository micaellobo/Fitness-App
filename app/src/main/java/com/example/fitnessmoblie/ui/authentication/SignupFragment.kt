package com.example.fitnessmoblie.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.fitnessmoblie.MainActivity
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentSignupBinding
import com.example.fitnessmoblie.models.User
import com.example.fitnessmoblie.database.FitnessDb
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val TAG: String = "TAG_SIGNUP_FRAGMENT"

class SignupFragment : Fragment() {

    private lateinit var createAccountInputsArray: Array<EditText>

    private val firebaseAuth by lazy { Firebase.auth }
    private val firebaseFireStore by lazy { Firebase.firestore }
    private val binding by lazy { FragmentSignupBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        createAccountInputsArray =
            arrayOf(binding.etEmail, binding.etPassword, binding.etConfirmPassword, binding.etAge, binding.etHeight, binding.etWeight)
        binding.btnCreateAccount.setOnClickListener {
            signUp()
        }

        binding.imageViewPreview.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun notEmpty(): Boolean = binding.etEmail.text.toString().trim().isNotEmpty() &&
            binding.etPassword.text.toString().trim().isNotEmpty() &&
            binding.etConfirmPassword.text.toString().trim().isNotEmpty() && binding.etAge.text.toString().trim()
        .isNotEmpty() && binding.etHeight.text.toString().trim().isNotEmpty() && binding.etWeight.text.toString().trim().isNotEmpty()

    private fun identicalPassword(): Boolean {
        var identical = false
        if (notEmpty() && binding.etPassword.text.toString().trim() == binding.etConfirmPassword.text.toString().trim()) {
            identical = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach {
                if (it.text.toString().trim().isEmpty()) {
                    it.error = "${it.hint} em falta"
                }
            }
        } else {
            Toast.makeText(requireContext(), "Passwords não coincidem", Toast.LENGTH_SHORT).show()
        }
        return identical
    }

    private fun signUp() {
        if (identicalPassword()) {
            val userEmail = binding.etEmail.text.toString().trim()
            val userPassword = binding.etPassword.text.toString().trim()


            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user =
                            User(
                                firebaseAuth.currentUser!!.uid,
                                binding.etName.text.toString().trim(),
                                userEmail,
                                binding.etAge.text.toString().trim().toInt(),
                                binding.etHeight.text.toString().trim().toInt(),
                                binding.etWeight.text.toString().trim().toInt(),
                            )
                        Toast.makeText(requireContext(), "Conta criada com sucesso", Toast.LENGTH_SHORT).show()
                        saveUserFireStore(user)
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Falha na Autenticação", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveUserFireStore(user: User) {
        FitnessDb.executors.execute {
            firebaseFireStore.collection("users").document(firebaseAuth.currentUser!!.uid)
                .set(user)
                .addOnSuccessListener {
                    Log.i(TAG, "USER ADDED ${firebaseAuth.currentUser!!.uid}")
                }
                .addOnFailureListener { e ->
                    Log.i(TAG, "ERRO GUARDAR INFO", e)
                }
        }
    }


    //    private fun sendEmailVerification() {
//        firebaseAuth.currentUser?.let {
//            it.sendEmailVerification().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(requireActivity(), "email sent to ${binding.etEmail.text.toString().trim()}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }


    companion object {
        @JvmStatic
        fun newInstance() =
            SignupFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}