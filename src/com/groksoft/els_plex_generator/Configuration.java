package com.groksoft.els_plex_generator;

/**
 * Configuration
 * <p>
 * Contains all command-line options and any other application-level configuration.
 */
public class Configuration
{
    private final String PROGRAM_VERSION = "2.0.0";
    private String consoleLevel = "debug";  // Levels: ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, and OFF
    private String debugLevel = "info";
    private String inputFilename = "";
    private String logFilename = "els.log";
    private String[] originalArgs;
    private String outputFilename = "";
    private String server = "";
    private String token = "";

    public static final int RENAME_NONE = 0;
    public static final int RENAME_FILES = 1;
    public static final int RENAME_DIRECTORIES = 2;
    public static final int RENAME_BOTH = 3;

    /**
     * Instantiates a new Configuration
     */
    public Configuration()
    {
    }

    public String banner()
    {
        return "ELS-Plex-Generator, version " + PROGRAM_VERSION;
    }

    /**
     * Dump the configuration
     */
    public void dump(org.apache.logging.log4j.Logger logger)
    {
        String msg = "Arguments: ";
        for (int index = 0; index < originalArgs.length; ++index)
        {
            msg = msg + originalArgs[index] + " ";
        }
        logger.info(msg);

        logger.info("  cfg: -c Console logging level = " + getConsoleLevel());
        logger.info("  cfg: -d Debug logging level = " + getDebugLevel());
        logger.info("  cfg: -f Log filename = " + getLogFilename());
        logger.info("  cfg: -i Input filename = " + getInputFilename());
        logger.info("  cfg: -o Output filename = " + getOutputFilename());
        logger.info("  cfg: -s Plex Media Server = " + getServer());
        logger.info("  cfg: -t Authentication token = " + getToken());
    }

    /**
     * Gets console level
     *
     * @return the console level
     */
    public String getConsoleLevel()
    {
        return consoleLevel;
    }

    /**
     * Sets console level
     *
     * @param consoleLevel the console level
     */
    public void setConsoleLevel(String consoleLevel)
    {
        this.consoleLevel = consoleLevel;
    }

    /**
     * Gets debug level
     *
     * @return the debug level
     */
    public String getDebugLevel()
    {
        return debugLevel;
    }

    /**
     * Sets debug level
     *
     * @param debugLevel the debug level
     */
    public void setDebugLevel(String debugLevel)
    {
        this.debugLevel = debugLevel;
    }

    public String getInputFilename()
    {
        return inputFilename;
    }

    public void setInputFilename(String inputFilename)
    {
        this.inputFilename = inputFilename;
    }

    /**
     * Gets log filename
     *
     * @return the log filename
     */
    public String getLogFilename()
    {
        return logFilename;
    }

    /**
     * Sets log filename
     *
     * @param logFilename the log filename
     */
    public void setLogFilename(String logFilename)
    {
        this.logFilename = logFilename;
    }

    public String getOutputFilename()
    {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename)
    {
        this.outputFilename = outputFilename;
    }

    public int getRenamingType()
    {
        return this.RENAME_BOTH;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public void help()
    {
        System.out.println(banner());
        System.out.println("Required arguments:");
        System.out.println("  -s | --server Plex Media Server hostname or IP address[:]port");
        System.out.println("  -t | --token Authentication X-Plex-Token");
        System.out.println("  -o | --output-file Output filename");
        System.out.println("Optional arguments:");
        System.out.println("  -c | --console-level Console logging level");
        System.out.println("  -d | --debug-level Debug logging level");
        System.out.println("  -f | --log-file Log filename");
        System.out.println("  -i | --input-file Input filename for user-definable elements");
        System.out.println("Runtime example: ");
        System.out.println("  java -jar ELS-Plex-Generator.jar -s 192.168.2.1:32400 -t syMEox_DcT_4aIXfy3-J -o publisher.json");
        System.out.println("For the X-Plex-Token see https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/");
    }

    public boolean isCrossCheck()
    {
        return true;
    }

    public boolean isDryRun()
    {
        return false;
    }

    public boolean isSelectedLibrary(String name)
    {
        return true;
    }

    public boolean isSpecificLibrary()
    {
        return false;
    }

    /**
     * Parse command line
     * <p>
     * This populates the rest.
     *
     * @param args the args
     * @throws MungerException the els exception
     */
    public void parseCommandLine(String[] args) throws MungerException
    {
        int index;
        originalArgs = args;

        for (index = 0; index < args.length; ++index)
        {
            switch (args[index])
            {
                case "-c":                                             // console level
                case "--console-level":
                    if (index <= args.length - 2)
                    {
                        setConsoleLevel(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -c requires a level, trace, debug, info, warn, error, fatal, or off");
                    }
                    break;
                case "-d":                                             // debug level
                case "--debug-level":
                    if (index <= args.length - 2)
                    {
                        setDebugLevel(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -d requires a level, trace, debug, info, warn, error, fatal, or off");
                    }
                    break;
                case "-f":                                             // log filename
                case "--log-file":
                    if (index <= args.length - 2)
                    {
                        setLogFilename(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -f requires a log filename");
                    }
                    break;
                case "-i":                                             // input filename
                case "--input-file":
                    if (index <= args.length - 2)
                    {
                        setInputFilename(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -i requires an input filename");
                    }
                    break;
                case "-o":                                             // output filename
                case "--output-file":
                    if (index <= args.length - 2)
                    {
                        setOutputFilename(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -o requires an output filename");
                    }
                    break;
                case "-s":                                             // server:port
                case "--server":
                    if (index <= args.length - 2)
                    {
                        setServer(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -s requires a server:port");
                    }
                    break;
                case "-t":                                             // authentication token
                case "--token":
                    if (index <= args.length - 2)
                    {
                        setToken(args[index + 1]);
                        ++index;
                    }
                    else
                    {
                        throw new MungerException("Error: -t requires an authentication token");
                    }
                    break;
                case "-h":
                case "--help":
                    help();
                    System.exit(1);
                    break;
                default:
                    throw new MungerException("Error: unknown option " + args[index]);
            }
        }

        if (getServer().length() < 1)
            throw new MungerException("-s server:port required for Plex Media Server");

        if (getToken().length() < 1)
            throw new MungerException("-t token required for Plex Media Server X-Plex-Token");

        if (getOutputFilename().length() < 1)
            throw new MungerException("-o output filename required");

        if (logFilename.length() < 1)
        {
            setLogFilename("ELS-Plex-Generator.log");
        }
        if (consoleLevel.length() < 1)
        {
            setConsoleLevel("DEBUG");
        }
        if (debugLevel.length() < 1)
        {
            setDebugLevel("DEBUG");
        }
    }

    /**
     * Gets the export collection filename
     *
     * @return the export filename
     */
    public String getExportCollectionFilename()
    {
        // not used in this program
        return "";
    }

    /**
     * Gets the export text filename
     *
     * @return exportTextFilename the export text filename
     */
    public String getExportTextFilename()
    {
        // not used in this program
        return "";
    }
}
