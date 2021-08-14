package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.final1.Extensions.toast
import com.example.final1.FirebaseUtils.firebaseAuth
import kotlinx.android.synthetic.main.activity_setting_and_privacy.*

class Setting_and_Privacy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_and_privacy)

        to_logout_btn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            toast("signed out")
            finish()
        }
    }
}