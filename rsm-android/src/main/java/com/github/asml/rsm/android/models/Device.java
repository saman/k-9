package com.github.asml.rsm.android.models;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Device {
    @JsonProperty("_id")
    private String _id;
    private String name;
    private List<String> models = new ArrayList<>();

    @JsonProperty("models_has_state")
    private List<String> modelsHasState = new ArrayList<>();

    public Device() {
    }

    public Device(String name) {
        this.name = name;
        this._id = UUID.randomUUID().toString();
    }

    @JsonIgnore
    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public List<String> getModels() {
        return models;
    }

    @JsonIgnore
    public List<String> getModelsHasState() {
        return modelsHasState;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public void addModel(String model) {
        models.add(model);
    }

    public void addModelHasState(String model) {
        modelsHasState.add(model);
    }

    public void removeHasState(String model) {
        modelsHasState.remove(model);
    }
}
