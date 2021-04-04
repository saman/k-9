package com.github.asml.rsm.android;


import com.github.asml.rsm.android.models.Device;


class HasState implements Publishable {
    private Device device;
    private boolean value;
    private final String action = "has-state";

    public HasState() {
    }

    public HasState(Device device, boolean value) {
        this.device = device;
        this.value = value;
    }

    public Device getDevice() {
        return device;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String getAction() {
        return action;
    }
}
