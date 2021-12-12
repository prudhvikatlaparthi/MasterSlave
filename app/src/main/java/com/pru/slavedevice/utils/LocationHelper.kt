package com.pru.slavedevice.utils

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.pru.slavedevice.listeners.LocationHelperListener

class LocationHelper(
    private val activity: Activity,
    private val locationHelperListener: LocationHelperListener
) {

    companion object {
        const val PERMISSION_REQUEST_CODE_LOCATION = 1101
        const val REQUEST_CODE_LOCATION_SETTINGS = 1100
    }

    private lateinit var apiException: ApiException
    private lateinit var request: LocationRequest
    private val mFusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
    private val mLocationCallback: LocationCallback

    init {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                val latitude = result.lastLocation.latitude
                val longitude = result.lastLocation.longitude
                Log.d("HttpClient", "HttpClient onLocationResult: $latitude || $longitude")
                locationHelperListener.found(latitude, longitude)
                disconnect()
            }
        }
    }

    fun fetchLocation() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_REQUEST_CODE_LOCATION
        )
    }

    fun onGrantLocationPermission() {
        isPlayServicesAvailable()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            when (resultCode) {
                Activity.RESULT_CANCELED -> {
                    startGPSDialog()
                }
                Activity.RESULT_OK -> {
                    listen()
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mFusedLocationClient.requestLocationUpdates(
                        request,
                        mLocationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
        }
    }

    private fun isPlayServicesAvailable() {
        val playAPI = GoogleApiAvailability.getInstance()
        val resultCode = playAPI.isGooglePlayServicesAvailable(activity)
        if (resultCode != ConnectionResult.SUCCESS)
            locationHelperListener.error("Please enable/update Google Play Services from settings")
        else
            isGPSEnabled()
    }

    private fun isGPSEnabled() {
        request =
            LocationRequest.create().setInterval((1).toLong()).setFastestInterval(1)
                .setSmallestDisplacement(10F)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder().addLocationRequest(request)
        val result = LocationServices.getSettingsClient(activity.applicationContext)
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                listen()
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@addOnCompleteListener
                }
                mFusedLocationClient.requestLocationUpdates(
                    request,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
            } catch (exception: ApiException) {
                apiException = exception
                exception.printStackTrace()
                startGPSDialog()
            }
        }
    }

    private fun startGPSDialog() {
        if (this::apiException.isInitialized) {
            when (apiException.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        val resolvable = apiException as ResolvableApiException
                        resolvable.startResolutionForResult(
                            activity,
                            REQUEST_CODE_LOCATION_SETTINGS
                        )

                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    } catch (e: ClassCastException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            locationHelperListener.error("Please enable GPS for more accurate results.")
        }
    }

    private fun listen() {
        locationHelperListener.start()

    }

    fun disconnect() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }
}