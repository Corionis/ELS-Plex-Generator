![ELS-Plex logo](https://github.com/Corionis/ELS-Plex-Generator/blob/master/artifacts/images/els-plex-logo.png)
# ELS-Plex-Generator
ELS-Plex-Generator is a Plex-specific add-on tool for Entertainment Library Synchronizer (ELS),
available at [https://github.com/Corionis/ELS](https://github.com/Corionis/ELS), that generates the publisher library
JSON file required for ELS.

ELS-Plex-Generator queries a Plex Media Server (PMS) directly using the PMS REST interface, typically on port :32400
to gather the necessary data.

A PMS X-Plex-Token is required for authentication, see [Finding an authentication token / X-Plex-Token](https://support.plex.tv/articles/204059436-finding-an-authentication-token-x-plex-token/) on the Plex support site.
