package com.corionis.els_plex_generator;

import com.corionis.els_plex_generator.repository.Libraries;
import com.corionis.els_plex_generator.repository.Repository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * The type Utils. Various utility methods.
 */
public class Utils
{
    private static Logger logger = LogManager.getLogger("applog");

    public static final String WINDOWS = "windows";
    public static final String LINUX = "linux";
    public static final String APPLE = "apple";

    /**
     * Do not instantiate
     */
    private Utils()
    {
        // do not instantiate
    }

    /**
     * Get the file separator for the flavor of operating system
     *
     * @param flavor
     * @return String containing matching file separator character
     * @throws MungeException
     */
    public static synchronized String getFileSeparator(String flavor)
    {
        String separator = "";
        if (flavor.equalsIgnoreCase(Libraries.WINDOWS))
        {
            separator = "\\";
        }
        else if (flavor.equalsIgnoreCase(Libraries.LINUX))
        {
            separator = "/";
        }
        else if (flavor.equalsIgnoreCase(Libraries.MAC))
        {
            separator = "/";
        }
        return separator;
    }

    /**
     * Get the line separator for the flavor of operating system
     *
     * @param flavor
     * @return String containing matching line separator string
     * @throws MungeException
     */
    public static String getLineSeparator(String flavor) throws MungeException
    {
        String separator;
        if (flavor.equalsIgnoreCase(WINDOWS))
        {
            separator = "\r\n";
        }
        else if (flavor.equalsIgnoreCase(LINUX))
        {
            separator = "\n";
        }
        else if (flavor.equalsIgnoreCase(APPLE))
        {
            separator = "\n";
        }
        else
        {
            throw new MungeException("unknown flavor '" + flavor + "'");
        }
        return separator;
    }

    /**
     * Gets stack trace
     *
     * @param throwable the throwable
     * @return the stack trace
     */
    public static String getStackTrace(final Throwable throwable)
    {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * Parse the host from a site string
     * <p>
     * Expected format: [hostname|IP address]:[port number]
     *
     * @param location Site string
     * @return String host
     */
    public static String parseHost(String location)
    {
        String host = null;
        String[] a = location.split(":");
        if (a.length >= 1)
        {
            host = a[0];
        }
        if (host.length() < 1)
            host = "localhost";
        return host;
    }

    /**
     * Replace source path separators with pipe character for comparison
     *
     * @param repo Repository of source of path
     * @param path Path to modify with pipe characters
     * @return String Modified path
     * @throws MungeException
     */
    public static String pipe(Repository repo, String path) throws MungeException
    {
        String p = path.replaceAll(repo.getWriteSeparator(), "|");
        return p;
    }

}
