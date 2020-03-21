// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/services.dart';
import 'package:meta/meta.dart' show visibleForTesting;

/// API for accessing information about the gps
/// app is currently running on.
class FlutterGPS {
  /// Initializes the plugin and starts listening for potential platform events.
  factory FlutterGPS() {
    if (_instance == null) {
      final MethodChannel methodChannel =
          const MethodChannel('flutter.goolpe.com/methods');
      final EventChannel eventChannel =
          const EventChannel('flutter.goolpe.com/events');
      _instance = FlutterGPS.private(eventChannel, methodChannel);
    }
    return _instance;
  }

  /// This constructor is only used for testing and shouldn't be accessed by
  /// users of the plugin. It may break or change at any time.
  @visibleForTesting
  FlutterGPS.private(this._eventChannel, this._methodChannel);

  static FlutterGPS _instance;
  final EventChannel _eventChannel;
  final MethodChannel _methodChannel;
  Stream<bool> _onGPSStateChanged;

  /// Returns the current gps status.
  Future<bool> get stateGPS => _methodChannel
      .invokeMethod<bool>('stateGPS')
      .then<bool>((dynamic result) => result);

  /// Request gps and returns the current gps status.
  Future<bool> get requestGPS => _methodChannel
      .invokeMethod<bool>('requestGPS')
      .then<bool>((dynamic result) => result);

  /// Fires whenever the gps status changes.
  Stream<bool> get onGPSStateChanged {
    if (_onGPSStateChanged == null) {
      _onGPSStateChanged = _eventChannel
        .receiveBroadcastStream()
        .map((dynamic state) => state);
    }
    return _onGPSStateChanged;
  }
}
