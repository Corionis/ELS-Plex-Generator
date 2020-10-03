ELS-Plex-Generator is an add-on tool for Entertainment Library Synchronizer (ELS),
available at [https://github.com/GrokSoft/ELS](https://github.com/GrokSoft/ELS), that generates the publisher library
JSON file required for ELS.

## Command Line Options
ELS-Plex-Generator requires 3 arguments:

  1. Plex Media Server hostname or IP address[:]port
  2. X-Plex-Token
  3. Output filename

Runtime example:
  java -jar ELS-Plex-Generator.jar 192.168.2.1:32400 syMEox_DcT_4aIXfy3-J publisher.json
  
If this utility is being run on the same computer as Plex Media Server the hostname or IP address
may be omitted, but :port is required, example:
  java -jar ELS-Plex-Generator.jar :32400 syMEox_DcT_4aIXfy3-J publisher.json


For the X-Plex-Token see [Finding an authentication token / X-Plex-Token](https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/).


