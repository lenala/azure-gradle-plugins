package lenala.azure.gradle.webapp.configuration;

public class Authentication {
    public AuthenticationType type;
    public String file;
    public String client;
    public String tenant;
    public String key;
    public String certificate;
    public String certificatePassword;
    public String environment = "AZURE";


    public String getFile() {
        return file;
    }

    public String getClient() {
        return client;
    }

    public String getTenant() {
        return tenant;
    }

    public String getKey() {
        return key;
    }

    public String getEnvironment() {
        return environment;
    }

    public AuthenticationType getType() {
        return type;
    }

    public String getCertificate() {
        return certificate;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setType(AuthenticationType type) {
        this.type = type;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
