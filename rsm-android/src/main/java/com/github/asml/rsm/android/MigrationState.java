package com.github.asml.rsm.android;

import com.github.asml.rsm.android.models.Device;

class MigrationState implements Publishable {
    private Device device;

    public MigrationState() {
    }

    public MigrationState(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    @Override public String getAction() {
        return "migration";
    }
}
