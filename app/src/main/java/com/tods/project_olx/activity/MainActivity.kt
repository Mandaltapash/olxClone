package com.tods.project_olx.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.databinding.ActivityMainBinding
import com.tods.project_olx.helper.RecyclerItemClickListener
import com.tods.project_olx.model.Ad
import dmax.dialog.SpotsDialog

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerPublicAds: RecyclerView
    private lateinit var adRef: DatabaseReference
    private lateinit var dialog: AlertDialog
    private var auth: FirebaseAuth = Firebase.auth
    private var adList: MutableList<Ad> = ArrayList<Ad>()
    private var adapterAd: AdapterAd = AdapterAd(adList)
    private var filterRegion: String = ""
    private var filterCategory: String = ""
    private var filteredByState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configViewBinding()
        configRecyclerView()
        recoverPublicAds()
        configButtonRegionClickListener()
        configButtonCategoryClickListener()
        configRecyclerViewItemClickListener()
    }

    private fun configRecyclerViewItemClickListener() {
        binding.recyclerPublicAds.addOnItemTouchListener(RecyclerItemClickListener
            (this, recyclerPublicAds, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedAd: Ad = adList[position]
                val intent: Intent = Intent(applicationContext, AdDetailsActivity::class.java)
                intent.putExtra("selectedAd", selectedAd)
                startActivity(intent)
            }

            override fun onItemLongClick(view: View?, position: Int) {
                null
            }
        })
        )
    }

    private fun configButtonCategoryClickListener() {
        binding.buttonCategory.setOnClickListener(View.OnClickListener {
            filterByCategory()
        })
    }

    private fun filterByCategory(){
        if (filteredByState){
            val viewSpinner: View = layoutInflater.inflate(R.layout.custom_dialog_spinner, null)
            val spinnerCategory: Spinner = viewSpinner.findViewById(R.id.spinnerFilter)
            val categories: Array<out String> = resources.getStringArray(R.array.category)
            val adapterCategories: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapterCategories
            val dialogState: AlertDialog.Builder = AlertDialog.Builder(this)
            dialogState.setTitle("Select the category")
            dialogState.setView(viewSpinner)
            dialogState.setPositiveButton("Filter") { _, _ ->
                filterCategory = spinnerCategory.selectedItem.toString()
                recoverAdsByCategory()
            }
            dialogState.setNeutralButton("Reset filter"){ _, _ ->
                recoverPublicAds()
                binding.buttonRegion.text = resources.getString(R.string.region)
                binding.buttonCategory.text = resources.getString(R.string.category)
            }
            dialogState.setNegativeButton("Cancel") { _, _ ->

            }
            val dialog: AlertDialog = dialogState.create()
            dialog.show()
        } else {
            Toast.makeText(this, "Please select your region first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recoverAdsByCategory(){
        configDialog()
        adRef = FirebaseDatabase.getInstance()
            .getReference("adds")
            .child(filterRegion)
            .child(filterCategory)
        adRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adList.clear()
                for (ads: DataSnapshot in snapshot.children){
                    val ad: Ad = ads.getValue(Ad::class.java)!!
                    adList.add(ad)
                }
                adList.reverse()
                adapterAd.notifyDataSetChanged()
                dialog.dismiss()
                binding.buttonCategory.text = filterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    private fun configButtonRegionClickListener() {
        binding.buttonRegion.setOnClickListener(View.OnClickListener {
            filterByRegion()
        })
    }

    private fun filterByRegion(){
        val viewSpinner: View = layoutInflater.inflate(R.layout.custom_dialog_spinner, null)
        val spinnerState: Spinner = viewSpinner.findViewById(R.id.spinnerFilter)
        val states: Array<out String> = resources.getStringArray(R.array.states)
        val adapterStates: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, states)
        adapterStates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerState.adapter = adapterStates
        val dialogState: AlertDialog.Builder = AlertDialog.Builder(this)
        dialogState.setTitle("Select your region")
        dialogState.setView(viewSpinner)
        dialogState.setPositiveButton("Filter") { _, _ ->
            filterRegion = spinnerState.selectedItem.toString()
            recoverAdsByRegion()
            filteredByState = true
        }
        dialogState.setNeutralButton("Reset filter"){ _, _ ->
            recoverPublicAds()
            binding.buttonRegion.text = resources.getString(R.string.region)
            binding.buttonCategory.text = resources.getString(R.string.category)
        }
        dialogState.setNegativeButton("Cancel") { _, _ ->

        }
        val dialog: AlertDialog = dialogState.create()
        dialog.show()
    }

    private fun recoverAdsByRegion(){
        configDialog()
        adRef = FirebaseDatabase.getInstance()
            .getReference("adds")
            .child(filterRegion)
        adRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adList.clear()
                for (category: DataSnapshot in snapshot.children){
                    for (ads: DataSnapshot in category.children){
                        val ad: Ad = ads.getValue(Ad::class.java)!!
                        adList.add(ad)
                    }
                }
                adList.reverse()
                adapterAd.notifyDataSetChanged()
                dialog.dismiss()
                binding.buttonRegion.text = filterRegion
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }

        })
    }

    private fun recoverPublicAds(){
        configDialog()
        adList.clear()
        adRef = FirebaseDatabase.getInstance()
            .getReference("adds")
        adRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (state: DataSnapshot in snapshot.children){
                    for (category: DataSnapshot in state.children){
                        for (ads: DataSnapshot in category.children){
                            val ad: Ad = ads.getValue(Ad::class.java)!!
                            adList.add(ad)
                        }
                    }
                }
                adList.reverse()
                adapterAd.notifyDataSetChanged()
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun configDialog() {
        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Recovering ads")
            .setCancelable(false)
            .build()
        dialog.show()
    }

    private fun configRecyclerView() {
        recyclerPublicAds = binding.recyclerPublicAds
        recyclerPublicAds.layoutManager = LinearLayoutManager(this)
        recyclerPublicAds.setHasFixedSize(true)
        recyclerPublicAds.adapter = adapterAd
    }

    private fun configViewBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.menu_log_in -> {
                val intent: Intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_log_off -> {
                auth.signOut()
                invalidateOptionsMenu()
                true
            }
            R.id.menu_register -> {
                val intent: Intent = Intent(applicationContext, RegisterActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_my_adds -> {
                val intent: Intent = Intent(applicationContext, MyAdsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (auth.currentUser == null){
            menu!!.setGroupVisible(R.id.group_logged_out, true)
        } else {
            menu!!.setGroupVisible(R.id.group_logged_in, true)
            menu.setGroupVisible(R.id.group_logged_out, false)
        }
        return super.onPrepareOptionsMenu(menu)
    }
}