package com.microsoft.azure.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueOutput;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class Timer {
    @FunctionName("Timer")
    @QueueOutput(name = "myQueueItem", queueName = "walkthrough", connection = "AzureWebJobsStorage")
    public String functionHandler(@TimerTrigger(name = "timerInfo", schedule = "*/30 * * * * *") String timerInfo, final ExecutionContext executionContext) {
        executionContext.getLogger().info("Timer trigger input: " + timerInfo);
        return "From timer: \"" + timerInfo + "\"";
    }
}
