package com.github.asml.rsm.android;


import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.asml.rsm.android.interfaces.OnDeviceJoinListener;
import com.github.asml.rsm.android.interfaces.OnDeviceLeaveListener;
import com.github.asml.rsm.android.interfaces.OnStateMigrationListener;
import com.github.asml.rsm.android.interfaces.OnStateReceiveListener;
import com.github.asml.rsm.android.interfaces.OnStateRequestListener;
import com.github.asml.rsm.android.models.Config;
import com.github.asml.rsm.android.models.Device;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class RuntimeStateMigration implements OnOnlineListener, OnMessageListener {
    private static Context appContext;
    private static Config config;
    private static Api api;
    private static Device device;
    private static RuntimeStateMigration INSTANCE;
    private static final String TAG = "RuntimeStateMigration";
    ObjectMapper mapper = new ObjectMapper();
    private final JsonSchemaValidator validator;
    private final List<Model> models = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private OnStateReceiveListener onStateReceiveListener;
    private OnStateRequestListener onStateRequestListener;
    private OnDeviceJoinListener onDeviceJoinListener;
    private OnDeviceLeaveListener onDeviceLeaveListener;
    private OnStateMigrationListener onStateMigrationListener;

    private RuntimeStateMigration() {
        api = new Api(appContext, config.getServer(), this, this);
        validator = new JsonSchemaValidator(appContext);
    }

    public static RuntimeStateMigration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RuntimeStateMigration();
        }
        return INSTANCE;
    }

    public static void init(Context context, Config configuration) {
        config = configuration;
        appContext = context;

        device = new Device(config.getName());
    }

    public boolean addModel(String content) throws JsonProcessingException, IllegalStateException {
        if (!validator.isModelValid(content)) {
            throw new IllegalStateException("This model is not valid");
        }

        StateModel model = mapper.readValue(content, StateModel.class);
        if (getModel(content) != null) {
            throw new IllegalStateException("model is already added");
        }
        Model m = new Model(model.info.title, content, device.getId());
        Log.d(TAG, "addModel: " + m.getName());
        return models.add(m);
    }

    public void introduce() {
        if (models.isEmpty()) {
            throw new IllegalStateException("At least one model needs to be added.");
        }
        api.run(device, () -> {
            for (Model m : models) {
                Log.d(TAG, "onConnected: Publishing model: " + m.getName());
                api.publishDevice(device, m);
            }
        });

    }

    public void getStateDevice(String modelName, String deviceId) {
        api.getStateDevice(modelName, deviceId, device);
    }

    public void setState(String modelName, String state) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        model.setState(state);
        api.publishHasState(modelName, true, device);
    }

    public void setHasState(String modelName, boolean value) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }

        api.publishHasState(modelName, value, device);
    }

    public void sendState(String modelName, String deviceId) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        api.publishState(modelName, deviceId, device, model.getState());
    }

    public void setMigration(String modelName, String deviceId) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        api.publishMigration(modelName, deviceId, device);
    }

    public Device getDevice() {
        return device;
    }

    public List<Model> getModels() {
        return models;
    }

    public List<Device> getDevices(String modelName) {
        return getDevices(modelName, false);
    }

    public List<Device> getDevices(String modelName, boolean hasState) {
        Model model = getModel(modelName);
        if (model == null) {
            throw new IllegalStateException("Could not find the model " + modelName);
        }
        devices.stream().forEach(d -> Log.d(TAG, "getDevices: " + d.getName()));
        if (hasState) {
            return devices.stream().filter(d -> d.getModelsHasState().contains(modelName)).collect(Collectors.toList());
        }
        return devices.stream().filter(d -> d.getModels().contains(modelName)).collect(Collectors.toList());
    }

    private Model getModel(String name) {
        return models.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public void onMessage(String topic, String payload, String deviceId) {
        Log.d(TAG, "onMessage() called with: topic = [" + topic + "], payload = [" + payload + "], deviceId = [" +
                deviceId + "]");
        Model model = getModel(topic);
        if (model == null) {
            Log.d(TAG, "onMessage: Model is null for: " + topic);
            // I think we shouldn't throw an exception in a callback
            //throw new IllegalStateException("onMessage: Could not find the model " + topic);

            return;
        }
        try {
            JSONObject json = new JSONObject(payload);
            String action = json.getString("action");
            Log.d(TAG, "onMessage: action => " + action);
            if (action.equals("device")) {
                JavaType type = mapper.getTypeFactory().constructParametricType(State.class, DeviceState.class);
                State<DeviceState> state = mapper.readValue(payload, type);

                Device device = devices.stream().filter(d -> d.getId().equals(state.getData().getDevice().getId())).findFirst().orElse(null);
                if (device == null) {
                    devices.add(state.getData().getDevice());
                    device = devices.get(devices.size() - 1);
                }
                device.addModel(topic);

                if (state.getData().isNew()) {
                    if (onDeviceJoinListener != null) {
                        onDeviceJoinListener.onDeviceJoin(topic, device);
                    }
                    api.publishDeviceToNewDevice(device, RuntimeStateMigration.device, model);
                }
            } else if (action.equals("request-state")) {
                if (onStateRequestListener != null) {
                    State<RequestState> state = mapper.readValue(payload, new TypeReference<State<RequestState>>() {
                    });
                    onStateRequestListener.onStateRequest(topic, state.getData().getDevice());
                }


            } else if (action.equals("response-state")) {
                if (onStateReceiveListener != null) {
                    State<ResponseState> state = mapper.readValue(payload, new TypeReference<State<ResponseState>>() {
                    });
                    onStateReceiveListener.onStateReceive(
                            topic,
                            state.getData().getDevice(),
                            state.getData().getState(),
                            validator.isStateValid(model.getContent(), state.getData().getState())
                    );
                }

            } else if (action.equals("migration")) {
                if (onStateMigrationListener != null) {
                    State<MigrationState> state = mapper.readValue(payload, new TypeReference<State<MigrationState>>() {
                    });
                    onStateMigrationListener.onStateMigration(topic, state.getData().getDevice());
                }
            } else if (action.equals("has-state")) {
                State<HasState> state = mapper.readValue(payload, new TypeReference<State<HasState>>() {
                });
                Device device = devices.stream().filter(d -> d.getId().equals(state.getData().getDevice().getId())).findFirst().orElse(null);
                if (device == null) {
                    return;
                }
                if (state.getData().getValue() && !device.getModelsHasState().contains(topic)) {
                    device.addModelHasState(topic);
                } else if (!state.getData().getValue()) {
                    device.removeHasState(topic);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOnline(String deviceId, boolean isOnline) {
        Log.d(TAG, "onOnline() called with: deviceId = [" + deviceId + "], isOnline = [" + isOnline + "]");
        if (deviceId.equals(device.getId())) {
            return;
        }
        if (!isOnline) {
            for (int i = 0; i < devices.size(); i++) {
                Device d = devices.get(i);
                if (d.getId().equals(deviceId)) {
                    Log.d(TAG, "onOnline: " + d.getName() + " removed");
                    devices.remove(i);
                    if (onDeviceLeaveListener != null) {
                        onDeviceLeaveListener.onDeviceLeave(d);
                    }
                    return;
                }
            }
        }
    }

    public void setOnStateReceiveListener(OnStateReceiveListener listener) {
        this.onStateReceiveListener = listener;
    }

    public void setOnStateRequestListener(OnStateRequestListener listener) {
        this.onStateRequestListener = listener;
    }

    public void setOnDeviceJoinListener(OnDeviceJoinListener listener) {
        this.onDeviceJoinListener = listener;
    }

    public void setOnDeviceLeaveListener(OnDeviceLeaveListener listener) {
        this.onDeviceLeaveListener = listener;
    }

    public void setOnStateMigrationListener(OnStateMigrationListener listener) {
        this.onStateMigrationListener = listener;
    }

    public void removeCallbacks() {
        this.onDeviceJoinListener = null;
        this.onDeviceLeaveListener = null;
        this.onStateMigrationListener = null;
        this.onStateReceiveListener = null;
        this.onStateRequestListener = null;
    }
}
