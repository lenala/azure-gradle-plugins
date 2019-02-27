package com.lenala.azure.functions;

import com.lenala.azure.functions.ExecutionContext;
import com.lenala.azure.functions.annotation.*;

public class Queue {
    @FunctionName("Queue")
    public void functionHandler(@QueueTrigger(name = "myQueueItem", queueName = "walkthrough", connection = "AzureWebJobsStorage") String myQueueItem, final ExecutionContext executionContext) {
        executionContext.getLogger().info("Queue trigger input: " + myQueueItem);
    }
}
