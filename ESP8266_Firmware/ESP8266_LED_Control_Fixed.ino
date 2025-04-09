#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

const char* ssid = "YourWiFiSSID";
const char* password = "YourWiFiPassword";

ESP8266WebServer server(80);
const int ledPin = 2; // Built-in LED (D4 on NodeMCU)

void handleControl() {
  digitalWrite(ledPin, LOW); // Turn LED on (active low)
  delay(500); // Keep on for 500ms
  digitalWrite(ledPin, HIGH); // Turn LED off
  server.send(200, "text/plain", "LED toggled");
}

void handleScan() {
  server.send(200, "text/plain", "ESP8266 Device");
}

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, HIGH); // Start with LED off

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  server.on("/control", handleControl);
  server.on("/scan", handleScan);
  server.begin();
}

void loop() {
  server.handleClient();
}
