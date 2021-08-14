package com.example.final1

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chatroom.*
import kotlinx.android.synthetic.main.messagelist.view.*
import kotlinx.android.synthetic.main.messagelist_other.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatroomActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    lateinit var current_group : String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        val userid = FirebaseAuth.getInstance().uid
        val adapter = GroupAdapter<GroupieViewHolder>()
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")

        send_btn.setOnClickListener {
            sendmsg()
            et_message.text.clear()
        }

        //從資料庫拿訊息
        database.child("Users").child(userid.toString()).child("currentGroup").get().addOnSuccessListener {
            current_group = it.value as String
            if (current_group == "not_have_yet") {
                val intent = Intent(this, GroupActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                database.child("Chats").child(current_group).child("GroupName").get().addOnSuccessListener {
                    chatroom_name.text = it.value as CharSequence?
                }
                database.child("Chats").child(current_group).child("Messagelist").addChildEventListener(object: ChildEventListener {
                    override fun onChildAdded(datasnapshot: DataSnapshot, previousChildName: String?) {
                        var messagepack = datasnapshot.getValue<messagepack>()
                        if (messagepack != null) {
                            var userkey = messagepack.userKey
                            if (userid == userkey) {
                                adapter.add(memberUserItem(messagepack.messageText, messagepack.currentTime))
                            }
                            else {
                                adapter.add(memberOtherItem(messagepack.userName, messagepack.messageText, messagepack.currentTime))
                            }
                        }
                        if (messagepack != null) {
                            Log.d(TAG, messagepack.messageText!!)
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }.addOnFailureListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
        //有變化捲到最下方
        recyclerview_chatroom.viewTreeObserver.addOnGlobalLayoutListener { (adapter.itemCount - 1).takeIf { it > 0 }?.let(recyclerview_chatroom::smoothScrollToPosition) }
        recyclerview_chatroom.adapter = adapter


        map_btn.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        returnmenu_btn_chatroom.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        group_btn.setOnClickListener {
            val intent = Intent(this, GroupInformationActivity::class.java)
            startActivity(intent)
            finish()
        }



    }

    private fun notEmpty(): Boolean = et_message.text.toString().isNotEmpty()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendmsg() {
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")
        val userid = FirebaseAuth.getInstance().uid
        var msg = et_message.text.toString().trim()
        var time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d H:m"))
        lateinit var username : String
        lateinit var current_group : String


        if(notEmpty()) {
            if (userid != null) {
                database.child("Users").child(userid).child("currentGroup").get().addOnSuccessListener {
                    current_group = it.value as String
                    database.child("Users").child(userid).child("userName").get().addOnSuccessListener {
                        username = it.value as String
                        var messagekey = database.child("Chats").child(current_group).child("Messagelist").push().key
                        if (messagekey != null) {
                            val messagepack = messagepack(userid, username, msg, time)
                            database.child("Chats").child(current_group).child("Messagelist").child(messagekey).setValue(messagepack)
                        }
                    }
                }
            }
        }

    }
}


class memberOtherItem(val txv_user_other: String?, val txv_msg_other: String?, val txv_time_other: String?) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.messagelist_other

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txv_user_other.text = txv_user_other
        viewHolder.itemView.txv_msg_other.text = txv_msg_other
        viewHolder.itemView.txv_time_other.text = txv_time_other
    }
}

class memberUserItem(val txv_msg_user: String?, val txv_time_user: String?) : Item<GroupieViewHolder>() {
    override fun getLayout() = R.layout.messagelist

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txv_msg_user.text = txv_msg_user
        viewHolder.itemView.txv_time_user.text = txv_time_user
    }
}

data class messagepack(val userKey: String? = "", val userName: String? = "", val messageText: String? = "", val currentTime: String? = "") {
}
