package com.example.fitnessmoblie.ui.authentication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.fitnessmoblie.MainActivity
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>

    private val firebaseAuth by lazy { Firebase.auth }
    private val binding by lazy { FragmentLoginBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        signInInputsArray = arrayOf(binding.etSignInEmail, binding.etSignInPassword)
        binding.btnCreateAccount2.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, SignupFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tvResetPassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ResetPasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnSignIn.setOnClickListener {
            it.isEnabled = false
            signInUser()
        }
        return binding.root
    }

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {

        signInEmail = binding.etSignInEmail.text.toString().trim()
        signInPassword = binding.etSignInPassword.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireActivity(), "Login com Sucesso", Toast.LENGTH_SHORT).show()
                        val mainActivity = Intent(requireActivity(), MainActivity::class.java)
                        mainActivity.putExtra("email", signInEmail)

                        startActivity(mainActivity)

                    } else {
                        Toast.makeText(requireActivity(), "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                    }
                    binding.btnSignIn.isEnabled = true
                }
        } else {
            signInInputsArray.forEach {
                if (it.text.toString().trim().isEmpty()) {
                    it.error = "${it.hint} em falta"
                }
            }
            binding.btnSignIn.isEnabled = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}