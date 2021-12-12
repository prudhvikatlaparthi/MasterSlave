package com.pru.slavedevice.nearby_api

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlaveAPI @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun startDiscovery(
        slaveName: String,
        onSuccessListener: () -> Unit,
        onConnectionInitiated: (String, ConnectionInfo) -> Unit,
        onConnectionSuccess: (String, ConnectionResolution) -> Unit,
        onPayloadReceived: (String, Payload) -> Unit,
        onEndpointLost: (String) -> Unit,
        onDisconnected: (String) -> Unit,
        onFailureListener: (Exception) -> Unit
    ) {
        val options = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        Nearby.getConnectionsClient(context)
            .startDiscovery(context.packageName, object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(
                    endPointId: String,
                    discoveredEndpointInfo: DiscoveredEndpointInfo
                ) {
                    Nearby.getConnectionsClient(context)
                        .requestConnection(
                            slaveName,
                            endPointId,
                            object : ConnectionLifecycleCallback() {
                                override fun onConnectionInitiated(
                                    endPointId: String,
                                    connectionInfo: ConnectionInfo
                                ) {
                                    onConnectionInitiated.invoke(endPointId, connectionInfo)
                                    Nearby.getConnectionsClient(context)
                                        .acceptConnection(endPointId, object : PayloadCallback() {
                                            override fun onPayloadReceived(
                                                endPointId: String,
                                                payload: Payload
                                            ) {
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
                                    endPoint: String,
                                    resolution: ConnectionResolution
                                ) {
                                    if (resolution.status.isSuccess) {
                                        onConnectionSuccess.invoke(endPoint, resolution)

                                        Nearby.getConnectionsClient(context)
                                            .stopDiscovery()
                                    }
                                }

                                override fun onDisconnected(endPointid: String) {
                                    onDisconnected.invoke(endPointId)
                                    startDiscovery(
                                        slaveName = slaveName,
                                        onSuccessListener = onSuccessListener,
                                        onConnectionInitiated = onConnectionInitiated,
                                        onConnectionSuccess = onConnectionSuccess,
                                        onPayloadReceived = onPayloadReceived,
                                        onEndpointLost = onEndpointLost,
                                        onDisconnected = onDisconnected,
                                        onFailureListener = onFailureListener
                                    )
                                }
                            })
                }

                override fun onEndpointLost(endPointId: String) {
                    onEndpointLost.invoke(endPointId)
                }
            }, options).addOnFailureListener {
                onFailureListener.invoke(it)
            }
    }

    fun sendRequest(endPointId: String, payload: String) {
        Nearby.getConnectionsClient(context).sendPayload(
            endPointId, Payload.fromBytes(
                payload.toByteArray()
            )
        )
    }

    fun stopServices(masterEndPoint: String? = null) {
        Nearby.getConnectionsClient(context).stopDiscovery()
        masterEndPoint?.let {
            Nearby.getConnectionsClient(context).disconnectFromEndpoint(it)
        }
    }
}