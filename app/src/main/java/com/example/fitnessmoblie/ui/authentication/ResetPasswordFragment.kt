package com.example.fitnessmoblie.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentLoginBinding
import com.example.fitnessmoblie.databinding.FragmentResetPasswordBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordFragment : Fragment() {

    private val firebaseAuth by lazy { Firebase.auth }
    private val binding by lazy { FragmentResetPasswordBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding.btnResetPassword.setOnClickListener {
            resetPassword()
        }

        binding.imageViewPreview.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }

    private fun notEmpty(): Boolean = binding.etEmail.text.toString().trim().isNotEmpty()

    private fun resetPassword() {
        val resetEmail = binding.etEmail.text.toString().trim()

        if (notEmpty()) {
            if (!Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                binding.etEmail.setError("Por favor introduza um email valido!")
                binding.etEmail.requestFocus()
                return
            }
            binding.progressBar.setVisibility(View.VISIBLE)

            firebaseAuth.sendPasswordResetEmail(resetEmail)
                .addOnCompleteListener(
                    OnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(requireActivity(), "Verifique o email para alterar a password!", Toast.LENGTH_SHORT).show()
                            binding.progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(requireActivity(), it.exception?.message, Toast.LENGTH_SHORT).show()
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    })
        } else {
            binding.etEmail.setError("Tem de inserir um email")
            binding.etEmail.requestFocus()
            return
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ResetPasswordFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}