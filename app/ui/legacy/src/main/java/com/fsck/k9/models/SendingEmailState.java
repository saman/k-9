package com.fsck.k9.models;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SendingEmailState {
    private String from;
    private List<String> to;
    private String subject;
    private String body;

    public SendingEmailState() {
    }

    public SendingEmailState(String from, List<String> to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public List<String> getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
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
                "\"from\":\"" + from + '"' +
                ", \"to\"" + to + '"' +
                ", \"subject\"" + subject + '"' +
                ", \"body\"" + body + '"' +
                '}';
    }

    @JsonIgnore
    public static SendingEmailState fromJsonString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, SendingEmailState.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new SendingEmailState();
    }
}
