package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.final1.Extensions.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_joingroup.*

class GroupJoinActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joingroup)
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")

        return_group_btn.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
            finish()
        }

        send_groupid_btn.setOnClickListener {
            val userid = FirebaseAuth.getInstance().uid
            var groupid = groupid_input.text.toString().trim()

            database.child("Chats").child(groupid).get().addOnSuccessListener { it1 ->
                if (it1.value == null) {
                    Toast.makeText(this@GroupJoinActivity, "group id wrong", Toast.LENGTH_SHORT).show()
                }
                else {
                    database.child("Users").child(userid.toString()).child("userName").get().addOnSuccessListener {
                        Toast.makeText(this@GroupJoinActivity, "join group success", Toast.LENGTH_SHORT).show()
                        database.child("Chats").child(groupid).child("Useruid").child(userid.toString()).setValue(it.value)
                        database.child("Users").child(userid.toString()).child("Groups").child(groupid).setValue("True")
                        }
                }

            }.addOnFailureListener {
                toast("unexpected wrong")
            }

        }

    }
}