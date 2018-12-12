
package util;


public enum Environment {
    
    INSTANCE;
    
    public String getJarPath() {
        return System.getProperty("application.jar", "../../tessera-app/target/tessera-app-0.8-SNAPSHOT-app.jar");
    }
    
}
