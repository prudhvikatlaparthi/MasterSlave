package com.pru.slavedevice.nearby_api

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterAPI @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun startAdvertising(
        onSuccessListener: () -> Unit,
        onConnectionInitiated: (String, ConnectionInfo) -> Unit,
        onPayloadReceived: (String, Payload) -> Unit,
        onDisconnected: (String) -> Unit,
        onFailureListener: (Exception) -> Unit
    ) {
        val options = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(context).startAdvertising(
            "Master Device",
            context.packageName,
            object : ConnectionLifecycleCallback() {
                override fun onConnectionInitiated(
                    endPointId: String,
                    connectionInfo: ConnectionInfo
                ) {
                    onConnectionInitiated.invoke(endPointId, connectionInfo)
                    Nearby.getConnectionsClient(context)
                        .acceptConnection(endPointId, object : PayloadCallback() {
                            override fun onPayloadReceived(endPointId: String, payload: Payload) {
                                onPayloadReceived.invoke(endPointId, payload)
                            }

                            override fun onPayloadTransferUpdate(
                                endPointId: String,
                                payloadTransferUpdate: PayloadTransferUpdate
                            ) {

                            }
                        })
                }

                override fun onConnectionResult(
                    endPointId: String,
                    connectionResolution: ConnectionResolution
                ) {
                }

                override fun onDisconnected(endPointId: String) {
                    onDisconnected.invoke(endPointId)
                }
            },
            options
        ).addOnFailureListener {
            onFailureListener.invoke(it)
        }
    }

    fun sendResponse(endPointId: String, payload: String) {
        Nearby.getConnectionsClient(context).sendPayload(
            endPointId, Payload.fromBytes(
                payload.toByteArray()
            )
        )
    }

    fun stopServices() {
        Nearby.getConnectionsClient(context).stopAdvertising()
        Nearby.getConnectionsClient(context).stopAllEndpoints()
    }


}