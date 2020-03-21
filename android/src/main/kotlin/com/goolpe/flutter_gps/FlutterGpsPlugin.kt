// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package com.goolpe.flutter_gps

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.*
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodChannel.MethodCallHandler


class FlutterGPSPlugin : MethodCallHandler, EventChannel.StreamHandler, FlutterPlugin, ActivityAware {
  private var applicationContext: Context? = null
  private var chargingStateChangeReceiver: BroadcastReceiver? = null
  private var methodChannel: MethodChannel? = null
  private var eventChannel: EventChannel? = null
  private var isGPSEnabled: Boolean? = null
  private var activity: Activity? = null

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {}

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    this.activity = binding.activity;
  }

  override fun onDetachedFromActivity() {
    this.activity = null
  }

  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    onAttachedToEngine(binding.applicationContext, binding.binaryMessenger)
  }

  private fun onAttachedToEngine(applicationContext: Context, messenger: BinaryMessenger) {
    this.applicationContext = applicationContext
    methodChannel = MethodChannel(messenger, "flutter.goolpe.com/methods")
    eventChannel = EventChannel(messenger, "flutter.goolpe.com/events")
    eventChannel!!.setStreamHandler(this)
    methodChannel!!.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    applicationContext = null
    methodChannel!!.setMethodCallHandler(null)
    methodChannel = null
    eventChannel!!.setStreamHandler(null)
    eventChannel = null
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when(call.method){
      "stateGPS" -> result.success(checkGPSState())
      "requestGPS" -> result.success(requestGPS)
      else -> result.notImplemented()
    }
  }

  override fun onListen(arguments: Any?, events: EventSink?) {
    chargingStateChangeReceiver = createChargingStateChangeReceiver(events)
    applicationContext!!.registerReceiver(
            chargingStateChangeReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
  }

  override fun onCancel(arguments: Any?) {
    applicationContext!!.unregisterReceiver(chargingStateChangeReceiver)
    chargingStateChangeReceiver = null
  }

  private val requestGPS: Boolean
    private get() {
      
      //request gps
      val locationRequest = LocationRequest.create()
      locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

      //create builder for gps alert
      val builder = LocationSettingsRequest.Builder()
          .addLocationRequest(locationRequest)
          .setAlwaysShow(true);

      val result = LocationServices.getSettingsClient(applicationContext!!).checkLocationSettings(builder.build())

      result.addOnCompleteListener { task ->
        try {
          val response = task.getResult(ApiException::class.java)
          // All location settings are satisfied. The client can initialize location
        } catch (exception: ApiException) {
          when (exception.statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> 
              try {
                // Cast to a resolvable exception.
                val resolvable = exception as ResolvableApiException
                // Show the dialog by calling startResolutionForResult(),
                resolvable.startResolutionForResult(
                        activity,
                        LocationRequest.PRIORITY_HIGH_ACCURACY)
              } 
              catch (e: SendIntentException) {} 
              catch (e: ClassCastException) {}
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
          }
        }
      }
      return checkGPSState()
    }

  private fun createChargingStateChangeReceiver(events: EventSink?): BroadcastReceiver {
    return object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action!! == "android.location.PROVIDERS_CHANGED") {
          val isGPS = checkGPSState()

          if(isGPSEnabled == null || isGPSEnabled != isGPS){
            isGPSEnabled = isGPS
            events!!.success(isGPS)
          }
        }
      }
    }
  }

  private fun checkGPSState(): Boolean{
      val locationManager =
              applicationContext!!.getSystemService(LOCATION_SERVICE) as LocationManager

      return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }
}