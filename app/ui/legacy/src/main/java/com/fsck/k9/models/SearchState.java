package com.fsck.k9.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SearchState {
    private String query;

    public boolean isSubmit() {
        return submit;
    }

    private boolean submit;

    public SearchState() {
    }

    public SearchState(String query, boolean submit) {
        this.query = query;
        this.submit = submit;
    }

    public String getQuery() {
        return query;
    }

    @JsonIgnore
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "{" + "\"query\":\"" + query + "\", \"submit\":" + (submit ? "true" : "false") + '}';
    }

    @JsonIgnore
    public static SearchState fromJsonString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, SearchState.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new SearchState();
    }
}
