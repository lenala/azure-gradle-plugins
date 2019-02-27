package com.lenala.azure.gradle.functions.bindings;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.microsoft.azure.functions.annotation.NotificationHubOutput;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotificationHubBinding extends BaseBinding {
    public static final String NOTIFICATION_HUB = "notificationHub";

    private String tagExpression = "";

    private String hubName = "";

    private String connection = "";

    private String platform = "";

    public NotificationHubBinding(final NotificationHubOutput hubOutput) {
        super(hubOutput.name(), NOTIFICATION_HUB, Direction.OUT, hubOutput.dataType());

        tagExpression = hubOutput.tagExpression();
        hubName = hubOutput.hubName();
        connection = hubOutput.connection();
        platform = hubOutput.platform();
    }

    @JsonGetter
    public String getTagExpression() {
        return tagExpression;
    }

    @JsonGetter
    public String getHubName() {
        return hubName;
    }

    @JsonGetter
    public String getConnection() {
        return connection;
    }

    @JsonGetter
    public String getPlatform() {
        return platform;
    }
}
