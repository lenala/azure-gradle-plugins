package com.lenala.azure.functions;

import com.lenala.azure.functions.annotation.*;
import com.lenala.azure.functions.ExecutionContext;

/**
 * Hello function with HTTP Trigger.
 */
public class Http {
    @FunctionName("hello")
    public String hello(@HttpTrigger(name = "req", methods = {"post"}, authLevel = AuthorizationLevel.ANONYMOUS) String req,
                        ExecutionContext context) {
        return String.format("Hello, %s!\n", req);
    }
}
