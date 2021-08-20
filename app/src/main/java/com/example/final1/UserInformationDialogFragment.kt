package com.example.final1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_information_fragment.*
import kotlinx.android.synthetic.main.user_information_fragment.view.*

class UserInformationDialogFragment : DialogFragment() {
    private var check:Boolean = true
    var database: DatabaseReference = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")
    val firebasestorage = FirebaseStorage.getInstance("gs://project-test-8dbf1.appspot.com").getReference("root")

    companion object {
        fun newInstance(uid : String): UserInformationDialogFragment {
            val args = Bundle()
            args.putString("uid", uid)
            val fragment = UserInformationDialogFragment()
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
        var rootView: View = inflater.inflate(R.layout.user_information_fragment,container,false)
        database.child("Users/$useruid/userName").get().addOnSuccessListener {
            rootView.user_name_memberinf.text = it.value.toString()
            rootView.username_edt.hint = it.value.toString()
        }
        database.child("Users/$useruid/userEmail").get().addOnSuccessListener {
            rootView.usermail.text = it.value.toString()
        }
        database.child("Users/$useruid/userPhone").get().addOnSuccessListener {
            rootView.userphone.hint = it.value.toString()
        }
        firebasestorage.child("Userimg").child(useruid).downloadUrl.addOnSuccessListener { it2 ->
            if (it2 != null) {
                Picasso.get().load(it2).into(user_pic_memberinf)
                select_photo_memberinf.alpha = 0f
            }
        }.addOnFailureListener {
        }

        rootView.select_photo_memberinf.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        rootView.confirm_edt_btn.setOnClickListener {
            val edtusername = rootView.username_edt.text.toString()
            val edtuserphone = rootView.userphone.text.toString()

            if (edtusername != "") {
                database.child("Users/$useruid/userName").setValue(edtusername)
            }
            if (edtuserphone != "") {
                database.child("Users/$useruid/userPhone").setValue(edtuserphone)
            }
            uploadImgtoFirebaseStorage()
        }

        return rootView
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedPhotoUri)
            user_pic_memberinf.setImageBitmap(bitmap)
            select_photo_memberinf.alpha = 0f
        }
    }

    private fun uploadImgtoFirebaseStorage() {
        if (selectedPhotoUri == null) return

        else {
            val userid = FirebaseAuth.getInstance().uid
            val firebasestorage = FirebaseStorage.getInstance("gs://project-test-8dbf1.appspot.com").getReference("root")

            firebasestorage.child("Userimg").child(userid!!).putFile(selectedPhotoUri!!)
        }
    }




}