############################################################
#
# zSite version(${version}) @zozoh 
#
#                          power-by Nutz (http://nutzam.com) 
------------------------------------------------------------
Useage:

  zsite [src] [dest] [-p patten]? [-c clean]?

  - src     : the source folder full path, 
              just like "~/mysite/abc"
  - dest    : the output folder full path, 
              just like "~/mysite/abc-output"
  - pattern : optional, a filter to indicate which object
              should be ignore
  - clean   : optional, clean the dest folder before output
  
------------------------------------------------------------
Example:
 
 - Render dir A to dir B, clear B before rendering:
 
    zsite ~/a ~/b -c
 
 - Render dir A to dir B, only process the .html files:
 
    zsite ~/a ~/b -p '^.*[.]html$'
 
 - Render dir A to dir B, 
   skip processing the files not .html and .zdoc
 
    zsite ~/a ~/b -p '!^(.*[.])(html|zdoc)$'
    
------------------------------------------------------------
Enjoy it ^_^
                                       @ ${now}