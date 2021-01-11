package com.fsck.k9.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SearchState {
    private String query;

    public SearchState() {
    }

    public SearchState(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @JsonIgnore
    @Override public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return "{" +
                "\"query\":\"" + query + '"' +
                '}';
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
