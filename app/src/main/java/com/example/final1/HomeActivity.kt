package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_chatroom.*
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val settingbtn : Button = findViewById(R.id.setting_btn)
        //val text :TextView = findViewById(R.id.textView)
        settingbtn.setOnClickListener{
            val intent = Intent(this, Setting_and_Privacy::class.java)
            startActivity(intent)
        }

        to_chatroom_btn.setOnClickListener{
            val intent = Intent(this@HomeActivity, ChatroomActivity::class.java)
            startActivity(intent)
            finish()
        }

        group_btn_home.setOnClickListener{
            val intent = Intent(this, GroupInformationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}