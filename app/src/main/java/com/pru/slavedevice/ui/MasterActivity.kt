package com.pru.slavedevice.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import com.pru.slavedevice.nearby_api.MasterAPI
import com.pru.slavedevice.theme.SlaveDeviceTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MasterActivity : ComponentActivity() {
    @Inject
    lateinit var masterAPI: MasterAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlaveDeviceTheme {
                val logState = rememberSaveable { mutableStateOf("Loading") }
                val multiplierState = rememberSaveable { mutableStateOf("2") }
                val focusManager = LocalFocusManager.current
                // A surface container using the 'background' color from the theme
                LaunchedEffect(true) {


                    masterAPI.startAdvertising(
                        onSuccessListener = {
                            logState.value = logState.value + "\nSuccess Listener"
                        },
                        onConnectionInitiated = { _: String, connectionInfo: ConnectionInfo ->
                            logState.value =
                                logState.value + "\nonConnectionInitiated endpointName ${connectionInfo.endpointName}"
                        },
                        onPayloadReceived = { endpointId: String, payload: Payload ->
                            logState.value =
                                logState.value + "\nonPayloadReceived endpointId $endpointId payload $payload"
                            if (payload.type == Payload.Type.BYTES) {
                                val numb: Int =
                                    String(payload.asBytes() ?: "0".toByteArray()).toIntOrNull()
                                        ?: 0
                                Log.i("Prudhvi Log", "onPayloadReceived: $numb")
                                logState.value = logState.value + "\nreceived $numb"
                                val result = (numb * (multiplierState.value.trim().toIntOrNull()
                                    ?: 1)).toString()
                                masterAPI.sendResponse(
                                    endPointId = endpointId,
                                    payload = result
                                )
                                logState.value = logState.value + "\nSent Ack $result"
                            }
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
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
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
                        Spacer(modifier = Modifier.height(15.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Text(logState.value, style = TextStyle(fontSize = 14.sp))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        masterAPI.stopServices()
    }
}