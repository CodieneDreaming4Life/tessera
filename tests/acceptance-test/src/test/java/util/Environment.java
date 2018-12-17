
package util;


public enum Environment {
    
    INSTANCE;
    
    public String getJarPath() {
        return System.getProperty("application.jar", "../../tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");
    }
    
    public String getEnclaveJarPath() {
        return System.getProperty("enclave.jar", "../..encryption/enclave-jaxrs/target/enclave-jaxrs-0.8-SNAPSHOT-app.jar");
    }
    
}
