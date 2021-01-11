package com.github.asml.rsm.android;

interface OnMessageListener {
    void onMessage(String topic, String payload, String deviceId);
}
