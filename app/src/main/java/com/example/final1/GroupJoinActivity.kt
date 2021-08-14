package com.example.final1

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.final1.Extensions.toast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_joingroup.*
import java.io.IOException

class GroupJoinActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joingroup)
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")
        getPermissionCamera()//測試相機權限 for QR code

        return_group_btn.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
            finish()
        }

        send_groupid_btn.setOnClickListener {
            val userid = FirebaseAuth.getInstance().uid
            var groupid = groupid_input.text.toString().trim()

            database.child("Chats").child(groupid).get().addOnSuccessListener { it1 ->
                if (it1.value == null) {
                    Toast.makeText(this@GroupJoinActivity, "group id wrong", Toast.LENGTH_SHORT).show()
                }
                else {
                    database.child("Users").child(userid.toString()).child("userName").get().addOnSuccessListener {
                        Toast.makeText(this@GroupJoinActivity, "join group success", Toast.LENGTH_SHORT).show()
                        database.child("Chats").child(groupid).child("Useruid").child(userid.toString()).setValue(it.value)
                        database.child("Users").child(userid.toString()).child("Groups").child(groupid).setValue("True")
                        }
                }

            }.addOnFailureListener {
                toast("unexpected wrong")
            }

        }

        //QR code
        var surfaceView: SurfaceView =findViewById(R.id.surfaceView)
        var barcodeDetector: BarcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE).build()
        var cameraSource : CameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(300, 300).build()


        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {//這一大串是他自己跑的，我也不懂啊
        override fun surfaceCreated(@NonNull surfaceHolder: SurfaceHolder) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED
            ) return
            try {
                cameraSource.start(surfaceHolder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
            override fun surfaceChanged(
                @NonNull surfaceHolder: SurfaceHolder,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }
            override fun surfaceDestroyed(@NonNull surfaceHolder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val qrCodes = detections.detectedItems
                if (qrCodes.size() != 0) {
                    groupid_input.post(Runnable { groupid_input.setText(qrCodes.valueAt(0).displayValue )})
                }
            }
        })


    }
    private fun getPermissionCamera(){
        if(ActivityCompat.checkSelfPermission(this@GroupJoinActivity,android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@GroupJoinActivity,
                arrayOf(android.Manifest.permission.CAMERA),1
            )
        }
    }

    }
