package com.pru.slavedevice

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import com.pru.slavedevice.ui.theme.SlaveDeviceTheme
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private lateinit var message: Message

    /**
     * A [MessageListener] for processing messages from nearby devices.
     */
    private lateinit var messageListener: MessageListener

    /**
     * Sets the time to live in seconds for the publish or subscribe.
     */
    private val TTL_IN_SECONDS = 120 // Two minutes.

    /**
     * Choose of strategies for publishing or subscribing for nearby messages.
     */
    private val PUB_SUB_STRATEGY = Strategy.Builder().setTtlSeconds(TTL_IN_SECONDS).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        message = Message("Please come on".toByteArray(Charset.defaultCharset()))
        messageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                // Called when a new message is found.
                val msgBody = String(message.content)
                Log.i("Prudhvi Log", "onFound: $msgBody")
            }

            override fun onLost(message: Message) {
                // Called when a message is no longer detectable nearby.
                val msgBody = String(message.content)
                Log.i("Prudhvi Log", "onLost: $msgBody")
            }
        }
        setContent {
            SlaveDeviceTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Greeting()
                }
            }
        }
    }

    private fun publish() {
        val options = PublishOptions.Builder()
            .setStrategy(PUB_SUB_STRATEGY)
            .setCallback(object : PublishCallback() {
                override fun onExpired() {
                    super.onExpired()
                    // flick the switch off since the publishing has expired.
                    // recall that we had set expiration time to 120 seconds
                    Log.i("Prudhvi Log", "onExpired: ")
                }
            }).build()

        Nearby.getMessagesClient(this).publish(message, options)
    }

    private fun unpublish() {
        Nearby.getMessagesClient(this).unpublish(message)
    }

    private fun subscribe() {
        val options = SubscribeOptions.Builder()
            .setStrategy(PUB_SUB_STRATEGY)
            .setCallback(object : SubscribeCallback() {
                override fun onExpired() {
                    super.onExpired()
                    Log.i("Prudhvi Log", "onExpired: ")
                }
            }).build()

        Nearby.getMessagesClient(this).subscribe(messageListener, options)
    }

    private fun unsubscribe() {
        Nearby.getMessagesClient(this).unsubscribe(messageListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        // although the API should shutdown its processes when the client process dies,
        // you may want to stop subscribing (and publishing if convenient)
        Nearby.getMessagesClient(this).unpublish(message)
        Nearby.getMessagesClient(this).unsubscribe(messageListener)
    }

    @Composable
    fun Greeting() {
        Column {
            Button(onClick = { subscribe() }) {
                Text(text = "Subscribe")
            }
            Button(onClick = { publish() }) {
                Text(text = "Publish")
            }
        }
    }
}
