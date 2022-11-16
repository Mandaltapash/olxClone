package com.tods.project_olx.model

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.Serializable

data class Ad(
    var id: String = "",
    var state: String = "",
    var category: String = "",
    var title: String = "",
    var description: String = "",
    var value: String = "",
    var phone: String = "",
    var adImages: List<String> = mutableListOf()): Serializable{
    private lateinit var database: DatabaseReference


    fun save(){
        database = FirebaseDatabase.getInstance().getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
            .child(id)
        database.setValue(this)
        savePublic()
    }

    private fun savePublic(){
        database = FirebaseDatabase.getInstance().getReference("adds")
            .child(state)
            .child(category)
            .child(id)
        database.setValue(this)
    }

    fun remove(){
        database = FirebaseDatabase.getInstance().getReference("my_adds")
            .child(User().configCurrentUser()!!.uid.toString())
            .child(id)
        database.removeValue()
        removePublic()
    }

    private fun removePublic(){
        database = FirebaseDatabase.getInstance().getReference("adds")
            .child(state)
            .child(category)
            .child(id)
        database.removeValue()
    }
}
