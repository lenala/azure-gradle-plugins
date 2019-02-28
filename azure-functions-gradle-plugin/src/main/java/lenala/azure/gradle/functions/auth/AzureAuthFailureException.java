package lenala.azure.gradle.functions.auth;

public class AzureAuthFailureException extends Exception {
    public AzureAuthFailureException(String message) {
        super(message);
    }
}
