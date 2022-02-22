package com.example.fitnessmoblie.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.fitnessmoblie.R
import com.example.fitnessmoblie.databinding.RankingItemBinding
import com.example.fitnessmoblie.models.RankingFilter
import com.example.fitnessmoblie.models.User
import com.example.fitnessmoblie.ui.authentication.TAG
import com.squareup.picasso.Picasso
import java.math.RoundingMode
import java.text.DecimalFormat


class RankingAdapter(private val contex: Context, private var users: List<User>) : RecyclerView.Adapter<RankingAdapter.ViewHolder>() {

    private var rankingFilter = RankingFilter.DISTANCE_TRAVELLED

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = RankingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val url =
            "https://firebasestorage.googleapis.com/v0/b/fitnesscmu-66c4f.appspot.com/o/images%2F${user.id}.jpg%20?alt=media&token=6fb19564-53f7-4b4c-9edf-db7f8391b08d"
        when (rankingFilter) {
            RankingFilter.DISTANCE_TRAVELLED -> {
                holder.tvLabelScore.text = "Distancia"
                val label = "${round(user.totalDistance.toDouble()) / 1000.0} km"
                holder.tvScore.text = label
            }
            RankingFilter.CALORIES_BURNED -> {
                holder.tvLabelScore.text = "Calorias"
                holder.tvScore.text = "${user.totalBurnedCalories}"
            }
        }
        Log.i("URL_PHOTO_ADAPTER", url)

        Glide.with(contex)
            .asBitmap()
            .signature(ObjectKey(System.currentTimeMillis()))
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .placeholder(R.drawable.photocercle)
            .into(holder.userPhoto)

        holder.tvUserName.text = user.name
        holder.position.text = position.plus(1).toString()
    }

    fun changeRankingFilter(rankingFilter: RankingFilter, users: List<User>) {
        this.rankingFilter = rankingFilter
        this.users = users
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = users.size

    private fun round(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.UP
        val format = df.format(number).replace(",".toRegex(), ".")
        return format.toDouble()
    }

    inner class ViewHolder(binding: RankingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvScore = binding.tvScore
        val tvUserName = binding.tvUserName
        val userPhoto = binding.userPhoto
        val position = binding.userScore
        val tvLabelScore = binding.tvLabelScore
    }


}