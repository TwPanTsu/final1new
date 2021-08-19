package com.example.final1.Models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.time.LocalDateTime

class ClusterMarker : ClusterItem {

    private var mTitle:String
    private var mSnippet:String
    private var Lat:Double
    private var Lng:Double
    private var mPosition:LatLng
    private lateinit var Time:String

    constructor(lat: Double, lng:Double, titles:String, snippet: String, time: String){
        mTitle=titles
        mSnippet=snippet
        Lat=lat
        Lng=lng
        mPosition=LatLng(Lat,Lng)
        Time=time
    }
    constructor(lat: Double, lng:Double, titles:String, snippet: String){
        mTitle=titles
        mSnippet=snippet
        Lat=lat
        Lng=lng
        mPosition=LatLng(Lat,Lng)
    }



    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return mSnippet
    }
    fun getTime():String{
        return Time
    }
}