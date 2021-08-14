package com.example.final1

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.final1.Extensions.toast
import com.example.final1.FirebaseUtils.firebaseAuth
import com.example.final1.FirebaseUtils.firebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    lateinit var userEmail: String
    lateinit var userPassword: String
    lateinit var userName: String
    lateinit var userPhone: String
    lateinit var currentGroup: String
    lateinit var createAccountInputsArray: Array<EditText>
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")

        setContentView(R.layout.activity_register)
        createAccountInputsArray = arrayOf(account_name, account_id, passwords, passwords_check, phone_num)
        button_confirm_register.setOnClickListener {
            signIn()
        }

        button_to_login.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            toast("please sign into your account")
            finish()
        }

    }

    /* check if there's a signed-in user*/

    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            startActivity(Intent(this, HomeActivity::class.java))
            toast("welcome back")
        }
    }

    private fun notEmpty(): Boolean = account_id.text.toString().trim().isNotEmpty() &&
            passwords.text.toString().trim().isNotEmpty() &&
            passwords_check.text.toString().trim().isNotEmpty() &&
            account_name.toString().trim().isNotEmpty() &&
            phone_num.toString().trim().isNotEmpty()

    private fun identicalPassword(): Boolean {
        var identical = false
        if (notEmpty() &&
            passwords.text.toString().trim() == passwords_check.text.toString().trim()
        ) {
            identical = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        } else {
            toast("passwords are not matching !")
        }
        return identical
    }

    private fun signIn() {
        if (identicalPassword()) {
            // identicalPassword() returns true only  when inputs are not empty and passwords are identical
            userEmail = account_id.text.toString().trim()
            userPassword = passwords.text.toString().trim()
            userName = account_name.text.toString().trim()
            userPhone = phone_num.text.toString().trim()
            currentGroup = "not_have_yet".trim()
            val User = User(userEmail, userName, userPhone, currentGroup)

            /*create a user*/
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = FirebaseAuth.getInstance().uid
                        if (uid != null) {
                            database.child("Users").child(uid).setValue(User)
                        }
                        toast("created account successfully !")
                        sendEmailVerification()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        toast("failed to Authenticate !")
                    }
                }
        }
    }

    /* send verification email to the new user. This will only
    *  work if the firebase user is not null.
    */

    private fun sendEmailVerification() {
        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("email sent to $userEmail")
                }
            }
        }
    }
}