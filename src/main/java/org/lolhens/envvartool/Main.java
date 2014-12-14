package org.lolhens.envvartool;

/**
 * Created by LolHens on 14.12.2014.
 */
public class Main {
    public static final String version = "1.0";
    public static final String envVarPath = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";

    public static void main(String[] args) {
        new EnvVarManager();
    }
}
