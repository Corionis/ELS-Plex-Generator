package com.groksoft.els_plex_generator;

import com.groksoft.els_plex_generator.repository.Location;
import com.groksoft.els_plex_generator.repository.Renaming;
import com.groksoft.els_plex_generator.repository.Repository;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

/**
 * Main ELS Plex Generator program
 */
public class Main
{
    private Configuration cfg = null;
    private String flavor = "";
    private String hostname = "";
    private String lineSep = "";
    private Logger logger = null;
    private Repository repo = null;

    /**
     * Instantiates the Main application
     */
    public Main()
    {
    }

    /**
     * main() entry point
     *
     * @param args the input arguments
     */
    public static void main(String[] args)
    {
        Main elsGenerator = new Main();
        int returnValue = elsGenerator.process(args);
        System.exit(returnValue);
    } // main

    private String generateLocal() throws Exception
    {
        String value = "";
        CharSequence singleBack = new StringBuffer("\\");
        CharSequence doubleBack = new StringBuffer("\\\\");

        // generate host element
        if (repo != null && repo.getLibraryData().libraries.host != null)
            value = repo.getLibraryData().libraries.host;
        else
            value = hostname + ":50271";
        String json = "\t\t\"host\": \"" + value + "\"," + lineSep;

        // handle listen element if present
        if (repo != null && repo.getLibraryData().libraries.listen != null)
        {
            // don't add listen if it's the same as host
            if (!repo.getLibraryData().libraries.listen.equalsIgnoreCase(value))
            {
                value = repo.getLibraryData().libraries.listen;
                json += "\t\t\"listen\": \"" + value + "\"," + lineSep;
            }
        }

        // add terminal_allowed
        if (repo != null && repo.getLibraryData().libraries.terminal_allowed != null)
            value = repo.getLibraryData().libraries.terminal_allowed;
        else
            value = "true";
        json += "\t\t\"terminal_allowed\": \"" + value + "\"," + lineSep;

        // add key
        if (repo != null && repo.getLibraryData().libraries.key != null)
            value = repo.getLibraryData().libraries.key;
        else
        {
            UUID uuid = UUID.randomUUID();
            value = uuid.toString();
        }
        json += "\t\t\"key\": \"" + value + "\"," + lineSep;

        // add case_sensitive
        if (repo != null && repo.getLibraryData().libraries.case_sensitive != null)
            value = (repo.getLibraryData().libraries.case_sensitive) ? "true" : "false";
        else
            value = (isCaseSensitive(flavor) ? "true" : "false");
        json += "\t\t\"case_sensitive\": \"" + value + "\"," + lineSep;

        // add ignore_patterns
        if (repo != null && repo.getLibraryData().libraries.ignore_patterns != null && repo.getLibraryData().libraries.ignore_patterns.length > 0)
        {
            json += "\t\t\"ignore_patterns\": [" + lineSep;
            int c = 0;
            for (String patt : repo.getLibraryData().libraries.ignore_patterns)
            {
                if (c > 0)
                    json += "," + lineSep;
                patt = patt.replace(singleBack, doubleBack);
                json += "\t\t\t\"" + patt + "\"";
                ++c;
            }
            json += lineSep + "\t\t]," + lineSep;
        }
        else
        {
            json += "\t\t\"ignore_patterns\": [" + lineSep +
                    "\t\t\t\"desktop.ini\"," + lineSep +
                    "\t\t\t\"Thumbs.db\"" + lineSep +
                    "\t\t]," + lineSep;
        }

        // add renaming
        if (repo != null && repo.getLibraryData().libraries.renaming != null && repo.getLibraryData().libraries.renaming.length > 0)
        {
            json += "\t\t\"renaming\": [" + lineSep;
            int c = 0;
            for (Renaming rename : repo.getLibraryData().libraries.renaming)
            {
                if (c > 0)
                    json += "," + lineSep;
                json += "\t\t\t{" + lineSep;
                rename.from = rename.from.replace(singleBack, doubleBack);
                json += "\t\t\t\t\"from\": \"" + rename.from + "\"," + lineSep;
                rename.to = rename.to.replace(singleBack, doubleBack);
                json += "\t\t\t\t\"to\": \"" + rename.to + "\"" + lineSep;
                json += "\t\t\t}";
                ++c;
            }
            json += lineSep + "\t\t]," + lineSep;
        }

        // add locations
        if (repo != null && repo.getLibraryData().libraries.locations != null && repo.getLibraryData().libraries.locations.length > 0)
        {
            json += "\t\t\"locations\": [" + lineSep;
            int c = 0;
            for (Location loc : repo.getLibraryData().libraries.locations)
            {
                if (c > 0)
                    json += "," + lineSep;
                json += "\t\t\t{" + lineSep;
                loc.location = loc.location.replace(singleBack, doubleBack);
                json += "\t\t\t\t\"location\": \"" + loc.location + "\"," + lineSep;
                json += "\t\t\t\t\"minimum\": \"" + loc.minimum + "\"" + lineSep;
                json += "\t\t\t}";
                ++c;
            }
            json += lineSep + "\t\t]," + lineSep;
        }

        return json;
    }

    private String getFlavor(String os)
    {
        String flavor;
        if (os.toLowerCase().contains("windows"))
        {
            flavor = Utils.WINDOWS;
        }
        else if (os.toLowerCase().contains("mac") || os.toLowerCase().contains("os x"))
        {
            flavor = Utils.APPLE;
        }
        else
        {
            flavor = Utils.LINUX;
        }
        return flavor;
    }

    private String getIpAddresses() throws Exception
    {
        String localIp = "";
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();

            // find enabled, outside, "real" network interface
            if (n.isUp() && !n.isLoopback() && !n.isVirtual())
            {
                Enumeration ee = n.getInetAddresses();
                // see note, instead of:  while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    localIp = i.getHostAddress();
                }
                // note: break here and take first-found from first enabled interface
                break;
            }
        }
        if (localIp.length() < 1)
        {
            logger.warn("could not find an enabled physical interface to get IP address, using default");
            localIp = "127.0.0.1";
        }
        return localIp;
    }

    private boolean isCaseSensitive(String flavor)
    {
        return !flavor.equalsIgnoreCase("windows");
    }

    private String parseInformation(String filename) throws Exception
    {
        String description = "";
        String platform = "";

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(filename);

        NodeList nodeList = document.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); ++i)
        {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getNodeName().equalsIgnoreCase("MediaContainer"))
                {
                    if (node.hasAttributes())
                    {
                        NamedNodeMap attrs = node.getAttributes();
                        for (int j = 0; j < attrs.getLength(); ++j)
                        {
                            String name = attrs.item(j).getNodeName();
                            if (name.equalsIgnoreCase("friendlyName"))
                            {
                                if (repo != null && repo.getLibraryData().libraries.description != null)
                                    description = repo.getLibraryData().libraries.description;
                                else
                                    description = attrs.item(j).getNodeValue();
                            }
                            if (name.equalsIgnoreCase("platform"))
                            {
                                platform = attrs.item(j).getNodeValue();
                            }
                        }
                    }
                }
            }
        }

        flavor = getFlavor(platform);
        lineSep = Utils.getLineSeparator(flavor);

        String json = "{" + lineSep +
                "\t\"libraries\": {" + lineSep;

        json += "\t\t\"description\": \"" + description + "\"," + lineSep;

        json += "\t\t\"flavor\": \"" + flavor + "\"," + lineSep;

        logger.info("Plex Media Server: " + description);
        return json;
    }

    private String parseLibrary(String filename) throws Exception
    {
        int addedLibraries = 0;
        int addedLocations = 0;
        String json = "\t\t\"bibliography\": [" + lineSep;
        String library = "";

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(filename);

        NodeList nodeList = document.getElementsByTagName("*");
        for (int i = 0; i < nodeList.getLength(); ++i)
        {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                if (node.getNodeName().equalsIgnoreCase("Directory"))
                {
                    if (node.hasAttributes())
                    {
                        NamedNodeMap attrs = node.getAttributes();
                        for (int j = 0; j < attrs.getLength(); ++j)
                        {
                            if (addedLocations > 0)
                            {
                                logger.info("Plex library: " + library + ", " + addedLocations + " sources");
                                json += lineSep + "\t\t\t\t]" + lineSep +
                                        "\t\t\t}," + lineSep;
                                addedLocations = 0;
                            }
                            String name = attrs.item(j).getNodeName();
                            if (name.equalsIgnoreCase("title"))
                            {
                                library = attrs.item(j).getNodeValue();
                                json += "\t\t\t{" + lineSep +
                                        "\t\t\t\t\"name\": \"" + library + "\"," + lineSep +
                                        "\t\t\t\t\"sources\": [" + lineSep;
                                ++addedLibraries;
                            }
                        }
                    }
                }
                if (node.getNodeName().equalsIgnoreCase("Location"))
                {
                    if (node.hasAttributes())
                    {
                        NamedNodeMap attrs = node.getAttributes();
                        for (int j = 0; j < attrs.getLength(); ++j)
                        {
                            String name = attrs.item(j).getNodeName();
                            if (name.equalsIgnoreCase("path"))
                            {
                                if (addedLocations > 0)
                                {
                                    json += "," + lineSep;
                                }
                                json += "\t\t\t\t\t\"" + attrs.item(j).getNodeValue() + "\"";
                                ++addedLocations;
                            }
                        }
                    }
                }
            }
        }

        json += lineSep + "\t\t\t\t]" + lineSep +
                "\t\t\t}" + lineSep;

        json += "\t\t]" + lineSep;

        logger.info("Found " + addedLibraries + " Plex libraries");
        return json;
    }

    /**
     * execute the process
     *
     * @param args the input arguments
     * @return returnValue
     */
    private int process(String[] args)
    {
        int returnValue = 0;

        cfg = new Configuration();

        if (args.length < 3)
        {
            System.out.println("Error: Arguments missing");
            System.out.println("");
            cfg.help();
            return 1;
        }

        try
        {
            cfg.parseCommandLine(args);

            // setup the logger based on configuration
            System.setProperty("logFilename", cfg.getLogFilename());
            System.setProperty("consoleLevel", cfg.getConsoleLevel());
            System.setProperty("debugLevel", cfg.getDebugLevel());
            //System.setProperty("pattern", cfg.getPattern());
            org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();

            // get the named logger
            logger = LogManager.getLogger("applog");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return 1;
        }

        logger.info(cfg.banner());
        cfg.dump(logger);

        String connectString = cfg.getServer();
        String xPlexToken = cfg.getToken();
        String outputFilename = cfg.getOutputFilename();

        String host = "http://" + connectString;
        hostname = Utils.parseHost(connectString);

        try
        {
            if (cfg.getInputFilename().length() > 0)
            {
                readRepo(cfg.getInputFilename());
            }

            // get basic server capabilities XML
            String request = host + "?X-Plex-Token=" + xPlexToken;
            String responseBody = roundTrip(request);
            PrintWriter output = new PrintWriter(outputFilename + ".xml");
            output.print(responseBody);
            output.close();

            String json = parseInformation(outputFilename + ".xml");

            json += generateLocal();

            // get server library sections XML
            request = host + "/library/sections?X-Plex-Token=" + xPlexToken;
            responseBody = roundTrip(request);
            output = new PrintWriter(outputFilename + ".xml");
            output.print(responseBody);
            output.close();
            json += parseLibrary(outputFilename + ".xml");

            json += "\t}" + lineSep + "}" + lineSep;

            output = new PrintWriter(outputFilename);
            output.print(json);
            output.close();

            File outFile = new File(outputFilename + ".xml");
            outFile.delete();
        }
        catch (Exception e)
        {
            logger.error(Utils.getStackTrace(e));
        }

        return returnValue;
    } // process

    private void readRepo(String filename) throws Exception
    {
        repo = new Repository(cfg);
        repo.read(filename);
    }

    private String roundTrip(String request)
    {
        String responseBody = "";
        try (final CloseableHttpClient httpclient = HttpClients.createDefault())
        {
            final HttpGet httpget = new HttpGet(request);
            logger.info("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            // Create a custom response handler
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION)
                {
                    final HttpEntity entity = response.getEntity();
                    try
                    {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    }
                    catch (final ParseException ex)
                    {
                        throw new ClientProtocolException(ex);
                    }
                }
                else
                {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            responseBody = httpclient.execute(httpget, responseHandler);
            //logger.info("Response" + lineSep + responseBody);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage());
        }
        return responseBody;
    }

} // Main
