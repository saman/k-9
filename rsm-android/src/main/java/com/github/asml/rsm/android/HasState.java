package com.github.asml.rsm.android;


import com.github.asml.rsm.android.models.Device;


class HasState implements Publishable {
    private Device device;
    private String action = "has-state";

    public HasState() {
    }

    public HasState(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public String getAction() {
        return action;
    }
}
