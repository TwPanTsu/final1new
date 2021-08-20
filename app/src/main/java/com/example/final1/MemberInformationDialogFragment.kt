package com.example.final1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.member_information_fragment.*
import kotlinx.android.synthetic.main.member_information_fragment.view.*

class MemberInformationDialogFragment : DialogFragment() {
    private var check:Boolean = true
    var database: DatabaseReference = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")
    val firebasestorage = FirebaseStorage.getInstance("gs://project-test-8dbf1.appspot.com").getReference("root")

    companion object {
        fun newInstance(uid : String): MemberInformationDialogFragment {
            var useruid = uid
            val args = Bundle()
            args.putString("uid", uid)
            val fragment = MemberInformationDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val useruid = arguments?.get("uid").toString()
        var rootView: View = inflater.inflate(R.layout.member_information_fragment,container,false)
        database.child("Users/$useruid/userName").get().addOnSuccessListener {
            rootView.member_name_memberinf.text = it.value.toString()
        }
        database.child("Users/$useruid/userEmail").get().addOnSuccessListener {
            rootView.membermail.text = it.value.toString()
        }
        database.child("Users/$useruid/userPhone").get().addOnSuccessListener {
            rootView.memberphone.text = it.value.toString()
        }
        firebasestorage.child("Userimg").child(useruid).downloadUrl.addOnSuccessListener { it2 ->
            if (it2 == null) {
                firebasestorage.child("Userimg").child("default_user_img.png").downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it).into(member_pic_memberinf)
                }
            }
            else Picasso.get().load(it2).into(member_pic_memberinf)
        }.addOnFailureListener {
            firebasestorage.child("Userimg").child("default_user_img.png").downloadUrl.addOnSuccessListener {
                Picasso.get().load(it).into(member_pic_memberinf)
            }
        }


        return rootView
    }


}