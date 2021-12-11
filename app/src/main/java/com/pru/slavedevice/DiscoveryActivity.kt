package com.pru.slavedevice

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.pru.slavedevice.ui.theme.SlaveDeviceTheme

class DiscoveryActivity : ComponentActivity() {
    private var mRemoteHostEndpoint: String? = null
    private var isAdvertising: Boolean = false
    private var isDiscovery: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlaveDeviceTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Button(onClick = { startAdvertising() }) {
                            Text(text = "Start Advertising")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = { startDiscovery() }) {
                            Text(text = "Start Discovery")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isAdvertising) {
            Nearby.getConnectionsClient(this).stopAdvertising()
            Nearby.getConnectionsClient(this).stopAllEndpoints()
        }
        if (isDiscovery) {
            Nearby.getConnectionsClient(this).stopDiscovery()
            mRemoteHostEndpoint?.let {
                Nearby.getConnectionsClient(this).disconnectFromEndpoint(it)
            }
        }
    }

    private fun startAdvertising() {
        isAdvertising = true
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(this).startAdvertising(
            "message String",
            packageName,
            object : ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
                    Log.i("Prudhvi Log", "onConnectionInitiated: ")
                    Toast.makeText(
                        this@DiscoveryActivity,
                        "onConnectionInitiated: $p0 $p1",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
                    Log.i("Prudhvi Log", "onConnectionResult: ")
                    Toast.makeText(
                        this@DiscoveryActivity,
                        "onConnectionResult: $p0 $p1",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onDisconnected(p0: String) {
                    Log.i("Prudhvi Log", "onDisconnected: ")
                    Toast.makeText(
                        this@DiscoveryActivity,
                        "onDisconnected: $p0",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            options
        ).addOnSuccessListener {
            Log.i("Prudhvi Log", "startAdvertising: success")
            Toast.makeText(this@DiscoveryActivity, "startAdvertising: success", Toast.LENGTH_SHORT)
                .show()
        }.addOnFailureListener {
            Log.i("Prudhvi Log", "startAdvertising: failure $it")
            Toast.makeText(
                this@DiscoveryActivity,
                "startAdvertising: failure $it",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(this)
            .startDiscovery(packageName, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(p0: String, p1: DiscoveredEndpointInfo) {
                    Log.i("Prudhvi Log", "onEndpointFound: ")
                    Toast.makeText(
                        this@DiscoveryActivity,
                        "onEndpointFound $p0 ${String(p1.endpointInfo)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Nearby.getConnectionsClient(this@DiscoveryActivity)
                        .requestConnection("Tess", p0, object : ConnectionLifecycleCallback() {
                            override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
                                Nearby.getConnectionsClient(this@DiscoveryActivity)
                                    .acceptConnection(p0, object : PayloadCallback() {
                                        override fun onPayloadReceived(p0: String, p1: Payload) {
                                            Log.i(
                                                "Prudhvi Log",
                                                "onPayloadReceived: ${String(p1.asBytes() ?: "Not".toByteArray())}"
                                            )
                                            Nearby.getConnectionsClient(this@DiscoveryActivity)
                                                .sendPayload(
                                                    p0,
                                                    Payload.fromBytes("Ack".toByteArray())
                                                )
                                        }

                                        override fun onPayloadTransferUpdate(
                                            p0: String,
                                            p1: PayloadTransferUpdate
                                        ) {

                                        }
                                    });
                            }

                            override fun onConnectionResult(
                                endPoint: String,
                                resolution: ConnectionResolution
                            ) {
                                if (resolution.status.isSuccess) {
                                    mRemoteHostEndpoint = endPoint
                                    Log.i(
                                        "Prudhvi Log",
                                        "onConnectionResult: Connected successfully "
                                    )
                                    Nearby.getConnectionsClient(this@DiscoveryActivity)
                                        .stopDiscovery()
                                } else {
                                    if (resolution.status.statusCode == ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED) {
                                        Log.i(
                                            "Prudhvi Log",
                                            "onConnectionResult: The connection was rejected by one or both sides"
                                        )
                                    } else {
                                        Log.i(
                                            "Prudhvi Log",
                                            "onConnectionResult: \"Connection to \" + $endPoint + \" failed. Code: \" + resolution.status.statusCode"
                                        )
                                    }
                                }
                            }

                            override fun onDisconnected(p0: String) {
                                TODO("Not yet implemented")
                            }
                        }).addOnSuccessListener {

                        }.addOnFailureListener {

                        }
                }

                override fun onEndpointLost(p0: String) {
                    Log.i("Prudhvi Log", "onEndpointLost: ")
                    Toast.makeText(this@DiscoveryActivity, "onEndpointLost $p0", Toast.LENGTH_SHORT)
                        .show()
                }
            }, options).addOnSuccessListener {
                Log.i("Prudhvi Log", "startDiscovery: success")
                Toast.makeText(
                    this@DiscoveryActivity,
                    "startDiscovery: success",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Log.i("Prudhvi Log", "startDiscovery: failure $it")
                Toast.makeText(
                    this@DiscoveryActivity,
                    "startDiscovery: failure $it",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SlaveDeviceTheme {
        Greeting("Android")
    }
}