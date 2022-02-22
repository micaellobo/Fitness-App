package com.example.fitnessmoblie.ui.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.FragmentExerciseListBinding


class ExerciseListFragment : Fragment() {

    private val binding by lazy { FragmentExerciseListBinding.inflate(layoutInflater) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ExerciseListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}