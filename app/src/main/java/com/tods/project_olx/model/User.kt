package com.tods.project_olx.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

data class User(
    var name: String = "",
    var email: String = "",
    var id: String = "",
    var password: String = ""){
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    fun configCurrentUser(): FirebaseUser? {
        auth = Firebase.auth
        return auth.currentUser
    }

    fun save(){
        val user = User(name, email, id)
        user.id = configCurrentUser()!!.uid
        database = FirebaseDatabase.getInstance().getReference("users")
        database.child(configCurrentUser()!!.uid).setValue(user)
    }
}