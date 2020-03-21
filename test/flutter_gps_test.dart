import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_gps/flutter_gps.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter.goolpe.com/methods');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return false;
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getGPSState', () async {
    expect(await FlutterGPS().stateGPS, false);
  });

  test('getGPSRequest', () async {
    expect(await FlutterGPS().requestGPS, false);
  });
}
