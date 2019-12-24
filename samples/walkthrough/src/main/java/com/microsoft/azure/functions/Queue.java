package com.microsoft.azure.functions;

import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

public class Queue {
    @FunctionName("Queue")
    public void functionHandler(@QueueTrigger(name = "myQueueItem", queueName = "walkthrough", connection = "AzureWebJobsStorage") String myQueueItem, final ExecutionContext executionContext) {
        executionContext.getLogger().info("Queue trigger input: " + myQueueItem);
    }
}
