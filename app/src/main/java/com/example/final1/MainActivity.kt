package com.example.final1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.final1.Extensions.toast
import com.example.final1.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val login_btn : Button = findViewById(R.id.login_btn)
        val regisbtn : Button = findViewById(R.id.register_btn)

        signInInputsArray = arrayOf(account_id_login, password_login)

        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            startActivity(Intent(this, GroupInformationActivity::class.java))
            finish()
        }

        regisbtn.setOnClickListener{
            val intent = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        Log.d("test", "This is Verbosdtfyntre.")
        login_btn.setOnClickListener {
            signInUser()
        }
    }
    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    private fun signInUser() {
        signInEmail = account_id_login.text.toString().trim()
        signInPassword = password_login.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(this, GroupInformationActivity::class.java))
                        toast("signed in successfully")
                        finish()
                    } else {
                        toast("sign in failed")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }
}