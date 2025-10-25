package com.tods.project_olx.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityRegisterBinding
import com.tods.project_olx.model.User
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        auth = Firebase.auth
        configViewBinding()
        configButtonRegisterClickListener()
    }

    private fun configButtonRegisterClickListener() {
        binding.buttonRegister.setOnClickListener(View.OnClickListener {
            validate()
        })
    }

    private fun configViewBinding() {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun register(user: User){
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    user.save()
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    try {
                        it.exception
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(this, "Please use a stronger password!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this, "Please use a valid email!", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "E-mail already in use!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("REGISTER", "INVALID REGISTRATION: ${e.message}")
                    }
                }
            }
    }

    private fun validate(){
        val name = binding.editRegisterName.text.toString()
        val email = binding.editRegisterEmail.text.toString()
        val password = binding.editRegisterPassword.text.toString()
        val confirmPassword = binding.editRegisterConfirmPassword.text.toString()
        if (name.isNotEmpty()){
            if (email.isNotEmpty()){
                if (password.isNotEmpty()){
                    if (confirmPassword.isNotEmpty()){
                        if (password == confirmPassword){
                            val user = User()
                            user.name = name
                            user.email = email
                            user.password = password
                            register(user)
                        } else {
                            Toast.makeText(this, "Passwords doesn't match!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Field 'Confirm password' cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Field 'Password' cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Field 'E-mail' cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Field 'Name' cannot be empty!", Toast.LENGTH_SHORT).show()
        }
    }
}