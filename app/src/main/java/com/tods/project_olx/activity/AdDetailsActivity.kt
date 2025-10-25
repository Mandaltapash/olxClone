package com.tods.project_olx.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.tods.project_olx.databinding.ActivityAdDetailsBinding
import com.tods.project_olx.model.Ad
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var selectedAd: Ad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configAdDetails()
        configCarousel()
        configButtonCallClickListener()
    }

    private fun configButtonCallClickListener() {
        binding.buttonCall.setOnClickListener(View.OnClickListener {
            automaticCall()
        })
    }

    private fun configToolbar() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Ad Details"
    }

    private fun configCarousel() {
        val imageAdapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val imageView = ImageView(parent.context)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                return object : RecyclerView.ViewHolder(imageView) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val imageView = holder.itemView as ImageView
                val urlString: String = selectedAd.adImages[position]
                Picasso.get().load(urlString).into(imageView)
            }

            override fun getItemCount(): Int {
                return selectedAd.adImages.size
            }
        }
        binding.viewPagerImages.adapter = imageAdapter
    }

    private fun configAdDetails() {
        selectedAd = intent.getSerializableExtra("selectedAd") as Ad
        binding.textAdDetailTitle.text = selectedAd.title
        binding.textAdDetailUf.text = selectedAd.state
        binding.textAdDetailValue.text = selectedAd.value
        binding.textAdDetailDescription.text = selectedAd.description
    }

    private fun configViewBinding() {
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun automaticCall() {
        val rawValueA: String = selectedAd.phone.replace("(", "")
        val rawValueB: String = rawValueA.replace(")", "")
        val rawValueC: String = rawValueB.replace(" ", "")
        val phone: String = "tel:$rawValueC"
        Log.i("PHONE", "automaticCall: $phone")
        val intent: Intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse(phone)
        startActivity(intent)
    }
}