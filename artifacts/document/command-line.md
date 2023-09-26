ELS-Plex-Generator is an add-on tool for Entertainment Library Synchronizer (ELS),
available at [https://github.com/Corionis/ELS](https://github.com/Corionis/ELS), that generates the publisher library
JSON file required for ELS by querying a Plex Media Server for metadata and library source definitions.

## Command Line Options
ELS-Plex-Generator requires 3 arguments.

### Required arguments                                                                                           
  * -s | --server Plex Media Server hostname or IP address[:]port                                               
  * -t | --token Authentication X-Plex-Token                                                                    
  * -o | --output-file Output filename                                                                          

### Optional arguments                                                                                           
  * -c | --console-level Console logging level                                                                  
  * -d | --debug-level Debug logging level                                                                      
  * -f | --log-file Log filename                                                                                
  * -i | --input-file Input filename for user-definable elements                                                

### Runtime example
```                                                                                             
  java -jar ELS-Plex-Generator.jar -s 192.168.2.1:32400 -t syMEox_DcT_4aIXfy3-J -o publisher.json
```             

### Notes
 1. For the X-Plex-Token see [Finding an authentication token / X-Plex-Token](https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/).

 2. If a -i input filename is specified it must be an ELS publisher file in JSON format. When an
input filename is used certain values are copied to the output file. Those are:
 * description, default Plex Media Server name
 * host, default the -s | --server argument to this program
 * listen, if present, no default
 * terminal_allowed, default true
 * key, default generated UUID on every run of this program
 * case_sensitive, default Windows = false, others = true
 * ignore_patterns, default desktop.ini, Thumbs.db
 * renames, if present, no default
 * locations, if present, no default

 3. Using the -i option allows this program to be executed as part of an automated
 procedure, such as before ELS is run on a schedule.
 