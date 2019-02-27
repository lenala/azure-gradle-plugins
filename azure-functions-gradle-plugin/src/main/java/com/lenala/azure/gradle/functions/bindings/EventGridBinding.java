package com.lenala.azure.gradle.functions.bindings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.functions.annotation.EventGridTrigger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EventGridBinding extends BaseBinding {
    public static final String EVENT_GRID_TRIGGER = "eventGridTrigger";

    public EventGridBinding(final EventGridTrigger eventGridTrigger) {
        super(eventGridTrigger.name(), EVENT_GRID_TRIGGER, Direction.IN, eventGridTrigger.dataType());
    }
}
