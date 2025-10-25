package com.tods.project_olx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tods.project_olx.R
import com.tods.project_olx.databinding.CustomAdapterAdBinding
import com.tods.project_olx.model.Ad

class AdAdapter(
    private val onAdClick: (Ad) -> Unit
) : ListAdapter<Ad, AdAdapter.AdViewHolder>(AdDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdViewHolder {
        val binding = CustomAdapterAdBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdViewHolder(binding, onAdClick)
    }

    override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AdViewHolder(
        private val binding: CustomAdapterAdBinding,
        private val onAdClick: (Ad) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ad: Ad) {
            with(binding) {
                textTitle.text = ad.title
                textDescription.text = ad.description
                textValue.text = ad.value

                // Load image with Glide
                if (ad.adImages.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(ad.adImages[0])
                        .placeholder(R.drawable.standard)
                        .error(R.drawable.standard)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageAd)
                } else {
                    imageAd.setImageResource(R.drawable.standard)
                }

                root.setOnClickListener {
                    onAdClick(ad)
                }
            }
        }
    }

    class AdDiffCallback : DiffUtil.ItemCallback<Ad>() {
        override fun areItemsTheSame(oldItem: Ad, newItem: Ad): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Ad, newItem: Ad): Boolean {
            return oldItem == newItem
        }
    }
}