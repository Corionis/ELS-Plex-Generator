package com.corionis.els_plex_generator.repository;

import com.corionis.els_plex_generator.Context;
import com.corionis.els_plex_generator.MungeException;
import com.corionis.els_plex_generator.Utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The type Repository
 */
public class Repository
{
    public static final boolean NO_VALIDATE = false;
    public static final int PUBLISHER = 1;
    public static final int SUBSCRIBER = 2;
    public static final int HINT_SERVER = 3;
    public static final boolean VALIDATE = true;
    public final String SUB_EXCLUDE = "ELS-SUBSCRIBER-SKIP_";

    private String jsonFilename = "";
    private LibraryData libraryData = null;
    private int purpose = -1;

    private transient Context context;
    private transient Logger logger = LogManager.getLogger("applog");

    /**
     * Instantiate a Repository with a purpose
     * @param context The Context
     * @param purpose One of PUBLISHER, SUBSCRIBER, HINT_SERVER
     */
    public Repository(Context context, int purpose)
    {
        this.context = context;
        this.purpose = purpose;
    }

    /**
     * Sets LibraryData file.
     *
     * @param jsonFilename of the LibraryData file
     */
    public void setJsonFilename(String jsonFilename)
    {
        this.jsonFilename = jsonFilename;
    }

    /**
     * Gets LibraryData.
     *
     * @return the library
     */
    public LibraryData getLibraryData()
    {
        return libraryData;
    }

    /**
     * Get file separator for writing
     *
     * @return file separator string, may be multiple characters, e.g. \\
     * @throws MungeException
     */
    public String getWriteSeparator()
    {
        return Utils.getFileSeparator(getLibraryData().libraries.flavor);
    }

    /**
     * Normalize all JSON paths based on "flavor"
     */
    public void normalize() throws MungeException
    {
        if (getLibraryData() != null)
        {
            // if listen is empty use host
            if (getLibraryData().libraries.listen == null ||
                    getLibraryData().libraries.listen.length() < 1)
            {
                getLibraryData().libraries.listen = getLibraryData().libraries.host;
            }

            // set default timeout
            if (getLibraryData().libraries.timeout < 0)
                getLibraryData().libraries.timeout = 15; // default connection time-out if not defined

            String flavor = getLibraryData().libraries.flavor.toLowerCase();
            if (!flavor.equalsIgnoreCase(Libraries.LINUX) && !flavor.equalsIgnoreCase(Libraries.MAC) && !flavor.equalsIgnoreCase(Libraries.WINDOWS))
                throw new MungeException(context.cfg.gs("Repository.flavor.is.not.linux.mac.or.windows") + flavor);
            String from = "";
            String to = "";
            if (flavor.equalsIgnoreCase(Libraries.LINUX) || flavor.equalsIgnoreCase(Libraries.MAC))
            {
                from = "\\\\";
                to = "/";
            }
            else if (flavor.equalsIgnoreCase(Libraries.WINDOWS))
            {
                    from = "/";
                    to = "\\\\";
            }

            // temporary files location
            if (getLibraryData().libraries.temp_location != null)
            {
                String path = getLibraryData().libraries.temp_location;
                if (path.startsWith("~")) // is it relative to the user's home directory?
                {
                    path = System. getProperty("user.home") + path.substring(1);
                    getLibraryData().libraries.temp_location = path;
                }
            }

            if (getLibraryData().libraries.bibliography != null)
            {
                for (Library lib : getLibraryData().libraries.bibliography)
                {
                    if (lib.sources != null)
                    {
                        for (int i = 0; i < lib.sources.length; ++i)
                        {
                            if (lib.sources[i] != null && lib.sources[i].length() > 0)
                                lib.sources[i] = normalizeSubst(lib.sources[i], from, to);
                            else
                                throw new MungeException("Malformed JSON");
                        }
                    }
                    if (lib.items != null)
                    {
                        // setup the hash map for this library
                        if (lib.itemMap == null)
                            lib.itemMap = ArrayListMultimap.create();
                        else
                            lib.itemMap.clear();

                        for (int i = 0; i < lib.items.size(); ++i)
                        {
                            Item item = lib.items.elementAt(i);
                            item.setItemPath(normalizeSubst(item.getItemPath(), from, to));
                            item.setFullPath(normalizeSubst(item.getFullPath(), from, to));

                            // add itemPath & the item's index in the Vector to the hash map
                            String key = item.getItemPath();
                            if (!getLibraryData().libraries.case_sensitive)
                            {
                                key = key.toLowerCase();
                            }
                            lib.itemMap.put(Utils.pipe(this, key), i);
                        }
                    }
                }
            }
        }
    }

    /**
     * Normalize a path with a specific path separator character.
     *
     * @param path The path to normalize
     * @param from The previous path separator character
     * @param to   The new path separator character
     * @return String normalized path
     */
    private String normalizeSubst(String path, String from, String to)
    {
        if (from.equals("\\"))
                from = "\\\\";
        if (to.equals("\\"))
            to = "\\\\";
        return path.replaceAll(from, to).replaceAll("\\|", to);
    }

    /**
     * Read library.
     *
     * @param filename The JSON Libraries filename
     * @return boolean True if file is a valid ELS repository, false if not a repository
     * @throws MungeException the els exception
     */
    public boolean read(String filename, boolean printLog) throws MungeException
    {
        boolean valid = false;
        try
        {
            String json;
            if (getLibraryData() != null)
                libraryData = null;
            Gson gson = new Gson();
            if (printLog)
                logger.info("Reading Library file " + filename);
            setJsonFilename(filename);
            json = new String(Files.readAllBytes(Paths.get(filename)));
            libraryData = gson.fromJson(json, LibraryData.class);
            if (libraryData != null && libraryData.libraries != null)
            {
                normalize();
                if (printLog)
                    logger.info("Read \"" + libraryData.libraries.description + "\" successfully");
                valid = true;
            }
        }
        catch (IOException ioe)
        {
            throw new MungeException("Exception while reading library " + filename + " trace: " + Utils.getStackTrace(ioe));
        }
        return valid;
    }

}
