package com.tods.project_olx.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityAdDetailsBinding
import com.tods.project_olx.databinding.ActivityMainBinding
import com.tods.project_olx.model.Ad

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
        val imageListener: ImageListener = ImageListener { position, imageView ->
            val urlString: String = selectedAd.adImages[position]
            Picasso.get().load(urlString).into(imageView)
        }
        binding.carouselAds.pageCount = selectedAd.adImages.size
        binding.carouselAds.setImageListener(imageListener)
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