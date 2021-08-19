package com.example.final1

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.final1.Extensions.toast
import com.example.final1.Models.ClusterMarker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_group_information.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.security.Timestamp
import java.time.Instant
import java.time.ZoneId


class MapsActivity : FragmentActivity(), OnMapReadyCallback, LocationListener, LocationSource,ClusterManager.OnClusterClickListener<ClusterMarker>,ClusterManager.OnClusterItemClickListener<ClusterMarker> {

    private lateinit var mMap: GoogleMap
    private var REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION = 100 //定義權限編號
    private lateinit var mLocationMgr: LocationManager
    private lateinit var mLocationChangedListener: LocationSource.OnLocationChangedListener
    lateinit var database: DatabaseReference //gan->firebase
    lateinit var current_group: String //gan->firebase
    private lateinit var mClusterManager:ClusterManager<ClusterMarker>


    //測試權限
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<out String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndEnableIt(true)
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mLocationMgr = getSystemService(LOCATION_SERVICE) as LocationManager//開一個location
        database =
            FirebaseDatabase.getInstance("https://project-test-8dbf1-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("root")//database get 路徑


        // 建立SupportMapFragment，並且設定Map的callback
        val supportMapFragment = SupportMapFragment()
        supportMapFragment.getMapAsync(this)

        // 把SupportMapFragment放到介面佈局檔裡頭的FrameLayout顯示。
        val m = supportFragmentManager.beginTransaction()
        m.replace(R.id.map, supportMapFragment).commit()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.setLocationSource(this)
        mMap.uiSettings.isMapToolbarEnabled = false//allen->按下marker時不會跳出地圖的跳轉扭
        mMap.uiSettings.isZoomControlsEnabled=true//allen->加入地圖縮放按鈕
        //allen->add MapType
        var adapter = ArrayAdapter.createFromResource(
            this,
            R.array.map_type_content,
            android.R.layout.simple_dropdown_item_1line
        )
        spinner_map_type.adapter = adapter
        add_marker_switch.isChecked = false//allen->add marker button set false


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var location = mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location == null) {
                location = mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (location != null) {
                Log.d("test","取得了一次定位")
                onLocationChanged(location)
            } else {
                Log.d("test","沒有定位資料")
            }
        }
        //allen->add MapType setting
        spinner_map_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> mMap.mapType = MAP_TYPE_NORMAL
                    1 -> mMap.mapType = MAP_TYPE_HYBRID
                    2 -> mMap.mapType = MAP_TYPE_SATELLITE
                    else -> toast("error")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                toast("Nothing selected")
            }
        }

        //setting add marker的監聽器
        mMap.setOnMapLongClickListener {
            toast("如果要加marker，請點選上方開關")
        }
        add_marker_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mMap.setOnMapLongClickListener {
                    //mMap.addMarker(MarkerOptions().position(it).title("點點?"))
                    val markeritem=ClusterMarker(it.latitude,it.longitude,"點點?","內容的啦")
                    val nowtime=LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d H:m"))

                    var markerkey=database.child("Chats").child(current_group).child("Marker").push().key
                    //database.child("Chats").child(current_group).child("Marker").child(markerkey!!).setValue(markeritem)
                    database.child("Chats").child(current_group).child("Marker").child(markerkey!!).child("lat").setValue(it.latitude)
                    database.child("Chats").child(current_group).child("Marker").child(markerkey!!).child("lng").setValue(it.longitude)
                    database.child("Chats").child(current_group).child("Marker").child(markerkey!!).child("title").setValue("點點?")
                    database.child("Chats").child(current_group).child("Marker").child(markerkey!!).child("snippet").setValue("點4534點?")
                    database.child("Chats").child(current_group).child("Marker").child(markerkey!!).child("time").setValue(nowtime)
                    mClusterManager.addItem(markeritem)
                    mClusterManager.cluster()
                }
            } else {
                mMap.setOnMapLongClickListener {
                    toast("如果要加marker，請點選上方開關")
                }
            }
        }

    }//onMapReady end

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    override fun onLocationChanged(location: Location) {
        mLocationChangedListener.onLocationChanged(location)//抓之前的位置
        mMap.animateCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    location.latitude,
                    location.longitude
                )
            )
        )//移動鏡頭到新位置

        val la = location.latitude
        val lo = location.longitude


        //設定經緯度顯示

        //-------------------------firebase----------------------------------
        val userid = FirebaseAuth.getInstance().uid //取得現在的user的uid

        //拿User的current_group
        database.child("Users").child(userid.toString()).child("currentGroup").get()
            .addOnSuccessListener {
                current_group = (it.value as String?)!!
                //如果沒有的話，跳轉到GroupActivity裡加入Group或切換

                //顯示firebase的marker
                SetupCluster()

                if (current_group == "not_have_yet") {
                    val intent = Intent(this, GroupActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    if (userid != null) {
                        val time = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                        database.child("Chats").child(current_group).child("Useruid").child(userid)
                            .child("la").setValue(la)
                        database.child("Chats").child(current_group).child("Useruid").child(userid)
                            .child("lo").setValue(lo)
                        database.child("Chats").child(current_group).child("Useruid").child(userid)
                            .child("time").setValue(time)
                    }
                }
            }.addOnFailureListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
        //-------------------------firebase----------------------------------

        database.child("Users").child(userid.toString()).child("currentGroup").get()
            .addOnSuccessListener {
                current_group = it.value as String
                if (current_group == "not_have_yet") {
                    val intent = Intent(this, GroupActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    database.child("Chats").child(current_group).child("Useruid")
                        .addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(datasnapshot: DataSnapshot) {
                                val postSnapshot = datasnapshot.children
                                mMap.clear()
                                for (item in postSnapshot) {
                                    var useruid = item.key
                                    if (useruid != null) {
                                        database.child("Chats").child(current_group)
                                            .child("Useruid").child(useruid).get()
                                            .addOnSuccessListener { datasnapshot ->
                                                var positionlalo =
                                                    datasnapshot.getValue<positionlalo>()
                                                if (positionlalo != null) {
                                                    database.child("Users").child(useruid)
                                                        .child("userName").get()
                                                        .addOnSuccessListener {
                                                            var name = it.value.toString()
                                                            var last_time_string = positionlalo.time
                                                            var last_time_localdate: LocalDateTime =
                                                                LocalDateTime.parse(
                                                                    last_time_string,
                                                                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                                                                )
                                                            var duration = Duration.between(
                                                                last_time_localdate,
                                                                LocalDateTime.now()
                                                            ).toMinutes()
                                                            var timestring = "(" + duration + "分鐘前)"
                                                            if (duration > 60) {
                                                                timestring = "(1小時以上)"
                                                            }
                                                            if (useruid != userid) {
                                                                mMap.addMarker(
                                                                    MarkerOptions().position(
                                                                        LatLng(
                                                                            positionlalo.la,
                                                                            positionlalo.lo
                                                                        )
                                                                    ).title(name + timestring).icon(
                                                                        BitmapDescriptorFactory.defaultMarker(
                                                                            BitmapDescriptorFactory.HUE_ROSE
                                                                        )
                                                                    )
                                                                )
                                                            }
                                                            Log.d(
                                                                "test",
                                                                useruid + "----------" + userid
                                                            )//測試用log
                                                        }
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
    }//onLocationChange end

    override fun activate(p0: LocationSource.OnLocationChangedListener) {
        mLocationChangedListener = p0
        checkLocationPermissionAndEnableIt(true)
        Log.d("test","地圖塗層啟用")
    }

    override fun deactivate() {
        checkLocationPermissionAndEnableIt(false)
        Log.d("test","地圖的my location layer關掉了")

    }

    override fun onStart() {
        super.onStart()
        checkLocationPermissionAndEnableIt(true)
    }

    override fun onStop() {
        super.onStop()
        checkLocationPermissionAndEnableIt(false)//停止定位
    }

    private fun checkLocationPermissionAndEnableIt(on: Boolean) {
        if (ContextCompat.checkSelfPermission(
                this@MapsActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //這功能沒有使用者同意
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val listener = DialogInterface.OnClickListener { dialog, which ->
                    //答覆後執行onRequestPermissionsResult()
                    ActivityCompat.requestPermissions(
                        this@MapsActivity, arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ), REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION
                    )
                }
                val altDlgBuilde = AlertDialog.Builder(this)
                altDlgBuilde.setTitle("提示")
                altDlgBuilde.setMessage("你沒有啟動Chital的定位功能")
                altDlgBuilde.setIcon(android.R.drawable.ic_dialog_info)
                altDlgBuilde.setCancelable(false)
                altDlgBuilde.setPositiveButton("確定", listener)
                altDlgBuilde.show()
                return
            } else {
                //答覆後執行onRequestPermissionsResult()
                ActivityCompat.requestPermissions(
                    this@MapsActivity, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), REQUEST_PERMISSION_FOR_ACCESS_FINE_LOCATION
                )
            }
        }

        //已經有使用者同意
        if (on) {
            if (mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 5f, this)
                Log.d("test","用GPS定位")
            } else {
                if (mLocationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    mLocationMgr.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        500L,
                        5f,
                        this
                    )
                    Log.d("test","用網路定位")
                }
            }
        } else {
            mLocationMgr.removeUpdates(this)
            Log.d("test","定位已經停用了")
        }
    }//checkLocationPermissionAndEnableIt end
    private fun SetupCluster(){
        mClusterManager= ClusterManager(this,mMap)
        mClusterManager.setOnClusterClickListener(this)
        mClusterManager.setOnClusterItemClickListener(this)
        //allen->point the map's listener at the listeners implement by the cluster Manager
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        //allen->add item(Marker) to the cluster manager
        addItem()

    }

    private fun addItem() {
        database.child("Chats").child(current_group).child("Marker").get().addOnSuccessListener { it ->
            val dataSnapshot=it.children
            mClusterManager.clearItems()
            Log.v("test","跑得出來1")
            for(item in dataSnapshot){
                var markerid=item.key
                Log.v("test","$markerid")
                if(markerid!=null){
                    database.child("Chats").child(current_group).child("Marker").child(markerid).get().addOnSuccessListener{data->
                        var x=data.getValue<AC>()
                        val mMarker= x?.let { it1 -> ClusterMarker(it1.lat,x.lng,x.title,x.snippet,x.time) }
                        mClusterManager.addItem(mMarker)
                    }
                }
            }
        }
    }

    override fun onClusterClick(cluster: Cluster<ClusterMarker>?): Boolean {
        animateZoomInCamera(cluster!!.position)
        return true
    }

    override fun onClusterItemClick(cluster: ClusterMarker?): Boolean {
        animateZoomInCamera(cluster!!.position)
        return true
    }
    private fun animateZoomInCamera(latLng: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10f))
    }
}

data class positionlalo(val la: Double, val lo: Double, val time: String) {
    constructor() : this(-1.0, -1.0, "2021/08/16 00:06:53.268")
}

data class AC(val lat: Double, val lng: Double, val snippet:String,val time: String,val title:String) {
    constructor() : this(-1.0, -1.0, "預設內容","2021/08/16 00:06:53.268","預設標題")
}

