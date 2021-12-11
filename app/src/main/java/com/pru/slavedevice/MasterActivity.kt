package com.pru.slavedevice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.pru.slavedevice.ui.theme.SlaveDeviceTheme
import java.nio.charset.Charset

class MasterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlaveDeviceTheme {
                val logState = rememberSaveable { mutableStateOf<String>("Loading") }
                val multiplierState = rememberSaveable { mutableStateOf("2") }
                val focusManager = LocalFocusManager.current
                // A surface container using the 'background' color from the theme
                startAdvertising(logState, multiplierState)
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(logState.value)
                        Spacer(modifier = Modifier.height(15.dp))
                        OutlinedTextField(
                            value = multiplierState.value,
                            onValueChange = {
                                multiplierState.value = it
                            },
                            label = { Text(text = "Multiplier") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                            })
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Nearby.getConnectionsClient(this).stopAdvertising()
        Nearby.getConnectionsClient(this).stopAllEndpoints()
    }

    private fun startAdvertising(
        logState: MutableState<String>,
        multiplierState: MutableState<String>
    ) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(this).startAdvertising(
            "Master Device",
            packageName,
            object : ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(
                    endPoint: String,
                    connectionInfo: ConnectionInfo
                ) {
                    /*logState.value =
                        "onConnectionInitiated endpointId $endPoint connectionInfo $connectionInfo"*/
                    Log.i("Prudhvi Log", "onConnectionInitiated: ")
                    Nearby.getConnectionsClient(this@MasterActivity)
                        .acceptConnection(endPoint, object : PayloadCallback() {
                            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                                logState.value =
                                    "onPayloadReceived endpointId $endpointId payload $payload"
                                if (payload.type == Payload.Type.BYTES) {
                                    val numb: Int =
                                        String(payload.asBytes() ?: "0".toByteArray()).toIntOrNull()
                                            ?: 0
                                    Log.i("Prudhvi Log", "onPayloadReceived: $numb")
                                    logState.value = "received $numb"
                                    logState.value = "Sending Ack ${
                                        numb * (multiplierState.value.trim().toIntOrNull() ?: 1)
                                    }"
                                    Nearby.getConnectionsClient(this@MasterActivity).sendPayload(
                                        endpointId, Payload.fromBytes(
                                            "${
                                                numb * (multiplierState.value.trim()
                                                    .toIntOrNull() ?: 1)
                                            }".toByteArray(
                                                Charset.forName("UTF-8")
                                            )
                                        )
                                    )
                                    logState.value = "Sent Ack ${numb}"
                                }
                            }

                            override fun onPayloadTransferUpdate(
                                endpointId: String,
                                payloadTransferUpdate: PayloadTransferUpdate
                            ) {
                                /*logState.value =
                                    "onPayloadTransferUpdate endpointId $endpointId payloadTransferUpdate $payloadTransferUpdate"*/
                            }
                        })
                }

                override fun onConnectionResult(
                    endPointId: String,
                    connectionResolution: ConnectionResolution
                ) {
                    Log.i("Prudhvi Log", "onConnectionResult: ")
                    /*logState.value =
                        "onConnectionResult endPointId $endPointId connectionResolution $connectionResolution"*/
                }

                override fun onDisconnected(endPointId: String) {
                    Log.i("Prudhvi Log", "onDisconnected: ")
                    logState.value = "onDisconnected endPointId $endPointId"
                }
            },
            options
        ).addOnSuccessListener {
            Log.i("Prudhvi Log", "startAdvertising: success")
            logState.value = "Success Listener"
        }.addOnFailureListener {
            Log.i("Prudhvi Log", "startAdvertising: failure $it")
            logState.value = "Failure $it"
        }
    }
}