package com.github.asml.rsm.android.interfaces;

import com.github.asml.rsm.android.models.Device;

public interface OnDeviceJoinListener {
    void onDeviceJoin(String modelName, Device device);
}
