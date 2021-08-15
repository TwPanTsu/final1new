package com.example.final1

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.final1.Extensions.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.create_group_fragment.*
import kotlinx.android.synthetic.main.create_group_fragment.view.*
import kotlinx.android.synthetic.main.fragment_group_information.*
import kotlinx.android.synthetic.main.grouplist.*


class CreateGroupDialogFragment : DialogFragment() {

    lateinit var myname:String
    private var check:Boolean = true
    var database: DatabaseReference= FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView:View = inflater.inflate(R.layout.create_group_fragment,container,false)

        rootView.create_btn.setOnClickListener {
            myname=groupname_edit.text.toString().trim()
            check=false

            if(myname.isEmpty()){
                Toast.makeText(context,"請輸入名稱!",Toast.LENGTH_LONG).show()
            }else{
                val userid = FirebaseAuth.getInstance().uid
                //從資料庫獲得username
                database.child("Users").child(userid!!).child("userName").get().addOnSuccessListener {

                    var chatkey = database.child("Chats").push().key
                    val username = it.getValue<String>()
                    // 資料庫Chats下增加成員
                    database.child("Chats").child(chatkey!!).child("Useruid").child(userid.toString()).setValue(username)
                    // Chats給予預設名稱

                    database.child("Chats").child(chatkey).child("GroupName").setValue(myname)
                    //Users底下增加group的key
                    database.child("Users").child(userid!!).child("Groups").child(chatkey!!).setValue("True").addOnSuccessListener {
                        Toast.makeText(context,"新增成功", Toast.LENGTH_LONG).show()
                        dismiss()
                    }
                }

            }
        }
        rootView.cancel_btn.setOnClickListener {
            Toast.makeText(context,check.toString(),Toast.LENGTH_LONG).show()
            dismiss()
        }
        return rootView
    }

    fun GetGroupname(): String {
        return myname
    }
    fun checkcancel():Boolean{
        return check
    }

}