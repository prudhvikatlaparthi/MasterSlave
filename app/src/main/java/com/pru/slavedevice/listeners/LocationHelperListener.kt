package com.pru.slavedevice.listeners

interface LocationHelperListener {
    fun start()
    fun found(latitude: Double, longitude: Double)
    fun error(message :String)
}