package com.ask4amit.wikinewpages;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;

/**
 *
 * @author Amit Kumar
 * @email ask4amit@gmail.com
 *
 *
 */
public class NewWikiPagesLister {

    private Config config;
    private SimpleDateFormat WIKI_TIME_PARSER; 
    private SimpleDateFormat WIKI_TIME_OUTPUT_FORMATTER; 
    private final static Logger LOGGER = Logger.getLogger(NewWikiPagesLister.class.getName());
    private List<Element> times, pageNames;
    // returns the Next page url
    // fills the list provided with pages and time found

    public String getTimeAndPages(String baseUrl, String parameters) throws IOException {
        String url = baseUrl + parameters;
        // download the NewPages webpage from wikipedia's site. Added timeout to download with lesser pagination
        Document doc = Jsoup.connect(url).timeout(10000).get();

        // get all the creation times
        times = doc.select(".mw-newpages-time");

        //get all the pageNames
        pageNames = doc.select(".mw-newpages-pagename");

        // there are two places where next link comes
        List<Element> nextPageLinks = doc.select("a.mw-nextlink");

        if (nextPageLinks.size() < 1) // there doesn't seem to be a next url
        {
            return null;
        } else // return the next links url
        // org.jsoup.nodes.Element = <a href="/w/index.php?title=Special:NewPages&amp;offset=20130802184004&amp;limit=500" title="Special:NewPages" rel="next" class="mw-nextlink">older 500</a>
        {
            return nextPageLinks.get(0).attr("href");
        }

    }

    // filters out the pages 
    private boolean filterResult(List<Result> result, Date fromDate, Date toDate) throws ParseException {
        Date creationDate = new Date();
        for (int i = 0; i < times.size(); i++) {
            String time = times.get(i).text();
            creationDate = WIKI_TIME_PARSER.parse(time);
            String pageName = pageNames.get(i).text();
            if (creationDate.compareTo(fromDate) >= 0) {
                if (creationDate.compareTo(toDate) < 0) {
                    result.add(new Result(WIKI_TIME_OUTPUT_FORMATTER.format(creationDate), pageName));
                } else {
                    continue;
                }
            } else {
                break;
            }

        }

        return (creationDate.compareTo(fromDate) >= 0);
    }

    private class Result {

        String time;
        String title;

        public Result(String time, String title) {
            this.time = time;
            this.title = title.replaceAll(" ", "_");
        }

        public String getTime() {
            return time;
        }

        public String getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return getTitle() + "\t" + getTime();
        }
    }

    private  void dumpResult(List<Result> result, PrintWriter outputFile) {
        for (Result r : result) {
            outputFile.println(r.toString());
        }
        outputFile.close();
        LOGGER.log(Level.INFO, "Stored {0} entries at {1}", new Object[]{result.size(), config.OutputFilelocation()});
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invalid usage.\n Please Provide the location of the config file as well");
        }
        String configFile = args[0];
        NewWikiPagesLister lister = new NewWikiPagesLister(configFile);
        lister.run();
    }

    private void run() {
        List<Result> result = new ArrayList<Result>();
        Boolean keepGoing = true;
        String parameters = config.parameters;
        while (keepGoing) {
            try {
                LOGGER.log(Level.INFO, "Querying Wikipedia for new pages with {0} ", parameters);
                parameters = getTimeAndPages(config.baseURL, parameters);
                LOGGER.log(Level.INFO, "Found: {0} pages", pageNames.size());
                LOGGER.log(Level.INFO, "Next Page link is {0}", parameters);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Error while getting new Page List", ex);
                System.exit(1);
            }
            try {
                keepGoing = filterResult(result, config.fromDate, config.toDate);
                
            } catch (ParseException ex) {
                LOGGER.log(Level.SEVERE, "Error while filtering results", ex);
                System.exit(1);
            }
            if (parameters == null) {
                keepGoing = false;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        dumpResult(result, config.outputFile);
    }

    public NewWikiPagesLister(String configFile) {
        try {
            config = new Config(configFile);
            WIKI_TIME_PARSER = new SimpleDateFormat(config.wikiTimeFormat, new Locale(config.Locale));
            WIKI_TIME_OUTPUT_FORMATTER = new SimpleDateFormat(config.outputTimeFormat);
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error while Parsing the Config", ex);
            System.exit(1);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while Parsing the Config", ex);
            System.exit(1);
        }
    }
}
