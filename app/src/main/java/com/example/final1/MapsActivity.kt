package com.example.final1

import android.Manifest
import android.location.Location
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.LocationListener
import android.location.LocationManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_group_information.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback ,LocationListener ,LocationSource{

    private lateinit var mMap: GoogleMap
    private var REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION=100 //定義權限編號
    private lateinit var mLocationMgr :LocationManager
    private lateinit var mLocationChangedListener: LocationSource.OnLocationChangedListener
    private lateinit var mylocationtxt : String
    lateinit var database: DatabaseReference //gan->firebase
    lateinit var current_group : String //gan->firebase


    //測試權限
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<out String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                checkLocationPermissionAndEnableIt(true)
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mLocationMgr = getSystemService(LOCATION_SERVICE) as LocationManager//開一個location
        database = FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("root")//database get 路徑


        // 建立SupportMapFragment，並且設定Map的callback
        val supportMapFragment = SupportMapFragment()
        supportMapFragment.getMapAsync(this)

        // 把SupportMapFragment放到介面佈局檔裡頭的FrameLayout顯示。
        val m=supportFragmentManager.beginTransaction()
        m.replace(R.id.map, supportMapFragment).commit()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled=true
        mMap.setLocationSource(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
                var location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(location==null){
                location=mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if(location!=null){
                Toast.makeText(this@MapsActivity,"取得了一次定位",Toast.LENGTH_LONG).show()
                onLocationChanged(location)
            }else{
                Toast.makeText(this@MapsActivity,"沒有定位資料",Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun onLocationChanged(location: Location) {
        mLocationChangedListener.onLocationChanged(location)//抓之前的位置
        mMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude,location.longitude)))//可能要改!!!移動鏡頭到新位置

        val la=location.latitude
        val lo=location.longitude


        //設定經緯度顯示

        //-------------------------firebase----------------------------------
        val userid = FirebaseAuth.getInstance().uid //取得現在的user的uid

        //拿User的current_group
        database.child("Users").child(userid.toString()).child("currentGroup").get().addOnSuccessListener {
            current_group = (it.value as String?)!!
            //如果沒有的話，跳轉到GroupActivity裡加入Group或切換
            if (current_group == "not_have_yet") {
                val intent = Intent(this, GroupActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                if (userid != null) {
                    database.child("Chats").child(current_group).child("Useruid").child(userid).child("la").setValue(la)
                    database.child("Chats").child(current_group).child("Useruid").child(userid).child("lo").setValue(lo)
                }
            }
        }.addOnFailureListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
        //-------------------------firebase----------------------------------

        mylocationtxt="我的經度是"+lo+"，\n我的緯度是:"+la
        val mylocationinfo : TextView =findViewById(R.id.mylocationinfo)
        mylocationinfo.setText(mylocationtxt)

        database.child("Users").child(userid.toString()).child("currentGroup").get().addOnSuccessListener {
            current_group = it.value as String
            if (current_group == "not_have_yet") {
                val intent = Intent(this, GroupActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                database.child("Chats").child(current_group).child("Useruid").addValueEventListener(object:
                    ValueEventListener {
                    override fun onDataChange(datasnapshot: DataSnapshot) {
                        val postSnapshot = datasnapshot.children
                        mMap.clear()
                        for (item in postSnapshot) {
                            var useruid = item.key
                            if (useruid != null) {
                                database.child("Chats").child(current_group).child("Useruid").child(useruid).get().addOnSuccessListener { datasnapshot ->
                                    var positionlalo = datasnapshot.getValue<positionlalo>()
                                    if (positionlalo != null) {
                                        mMap.addMarker(MarkerOptions().position(LatLng(positionlalo.la, positionlalo.lo)).title(useruid))
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }
        }.addOnFailureListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }


    }

    override fun activate(p0: LocationSource.OnLocationChangedListener) {
        mLocationChangedListener = p0
        checkLocationPermissionAndEnableIt(true)
        Toast.makeText(this@MapsActivity,"地圖塗層啟用",Toast.LENGTH_LONG).show()
    }

    override fun deactivate() {
        checkLocationPermissionAndEnableIt(false)
        Toast.makeText(this@MapsActivity,"地圖的my location layer關掉了",Toast.LENGTH_LONG).show()

    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissionAndEnableIt(true)
    }

    override fun onStop(){
        super.onStop()
        checkLocationPermissionAndEnableIt(false)//停止定位
    }

    private fun checkLocationPermissionAndEnableIt(on: Boolean) {
        if(ContextCompat.checkSelfPermission(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //這功能沒有使用者同意
            if(ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                val listener = DialogInterface.OnClickListener { dialog, which ->
                    //答覆後執行onRequestPermissionsResult()
                    ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ),REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION)
                }
                val altDlgBuilde =AlertDialog.Builder(this)
                altDlgBuilde.setTitle("提示")
                altDlgBuilde.setMessage("你沒有啟動Chital的定位功能")
                altDlgBuilde.setIcon(android.R.drawable.ic_dialog_info)
                altDlgBuilde.setCancelable(false)
                altDlgBuilde.setPositiveButton("確定",listener)
                altDlgBuilde.show()
                return
            }else{
                //答覆後執行onRequestPermissionsResult()
                ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                    ),REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION)

            }
        }

        //已經有使用者同意
        if(on){
            if(mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,500L,5f,this)
                Toast.makeText(this@MapsActivity,"用GPS定位",Toast.LENGTH_LONG).show()
            }else{
                if(mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    mLocationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500L,5f,this)
                    Toast.makeText(this@MapsActivity,"用網路定位",Toast.LENGTH_LONG).show()
                }
            }
        }else{
            mLocationMgr.removeUpdates(this)
            Toast.makeText(this@MapsActivity,"定位已經停用了",Toast.LENGTH_LONG).show()
        }


    }



}

data class positionlalo(val la: Double, val lo: Double) {
constructor() : this   (-1.0,-1.0)
}