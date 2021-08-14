package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.final1.Extensions.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_group.*
import kotlinx.android.synthetic.main.activity_group_information.*
import kotlinx.android.synthetic.main.grouplist.*
import kotlinx.android.synthetic.main.grouplist.view.*

class GroupActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")

        //adapter設定
        val userid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<GroupieViewHolder>()
        var grpkeylist:MutableList<String> = mutableListOf()
        //為資料庫中的group增加進adapter
        //abc
        database.child("Users").child(userid.toString()).child("Groups").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                adapter.clear()
                val postSnapshot = datasnapshot.children
                for (item in postSnapshot) {
                    grpkeylist.add(item.key.toString())
                    database.child("Chats").child(item.key.toString()).child("GroupName").get().addOnSuccessListener {
                        adapter.add(GroupItem(it.value as String))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        recyclerview_group.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val positionofitem = adapter.getAdapterPosition(item)
            database.child("Users").child(userid.toString()).child("currentGroup").setValue(grpkeylist[positionofitem])
        }

        returnGroupInf_btn.setOnClickListener{
            val intent = Intent(this, GroupInformationActivity::class.java)
            startActivity(intent)
        }

        creategroup_btn.setOnClickListener{

            val userid = FirebaseAuth.getInstance().uid
            var chatkey = database.child("Chats").push().key
            //從資料庫獲得username
            database.child("Users").child(userid!!).child("userName").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val username = snapshot.getValue<String>()
                    // 資料庫Chats下增加成員
                    database.child("Chats").child(chatkey!!).child("Useruid").child(userid.toString()).setValue(username)
                    // Chats給予預設名稱
                    database.child("Chats").child(chatkey).child("GroupName").setValue("未命名聊天室")
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
            //Users底下增加group的key
            database.child("Users").child(userid!!).child("Groups").child(chatkey!!).setValue("True").addOnSuccessListener {
                toast("create group successful")
            }
        }

        joingroup_btn.setOnClickListener{
            val intent = Intent(this, GroupJoinActivity::class.java)
            startActivity(intent)
        }

        setting_btn.setOnClickListener{
            val intent = Intent(this, Setting_and_Privacy::class.java)
            startActivity(intent)
        }

        setting_btn.setOnClickListener{
            val intent = Intent(this, Setting_and_Privacy::class.java)
            startActivity(intent)
        }


    }
}


class GroupItem(val name: String) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.grouplist

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.groupname.text = name
    }
}

