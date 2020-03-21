import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter_gps/flutter_gps.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _isGPSEnabled = false;
  StreamSubscription<bool> _gpsSubscription;
  FlutterGPS gps = FlutterGPS();

  @override
  void initState() {
    super.initState();
    initGPSState();
  }

  Future<void> initGPSState() async {
    //get current state of gps
    bool _state = await gps.stateGPS;
    _upadateGPS(_state);

    //subscribe to get gps state whenever the this state changes
    _gpsSubscription = gps.onGPSStateChanged.listen((bool newState){  
      _upadateGPS(newState);
    });
  }

  void _upadateGPS(bool state){
    setState(() => _isGPSEnabled = state);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Text('GPS status: $_isGPSEnabled'),
              !_isGPSEnabled
              ? RaisedButton(
                child: Text('requestGPS'),
                onPressed: () async => await gps.requestGPS,
              ) : SizedBox()
            ],
          )
        )
      ),
    );
  }

  @override
  void dispose() {
    _gpsSubscription?.cancel();
    super.dispose();
  }
}
