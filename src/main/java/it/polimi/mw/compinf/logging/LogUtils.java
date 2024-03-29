package it.polimi.mw.compinf.logging;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Class to handle logging
 */
public class LogUtils {

    public static final void setLogLevel() {
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        Logger.getRootLogger().setLevel(Level.OFF);
    }

}