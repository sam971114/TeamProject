package com.example.myrunmain

import com.google.android.gms.maps.model.LatLng

data class NearData(val name: String, val len: String, val distance: String, val path: List<LatLng>):java.io.Serializable