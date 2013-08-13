Wikipedia-New-Pages
===================

Track and List new wikipedia pages created within last 30 days.
I have been looking into the Tracking New Wiki Pages created  for last few days. I did some research to find a simple process to gather new pages than monitoring the Wikipedia's Recent Changes IRC channel. 
There are issues with IRC bot solution as it has to be up all the time and if it is down we would miss those new Entities. During my research I found that wikipedia actually has a page which lists all the pages getting created. 
It looks like they maintain the record for last 30 days. For EN wiki, the tracking page is  http://en.wikipedia.org/wiki/Special:NewPages .

This java app  goes through  these pages and create the list of New Wikipedia Pages. It is configurable so that it can be used for all the different language wikipedia. The command to run the solution is :

java -cp wiki_new_pages-1.0-SNAPSHOT-jar-with-dependencies.jar com.ask4amit.wikinewpages.NewWikiPagesLister config/config.properties.en

I have also created the config files for french and italian wikis. Just replace 'config.properties.en' in the above command with 'config.properties.fr'
You can read the comments in  config.properties.en file to get an idea on how to select the date for which you want the Page list and for how many days.


You can run this solution anytime to get the new page list for any of the days in last 30 days for any of the wikipedias. For now it only tracks new Article pages.
