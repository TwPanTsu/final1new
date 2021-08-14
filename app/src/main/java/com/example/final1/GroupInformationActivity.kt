package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group_information.*
import kotlinx.android.synthetic.main.member.view.*

class GroupInformationActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    lateinit var current_group : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_information)
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")

        val userid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<GroupieViewHolder>()


        var userkeyingrouplist:MutableList<String> = mutableListOf()
        database.child("Users").child(userid.toString()).child("currentGroup").get().addOnSuccessListener {
            current_group = it.value as String
            if (current_group == "not_have_yet") {
                val intent = Intent(this, GroupActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                database.child("Chats").child(current_group).child("GroupName").get().addOnSuccessListener {
                    groupname_grpinf.text = it.value as CharSequence?
                }
                database.child("Chats").child(current_group).child("Useruid").addValueEventListener(object:
                    ValueEventListener {
                    override fun onDataChange(datasnapshot: DataSnapshot) {
                        adapter.clear()
                        val postSnapshot = datasnapshot.children
                        for (item in postSnapshot) {
                            userkeyingrouplist.add(item.key.toString())
                            database.child("Users").child(item.key.toString()).child("userName").get().addOnSuccessListener {
                                adapter.add(memberlistItem(it.value as String))
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
                recyclerView_members.adapter = adapter
            }
        }.addOnFailureListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }


        returnmenu_btn_groupinf.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        switch_groupbtn.setOnClickListener{
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }

        chatroom_btn_grpinf.setOnClickListener{
            val intent = Intent(this, ChatroomActivity::class.java)
            startActivity(intent)
        }


    }
}

class memberlistItem(val name: String) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.member

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.username.text = name
    }
}