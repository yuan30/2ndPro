package com.example.negativeion;

public interface IMqttResponse {
    void receiveMessage(String topic, String message);
    void connectState(boolean connectState);
}
