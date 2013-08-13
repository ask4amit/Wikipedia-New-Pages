package com.ask4amit.wikinewpages;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Amit Kumar
 * @email ask4amit@gmail.com
 */
public class Config {

    private int daysBeforeToday;
    private int numberOfDays;
    private String wikiLang;
    private String outputDir;
    
    public PrintWriter outputFile;
    public Date fromDate;
    public Date toDate;
    public String baseURL;
    public String parameters;
    public String wikiTimeFormat;
    public String outputTimeFormat;
    public String Locale;
    
    private static final SimpleDateFormat CUSTOM_TIME_PARSER = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat CUSTOM_TIME_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    private final static Logger LOGGER = Logger.getLogger(NewWikiPagesLister.class.getName());
    
    public Config(String configFile) throws ParseException, IOException {

        Properties prop = new Properties();
        Reader reader = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
        prop.load(reader);

        //get the property value and print it out
        wikiLang = prop.getProperty("wikiLang");
        baseURL = "http://" + wikiLang + ".wikipedia.org";
        
        parameters = prop.getProperty("paramters");
        daysBeforeToday = Integer.parseInt(prop.getProperty("daysBeforeToday"));
        if (daysBeforeToday > 30) throw new IllegalArgumentException("Records for pages created more than 30 days ago not available");
        numberOfDays = Integer.parseInt(prop.getProperty("numberOfDays"));
        setDateTimeBoundaries();
        wikiTimeFormat = prop.getProperty("wikiTimeFormat");
        outputTimeFormat = prop.getProperty("outputTimeFormat");
        Locale = prop.getProperty("Locale");
        
        outputDir = prop.getProperty("outputDir");
        File outDir = new File(outputDir);
        if (!outDir.exists() && !outDir.mkdirs())
            throw new IOException("Permission issue with creating output dir");
        outputFile = new PrintWriter(new BufferedWriter(new FileWriter(new File(OutputFilelocation()))));
        checkConfig();
    }

    // this function handles the nameing schema for the output File
    // File.pathSeparator should be used , but in Mac OSX it is ":" 
    /**
     *
     * @return location of the output file
     */
    public final String  OutputFilelocation() {
        return outputDir + "/" + CUSTOM_TIME_FORMATTER.format(fromDate);
    }

    private void setDateTimeBoundaries() throws ParseException {
        Calendar from = Calendar.getInstance();
        Calendar till = Calendar.getInstance();
        
        from.add(Calendar.DATE, -1 * daysBeforeToday);
        till.add(Calendar.DATE, -1 * daysBeforeToday + numberOfDays);

        fromDate = getStartingTimeOfDate(from);
        toDate = getStartingTimeOfDate(till);
        
    }

    private Date getStartingTimeOfDate(Calendar d) throws ParseException {
        String date = d.get(Calendar.DATE) + "/" + (d.get(Calendar.MONTH)+1) + "/" + d.get(Calendar.YEAR);
        return CUSTOM_TIME_PARSER.parse(date);
    }
    
    
    private void checkConfig()
    {
    if (outputFile == null || fromDate == null || toDate == null || wikiLang == null ||
            parameters == null || wikiTimeFormat == null || outputTimeFormat == null ||
            Locale == null)
            throw new IllegalArgumentException("Required Parameters cannot be null. Please check the config");
    if (numberOfDays > daysBeforeToday + 1 )
    LOGGER.log(Level.WARNING, "numberOfDays should not be more than daysBeforeToday + 1");
    
    }
    
}
