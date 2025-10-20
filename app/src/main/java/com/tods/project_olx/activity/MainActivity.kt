package com.tods.project_olx.activity

import android.view.LayoutInflater
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityMainBinding
import com.tods.project_olx.databinding.CategoryItemBinding

data class Category(val name: String, val iconResId: Int)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configViewBinding()
        setSupportActionBar(binding.toolbar)

        configCategoriesRecyclerView()
        configBottomNav()
    }

    private fun configBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_chats -> {
                    Toast.makeText(this, "Chats clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_sell -> {
                    startActivity(Intent(applicationContext, RegisterAddActivity::class.java))
                    true
                }
                R.id.nav_my_ads -> {
                    startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun configCategoriesRecyclerView() {
        val categories = listOf(
            Category("Furniture", R.drawable.ic_menu_gallery),
            Category("Electronics", R.drawable.ic_menu_camera),
            Category("Fashion", R.drawable.ic_menu_compass),
            Category("Books", R.drawable.ic_menu_agenda),
            Category("Sports", R.drawable.ic_menu_manage),
            Category("Free Zone", R.drawable.ic_menu_send),
            Category("Other", R.drawable.ic_menu_help)
        )

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 4)
        binding.recyclerCategories.adapter = CategoryAdapter(categories)
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
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                true
            }
            R.id.menu_log_off -> {
                auth.signOut()
                invalidateOptionsMenu()
                true
            }
            R.id.menu_register -> {
                startActivity(Intent(applicationContext, RegisterActivity::class.java))
                true
            }
            R.id.menu_my_adds -> {
                startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (auth.currentUser == null){
            menu!!.setGroupVisible(R.id.group_logged_out, true)
            menu.setGroupVisible(R.id.group_logged_in, false)
        } else {
            menu!!.setGroupVisible(R.id.group_logged_in, true)
            menu.setGroupVisible(R.id.group_logged_out, false)
        }
        return super.onPrepareOptionsMenu(menu)
    }
}

class CategoryAdapter(private val categories: List<Category>) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryName.text = category.name
        holder.binding.categoryIcon.setImageResource(category.iconResId)
    }

    override fun getItemCount() = categories.size
}

