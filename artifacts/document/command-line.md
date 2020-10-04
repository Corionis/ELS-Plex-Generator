ELS-Plex-Generator is an add-on tool for Entertainment Library Synchronizer (ELS),
available at [https://github.com/GrokSoft/ELS](https://github.com/GrokSoft/ELS), that generates the publisher library
JSON file required for ELS.

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
For the X-Plex-Token see [Finding an authentication token / X-Plex-Token](https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/).

If a -i input filename is specified it must be an ELS publisher file in JSON format. When an
input filename is used certain values are copied to the output file. Those are:
 * description, default Plex Media Server name
 * host, default the -s | --server argument to this program
 * listen, if present, no default
 * terminal_allowed, default true
 * key, default generated UUID on every run of this program
 * case_sensitive, default Windows = false, others = true
 * ignore_patterns, default desktop.ini, Thumbs.db

If this utility is being run on the same computer as Plex Media Server the hostname or IP address
may be omitted, but :port is required, example:
```
  java -jar ELS-Plex-Generator.jar :32400 syMEox_DcT_4aIXfy3-J publisher.json
```
