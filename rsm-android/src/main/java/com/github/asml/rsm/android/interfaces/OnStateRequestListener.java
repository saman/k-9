package com.github.asml.rsm.android.interfaces;

import com.github.asml.rsm.android.models.Device;

public interface OnStateRequestListener {
    void onStateRequest(String modelName, Device device);
}
