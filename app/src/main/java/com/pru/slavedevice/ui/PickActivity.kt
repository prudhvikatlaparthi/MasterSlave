package com.pru.slavedevice.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pru.slavedevice.listeners.LocationHelperListener
import com.pru.slavedevice.theme.SlaveDeviceTheme
import com.pru.slavedevice.utils.LocationHelper
import com.pru.slavedevice.utils.LocationHelper.Companion.PERMISSION_REQUEST_CODE_LOCATION

class PickActivity : ComponentActivity() {
    private lateinit var locationHelper: LocationHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationHelper =
            LocationHelper(this, locationHelperListener = object : LocationHelperListener {
                override fun start() {

                }

                override fun found(latitude: Double, longitude: Double) {
                    Log.i("Prudhvi Log", "found: $latitude $longitude")
                }

                override fun error(message: String) {
                    Toast.makeText(this@PickActivity, message, Toast.LENGTH_SHORT).show()
                }
            })
        locationHelper.fetchLocation()
        setContent {
            SlaveDeviceTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Button(onClick = {
                            Intent(this@PickActivity, MasterActivity::class.java).apply {
                                startActivity(this)
                            }
                        }, modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text(text = "Master Device")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            Intent(this@PickActivity, SlaveActivity::class.java).apply {
                                startActivity(this)
                            }
                        }, modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text(text = "Slave Device")
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationHelper.onActivityResult(requestCode = requestCode, resultCode = resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.onGrantLocationPermission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.disconnect()
    }
}