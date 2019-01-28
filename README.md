Flickr Album Backup utility focuses on obtaining a backup of Flickr metadata 
related with albums and photos / videos within the album.

Flickr has [announced](https://blog.flickr.net/en/2018/12/17/important-service-updates-and-dates-to-remember/) 
that Feb 5, 2019 is the deadline post which most of the media belonging to 
users with **more than 1000** photos would start getting deleted. Thus, this 
is the high time for anyone to start backing up the content if they do not 
want to lose them.

Flickr provides direct options to download the albums, but there might 
be some meta data associated with the albums that can be lost along with 
Flickr's change. 
Examples include the description that might contain details about the 
occasion when the memories were captured, comments where other users
might have expressed their views / appreciations etc.

This tool is for those who have used Flickr primarily for backing up 
their media along with some added description, sharing the albums with 
other users etc.

**Usage:**

The release package is equipped with executable scripts named `run` in batch
and shell formats. These need to be executed with API Key and Shared Secret 
as parameters. The application also supports non parameterized execution 
where the key and secret are requested at runtime.


To obtain the key and secret, apply for a non commercial application key 
from https://www.flickr.com/services/apps/create/apply 

**Reports:**

Reports appear as nicely formatted HTML files. Look for the `reports` 
directory that gets created. This has an `index.html` file which is 
the starting point of reading the report. Further pages are deep linked  
from the index page.

**Thanks to:**
* Flickr Library: https://github.com/boncey/Flickr4Java/
* HTML Layout: https://colorlib.com/wp/css3-table-templates/
