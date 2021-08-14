package com.example.final1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.final1.R
import com.google.zxing.WriterException

import com.google.zxing.BarcodeFormat

import android.graphics.Bitmap

import com.journeyapps.barcodescanner.BarcodeEncoder
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView


class QRcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.final1.R.layout.activity_qrcode)

        var text=intent.getStringExtra("current_group")
        var textview:TextView=findViewById(R.id.QRtext)
        textview.text = text

        val ivCode: ImageView = findViewById(com.example.final1.R.id.imageView)
        val encoder = BarcodeEncoder()
        try {
            val bit = encoder.encodeBitmap(
                text, BarcodeFormat.QR_CODE, 500, 500
            )
            ivCode.setImageBitmap(bit)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}