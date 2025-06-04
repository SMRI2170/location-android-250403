package com.example.demo0430

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import android.graphics.Color
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationText: TextView
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleMap: GoogleMap
    private val pathPoints = mutableListOf<LatLng>() // 履歴を保存
    private var currentMarker: Marker? = null
    private var currentCircle: Circle? = null

    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "位置情報の許可が必要です", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        locationText = findViewById(R.id.textLocation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 地図初期化
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationRequest()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateIntervalMillis(1000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return

                val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss", java.util.Locale.getDefault())
                val timeStr = sdf.format(java.util.Date(location.time))

                val info = """
                緯度: ${location.latitude}
                経度: ${location.longitude}
                高度: ${location.altitude} m
                精度: ${location.accuracy} m
                速度: ${location.speed} m/s
                進行方向: ${location.bearing} °
                時刻: $timeStr
            """.trimIndent()

                locationText.text = info

                val latLng = LatLng(location.latitude, location.longitude)

                if (::googleMap.isInitialized) {
                    // マーカーを1つだけ表示（前回削除して更新）
                    currentMarker?.remove()
                    currentMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("現在地")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )

                    currentCircle = googleMap.addCircle(
                        CircleOptions()
                            .center(latLng)
                            .radius(30.0)
                            .strokeColor(Color.RED)
                            .fillColor(0x30FF0000)  // 半透明の赤
                            .strokeWidth(2f)
                    )

                    // 軌跡を保存・Polylineで表示
                    pathPoints.add(latLng)
                    googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(pathPoints)
                            .color(Color.BLUE)
                            .width(8f)
                    )

                    // 地図を移動
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            }

        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onStop() {
        super.onStop()
        locationClient.removeLocationUpdates(locationCallback)
    }
}
