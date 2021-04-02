package com.springprojs.webscraper.models;

import java.util.Hashtable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Reviewer {

    private final UUID id;
    private final String name;
    private final String avatarPath;
    private final Hashtable<String, String> emotion;
    
    public Reviewer(@JsonProperty("id") UUID id,
                    @JsonProperty("name") String name,
                    @JsonProperty("avatarpath") String avatarPath,
                    @JsonProperty("emotion") Hashtable<String, String> emotion) {
        this.id = id;
        this.name = name;
        this.avatarPath = avatarPath;
        this.emotion = emotion;
    }

    public UUID getId( ){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public Hashtable<String, String> getEmotion() {
        return emotion;
    }
}
