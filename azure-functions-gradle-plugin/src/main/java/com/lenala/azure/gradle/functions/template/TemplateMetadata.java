package com.lenala.azure.gradle.functions.template;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateMetadata {
    private String name;

    private List<String> userPrompt;

    @JsonGetter
    public String getName() {
        return name;
    }

    @JsonSetter
    public void setName(String name) {
        this.name = name;
    }

    @JsonGetter
    public List<String> getUserPrompt() {
        return userPrompt;
    }

    @JsonSetter
    public void setUserPrompt(List<String> userPrompt) {
        this.userPrompt = userPrompt;
    }
}
