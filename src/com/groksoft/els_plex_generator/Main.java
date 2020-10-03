package com.groksoft.els_plex_generator;

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
 * Main ELS program
 */
public class Main
{
    private Logger logger = null;
    private String flavor = "";
    private String hostname = "";
    private String lineSep = "";

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
        // generate host element
        //String val = getIpAddresses();
        String json = "\t\t\"host\": \"" + hostname + ":50271\"," + lineSep;

        // add terminal_allowed
        json += "\t\t\"terminal_allowed\": \"true\"," + lineSep;

        // add key
        UUID uuid = UUID.randomUUID();
        json += "\t\t\"key\": \"" + uuid + "\"," + lineSep;

        // add case_sensitive
        json += "\t\t\"case_sensitive\": \"" + (isCaseSensitive(flavor) ? "true" : "false") + "\"," + lineSep;

        // add ignore_patterns
        json += "\t\t\"ignore_patterns\": [" + lineSep +
                "\t\t\t\"desktop.ini\"," + lineSep +
                "\t\t\t\"Thumbs.db\"" + lineSep +
                "\t\t]," + lineSep;

        return json;
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
    
    private String getFlavor()
    {
        String flavor;
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows"))
        {
            flavor = "windows";
        }
        else if (os.toLowerCase().contains("mac") || os.toLowerCase().contains("os x"))
        {
            flavor = "apple";
        }
        else
        {
            flavor = "linux";
        }
        return flavor;
    }
    
    private boolean isCaseSensitive(String flavor)
    {
        return flavor.equalsIgnoreCase("windows");
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

        lineSep = Utils.getLineSeparator(platform);
        flavor = platform.toLowerCase();

        String json = "{" + lineSep +
                "\t\"libraries\": {" + lineSep;

        json += "\t\t\"description\": \"" + description + "\"," + lineSep;

        json += "\t\t\"flavor\": \"" + platform.toLowerCase() + "\"," + lineSep;


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

        try
        {
            // setup the logger based on configuration
            System.setProperty("logFilename", "ELS-Plex-Generator.log");
            System.setProperty("consoleLevel", "DEBUG");
            System.setProperty("debugLevel", "DEBUG");
            //System.setProperty("pattern", cfg.getPattern());
            org.apache.logging.log4j.core.LoggerContext ctx = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
            ctx.reconfigure();

            // get the named logger
            logger = LogManager.getLogger("applog");
        }
        catch (Exception ignored)
        {
        }

        logger.info("ELS-Plex-Generator, version 1.00");

        if (args.length != 3)
        {
            logger.info("Error: Arguments missing");
            logger.info("Required arguments:");
            logger.info("  1. Plex Media Server hostname or IP address[:]port");
            logger.info("  2. X-Plex-Token");
            logger.info("  3. Output filename");
            logger.info("Runtime example: ");
            logger.info("  java -jar ELS-Plex-Generator.jar 192.168.1.4:32400 publisher.json");
            logger.info("For the X-Plex-Token see https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/");
            return 1;
        }

        String connectString = args[0];
        String xPlexToken = args[1];
        String outputFilename = args[2];
        String host = "http://" + connectString;
        hostname = Utils.parseHost(connectString);

        try
        {
//            flavor = getFlavor();
//            lineSep = System.getProperty("line.separator");
            
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
