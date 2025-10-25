package com.tods.project_olx.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.tods.project_olx.R
import com.tods.project_olx.adapter.AdAdapter
import com.tods.project_olx.adapter.CategoryAdapter
import com.tods.project_olx.databinding.ActivityMainBinding
import com.tods.project_olx.model.Category
import com.tods.project_olx.utils.Resource
import com.tods.project_olx.viewmodel.AdViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var adAdapter: AdAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var selectedRegion: String? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupCategories()
        setupAdsRecyclerView()
        setupBottomNav()
        setupSearchBar()
        observeViewModel()

        // Load initial ads
        loadAds()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupCategories() {
        val categories = listOf(
            Category("Furniture", R.drawable.ic_menu_gallery),
            Category("Electronics", R.drawable.ic_menu_camera),
            Category("Fashion", R.drawable.ic_menu_compass),
            Category("Books", R.drawable.ic_menu_agenda),
            Category("Sports", R.drawable.ic_menu_manage),
            Category("Free Zone", R.drawable.ic_menu_send),
            Category("Other", R.drawable.ic_menu_help)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            selectedCategory = category.name
            loadAds()
        }

        binding.recyclerCategories.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = categoryAdapter
        }
    }

    private fun setupAdsRecyclerView() {
        adAdapter = AdAdapter { ad ->
            val intent = Intent(this, AdDetailsActivity::class.java).apply {
                putExtra("selectedAd", ad)
            }
            startActivity(intent)
        }

        // Check if recyclerAds exists in layout
        binding.recyclerAds?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adAdapter
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    selectedCategory = null
                    selectedRegion = null
                    loadAds()
                    true
                }
                R.id.nav_chats -> {
                    Toast.makeText(this, "Chats feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_sell -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this, RegisterAddActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_ads -> {
                    if (auth.currentUser != null) {
                        startActivity(Intent(this, MyAdsActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    true
                }
                R.id.nav_account -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "Profile: ${auth.currentUser?.email}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.setOnEditorActionListener { textView, _, _ ->
            val query = textView.text.toString()
            if (query.isNotEmpty()) {
                searchAds(query)
            }
            true
        }
    }

    private fun loadAds() {
        adViewModel.fetchAds(selectedRegion, selectedCategory)
    }

    private fun searchAds(query: String) {
        lifecycleScope.launch {
            adViewModel.ads.value?.let { result ->
                when (result) {
                    is Resource.Success -> {
                        val filtered = result.data?.filter { ad ->
                            ad.title.contains(query, ignoreCase = true) ||
                                    ad.description.contains(query, ignoreCase = true)
                        }
                        adAdapter.submitList(filtered)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeViewModel() {
        adViewModel.ads.observe(this) { result ->
            when (result) {
                is Resource.Success -> {
                    adAdapter.submitList(result.data)
                    binding.emptyStateView?.isVisible = result.data?.isEmpty() == true
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    // Handle loading
                }
            }
        }

        adViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar?.isVisible = isLoading
        }

        adViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                adViewModel.clearError()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.custom_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val isLoggedIn = auth.currentUser != null
        menu?.setGroupVisible(R.id.group_logged_in, isLoggedIn)
        menu?.setGroupVisible(R.id.group_logged_out, !isLoggedIn)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_log_in -> {
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            R.id.menu_log_off -> {
                auth.signOut()
                invalidateOptionsMenu()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_register -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                true
            }
            R.id.menu_my_adds -> {
                startActivity(Intent(this, MyAdsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }
}