package com.example.fitnessmoblie.ui.ranking

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitnessmoblie.adapters.RankingAdapter
import com.example.fitnessmoblie.databinding.FragmentRankingBinding
import com.example.fitnessmoblie.models.RankingFilter
import com.example.fitnessmoblie.models.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RankingFragment : Fragment() {

    private val binding by lazy { FragmentRankingBinding.inflate(layoutInflater) }
    private val db by lazy { Firebase.firestore }
    private val users: ArrayList<User> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db.collection("users").get().addOnSuccessListener { snap ->
            snap.forEach {
                val user = it.toObject(User::class.java)
                if (user.totalDistance > 0 || user.totalBurnedCalories > 0) {
                    users.add(user)
                }
            }
            users.sortByDescending { it.totalDistance }

            Log.i("WORKOUTS_COMPLETED_LIST", users.toString())
            val rankingAdapter = RankingAdapter(requireActivity(), users)

            with(binding.rankingRecView) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = rankingAdapter
            }

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
                    when (position) {
                        0 ->
                            rankingAdapter.changeRankingFilter(
                                RankingFilter.DISTANCE_TRAVELLED,
                                users.sortedByDescending { it.totalDistance })
                        1 -> rankingAdapter.changeRankingFilter(
                            RankingFilter.CALORIES_BURNED,
                            users.sortedByDescending { it.totalBurnedCalories })
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }


        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RankingFragment().apply {
                arguments = Bundle().apply {}
            }
    }

}