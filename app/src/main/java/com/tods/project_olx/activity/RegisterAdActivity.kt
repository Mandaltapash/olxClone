package com.tods.project_olx.activity

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityRegisterAdBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User
import java.util.*

private const val SELECTION_GALLERY_AD1 = 100
private const val SELECTION_GALLERY_AD2 = 200
private const val SELECTION_GALLERY_AD3 = 300

class RegisterAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterAdBinding
    private lateinit var ad: Ad
    private lateinit var dialog: ProgressDialog  // ✅ Changed from SpotsDialog
    private var listImages: MutableList<String> = mutableListOf()
    private var listUrlImages: MutableList<String> = mutableListOf()
    private var storage: FirebaseStorage = Firebase.storage
    private var loggedUser: FirebaseUser? = User().configCurrentUser()
    private var permission: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i("PERMISSION", "onCreate: granted")
        } else {
            Log.i("PERMISSION", "onCreate: denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configLocaleCurrencyMask()
        requestPermission()
        configClickListenerAd1()
        configClickListenerAd2()
        configClickListenerAd3()
        configClickListenerRegisterAd()
        configSpinners()
    }

    private fun configClickListenerRegisterAd() {
        binding.buttonRegisterAd.setOnClickListener {
            validateAdFields()
        }
    }

    private fun configClickListenerAd1() {
        binding.imageAd1.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select: "), SELECTION_GALLERY_AD1)
        }
    }

    private fun configClickListenerAd2() {
        binding.imageAd2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select: "), SELECTION_GALLERY_AD2)
        }
    }

    private fun configClickListenerAd3() {
        binding.imageAd3.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select: "), SELECTION_GALLERY_AD3)
        }
    }

    private fun configLocaleCurrencyMask() {
        val locale = Locale("pt", "BR")
        binding.editValue.locale = locale
    }

    private fun configToolbar() {
        supportActionBar?.title = "New Ad"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun configViewBinding() {
        binding = ActivityRegisterAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    SELECTION_GALLERY_AD1
                )
            }
            else -> {
                permission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun alertDialogPermission() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("PERMISSION DENIED")
        builder.setMessage("In order to set images for the ad, it is necessary to accept the permission")
        builder.setCancelable(false)
        builder.setPositiveButton("CONFIRM") { _, _ ->
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val selectedImage = data!!.data
            val pathImage = selectedImage.toString()
            try {
                when (requestCode) {
                    SELECTION_GALLERY_AD1 -> {
                        binding.imageAd1.setImageURI(selectedImage)
                    }
                    SELECTION_GALLERY_AD2 -> {
                        binding.imageAd2.setImageURI(selectedImage)
                    }
                    SELECTION_GALLERY_AD3 -> {
                        binding.imageAd3.setImageURI(selectedImage)
                    }
                }
                listImages.add(pathImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImagesStorage(url: String, totalImages: Int, i: Int) {
        val imageRef: StorageReference = storage.getReference("images")
            .child("ads")
            .child(ad.id)
            .child("image$i.JPEG")
        val uploadTask: UploadTask = imageRef.putFile(Uri.parse(url))
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnCompleteListener {
                val firebaseUrl = it.result
                val convertedUrl: String = firebaseUrl.toString()
                listUrlImages.add(convertedUrl)
                if (totalImages == listUrlImages.size) {
                    ad.adImages = listUrlImages
                    ad.save()
                    dialog.dismiss()
                    finish()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAd() {
        configDialog()
        for (i in listImages.indices) {
            val urlImage: String = listImages[i]
            val sizeList = listImages.size
            saveImagesStorage(urlImage, sizeList, i)
        }
    }

    private fun configDialog() {
        // ✅ Using standard ProgressDialog instead of SpotsDialog
        dialog = ProgressDialog(this)
        dialog.setMessage("Saving ad...")
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun configAd(): Ad {
        val title = binding.editTitle.text.toString()
        val description = binding.editDescription.text.toString()
        val phone = binding.editPhone.text.toString()
        val value = binding.editValue.text.toString()
        val rawValue = binding.editValue.rawValue.toString()
        val state = binding.stateSpinner.selectedItem.toString()
        val category = binding.categoriesSpinner.selectedItem.toString()
        val reshapeTitle = title.replace(" ", "_")
        ad = Ad()
        ad.title = title
        ad.description = description
        ad.phone = phone
        ad.value = value
        ad.state = state
        ad.category = category
        ad.id = "${loggedUser!!.uid}_${rawValue}_${reshapeTitle}"
        return ad
    }

    private fun validateAdFields() {
        ad = configAd()
        val rawValue = binding.editValue.rawValue.toString()
        if (listImages.isNotEmpty()) {
            if (ad.title.isNotEmpty()) {
                if (ad.description.isNotEmpty()) {
                    if (ad.phone.isNotEmpty()) {
                        if (rawValue.isNotEmpty() && rawValue != "0") {
                            if (ad.state.isNotEmpty()) {
                                if (ad.category.isNotEmpty()) {
                                    saveAd()
                                } else {
                                    Toast.makeText(this, "Please choose the ad category", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Please choose your state", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Value cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please choose at least one image for your ad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configSpinners() {
        val states: Array<out String> = resources.getStringArray(R.array.states)
        val adapterStates: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        adapterStates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = adapterStates

        val categories: Array<out String> = resources.getStringArray(R.array.category)
        val adapterCategories: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoriesSpinner.adapter = adapterCategories
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            alertDialogPermission()
        }
    }
}