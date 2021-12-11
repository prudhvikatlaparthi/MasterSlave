package com.pru.slavedevice.utils

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.Payload
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterAPI @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun sendResponse(endpointId: String, payload: String) {
        Nearby.getConnectionsClient(context).sendPayload(
            endpointId, Payload.fromBytes(
                payload.toByteArray()
            )
        )
    }


}