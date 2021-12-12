package com.pru.slavedevice.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.pru.slavedevice.theme.SlaveDeviceTheme
import com.pru.slavedevice.nearby_api.SlaveAPI
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SlaveActivity : ComponentActivity() {
    private val mRemoteHostEndpoint = mutableStateOf<String?>(null)

    @Inject
    lateinit var slaveAPI: SlaveAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlaveDeviceTheme {
                // A surface container using the 'background' color from the theme
                val logState = rememberSaveable { mutableStateOf("Loading") }
                val focusManager = LocalFocusManager.current
                LaunchedEffect(true) {
                    slaveAPI.startDiscovery(
                        slaveName = "Slave 1",
                        onSuccessListener = {
                            logState.value = logState.value + "\nSuccess Listener"
                        },
                        onConnectionInitiated = { _: String, connectionInfo: ConnectionInfo ->
                            logState.value =
                                logState.value + "\nonConnectionInitiated endpointName ${connectionInfo.endpointName}"
                        },
                        onConnectionSuccess = { endPointId: String, _: ConnectionResolution ->
                            mRemoteHostEndpoint.value = endPointId
                        },
                        onPayloadReceived = { _: String, payload: Payload ->
                            logState.value = logState.value +
                                    "\n"+String(payload.asBytes() ?: "Error ".toByteArray())
                        },
                        onEndpointLost = { endPointId ->
                            logState.value =
                                logState.value + "\nonDisconnected endPointId $endPointId"
                        },
                        onDisconnected = { endPointId ->
                            logState.value =
                                logState.value + "\nonDisconnected endPointId $endPointId"
                        },
                        onFailureListener = {
                            logState.value = logState.value + "\nFailure $it"
                        })
                }
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        var numberState by rememberSaveable {
                            mutableStateOf("")
                        }
                        OutlinedTextField(
                            value = numberState,
                            onValueChange = {
                                numberState = it
                            },
                            label = { Text(text = "Enter Number") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            })
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = {
                            mRemoteHostEndpoint.value?.let { endPointId ->
                                slaveAPI.sendRequest(endPointId, numberState)
                            }
                        }, enabled = mRemoteHostEndpoint.value != null) {
                            Text("Send")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                item {
                                    Text(logState.value, style = TextStyle(fontSize = 14.sp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (mRemoteHostEndpoint.value == null)
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(30.dp)
                                )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        slaveAPI.stopServices(mRemoteHostEndpoint.value)
        /*Nearby.getConnectionsClient(this).stopDiscovery()
        mRemoteHostEndpoint.value?.let {
            Nearby.getConnectionsClient(this).disconnectFromEndpoint(it)
        }*/
    }

    /*private fun startDiscovery(
        logState: MutableState<String>
    ) {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(this)
            .startDiscovery(packageName, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(
                    endPointId: String,
                    discoveredEndpointInfo: DiscoveredEndpointInfo
                ) {
                    Log.i("Prudhvi Log", "onEndpointFound: ")
                    logState.value =
                        "onEndpointFound endPointId $endPointId discoveredEndpointInfo $discoveredEndpointInfo"
                    Nearby.getConnectionsClient(this@SlaveActivity)
                        .requestConnection(
                            "Slave Device 1",
                            endPointId,
                            object : ConnectionLifecycleCallback() {
                                override fun onConnectionInitiated(
                                    endPointId: String,
                                    connectionInfo: ConnectionInfo
                                ) {
                                    logState.value =
                                        "onConnectionInitiated endPointId $endPointId connectionInfo $connectionInfo"
                                    Nearby.getConnectionsClient(this@SlaveActivity)
                                        .acceptConnection(endPointId, object : PayloadCallback() {
                                            override fun onPayloadReceived(
                                                endPointId: String,
                                                payload: Payload
                                            ) {
                                                logState.value =
                                                    "onPayloadReceived endPointId $endPointId payload $payload"
                                                if (payload.getType() == Payload.Type.BYTES) {
                                                    Log.i(
                                                        "Prudhvi Log",
                                                        "onPayloadReceived: ${String(payload.asBytes() ?: "Not".toByteArray())}"
                                                    )
                                                }
                                                logState.value =
                                                    String(payload.asBytes() ?: "Not".toByteArray())
                                            }

                                            override fun onPayloadTransferUpdate(
                                                endPointId: String,
                                                payloadTransferUpdate: PayloadTransferUpdate
                                            ) {
                                                logState.value =
                                                    "onPayloadTransferUpdate endPointId $endPointId payloadTransferUpdate $payloadTransferUpdate"
                                            }
                                        });
                                }

                                override fun onConnectionResult(
                                    endPoint: String,
                                    resolution: ConnectionResolution
                                ) {
                                    logState.value =
                                        "onConnectionResult endPoint $endPoint resolution $resolution"
                                    if (resolution.status.isSuccess) {
                                        mRemoteHostEndpoint.value = endPoint
                                        Log.i(
                                            "Prudhvi Log",
                                            "onConnectionResult: Connected successfully "
                                        )
                                        Nearby.getConnectionsClient(this@SlaveActivity)
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

                                override fun onDisconnected(endPointId: String) {
                                    logState.value = "onDisconnected endPointId $endPointId"
                                }
                            }).addOnSuccessListener {
                            Log.i("Prudhvi Log", "onEndpointFound: ")
                        }.addOnFailureListener {
                            Log.i("Prudhvi Log", "onEndpointFound: ")
                        }
                }

                override fun onEndpointLost(endPointId: String) {
                    Log.i("Prudhvi Log", "onEndpointLost: ")
                    logState.value = "onEndpointLost endPointId $endPointId"
                }
            }, options).addOnSuccessListener {
                Log.i("Prudhvi Log", "startDiscovery: success")
                logState.value = "Success Listener"
            }.addOnFailureListener {
                Log.i("Prudhvi Log", "startDiscovery: failure $it")
                logState.value = "Failure $it"
            }
    }*/
}