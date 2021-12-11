package com.pru.slavedevice

import android.content.Intent
import android.os.Bundle
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
import com.pru.slavedevice.ui.theme.SlaveDeviceTheme

class PickActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        },modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text(text = "Master Device")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            Intent(this@PickActivity, SlaveActivity::class.java).apply {
                                startActivity(this)
                            }
                        },modifier = Modifier.fillMaxWidth(0.6f)) {
                            Text(text = "Slave Device")
                        }
                    }
                }
            }
        }
    }
}