package lenala.azure.functions;

import lenala.azure.functions.ExecutionContext;
import lenala.azure.functions.annotation.*;

public class Queue {
    @FunctionName("Queue")
    public void functionHandler(@QueueTrigger(name = "myQueueItem", queueName = "walkthrough", connection = "AzureWebJobsStorage") String myQueueItem, final ExecutionContext executionContext) {
        executionContext.getLogger().info("Queue trigger input: " + myQueueItem);
    }
}
