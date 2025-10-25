package com.tods.project_olx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityLoginBinding
import com.tods.project_olx.databinding.ActivityRegisterBinding
import com.tods.project_olx.model.User
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        auth = Firebase.auth
        configViewBinding()
        configButtonLoginClickListener()
    }

    private fun configButtonLoginClickListener() {
        binding.buttonLogin.setOnClickListener(View.OnClickListener {
            validate()
        })
    }

    private fun configViewBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun validate() {
        val email = binding.editLoginEmail.text.toString()
        val password = binding.editLoginPassword.text.toString()
        if (email.isNotEmpty()) {
            if (password.isNotEmpty()) {
                val user = User()
                user.email = email
                user.password = password
                login(user)
            } else {
                Toast.makeText(this, "Field 'Password' cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Field 'E-mail' cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(user: User) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    try {
                        it.exception
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Invalid user!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "E-mail and password doesn't match!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("LOGIN", "INVALID LOGIN: ${e.message}")
                    }
                }
            }
    }
}