package com.lenala.azure.functions;

import com.lenala.azure.functions.ExecutionContext;
import com.lenala.azure.functions.annotation.*;

public class Timer {
    @FunctionName("Timer")
    @QueueOutput(name = "myQueueItem", queueName = "walkthrough", connection = "AzureWebJobsStorage")
    public String functionHandler(@TimerTrigger(name = "timerInfo", schedule = "*/30 * * * * *") String timerInfo, final ExecutionContext executionContext) {
        executionContext.getLogger().info("Timer trigger input: " + timerInfo);
        return "From timer: \"" + timerInfo + "\"";
    }
}
