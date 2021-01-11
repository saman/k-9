package com.github.asml.rsm.android.interfaces;

import com.github.asml.rsm.android.models.Device;

public interface OnStateMigrationListener {
    void onStateMigration(String modelName, Device device);
}
