package it.greenvulcano.gvesb.virtual.utils;

import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.util.txt.DateUtils;

public class CalendarBody {

    private final static Logger LOGGER = LoggerFactory.getLogger(CalendarBody.class);

    final static String DTSTART = "DTSTART";
    final static String DTEND = "DTEND";
    final static String SUMMARY = "SUMMAR:";
    final static String DESCRIPTION = "DESCRIPTION";
    final static String LOCATION = "LOCATION";
    final static String ORGANIZER = "ORGANIZER";

    private final String icalendarContent;

    public CalendarBody(String icalendarContent) {
        this.icalendarContent = icalendarContent;
    }

    public Date getEventStart() {
        return readDate(DTSTART);
    }

    public Date getEventEnd() {
        return readDate(DTEND);
    }
    
    public String getEventStartTimestamp() {
        return readString(DTSTART);
    }

    public String getEventEndTimestamp() {
        return readString(DTEND);
    }

    public String getSummary() {
        return readString(SUMMARY);
    }

    public String getDescription() {
        return readString(DESCRIPTION);
    }

    public String getLocaltion() {
        return readString(LOCATION);
    }

    public String getOrganizer() {                
        return readString(ORGANIZER);
    }
   
    private Date readDate(String attribute) {

        if (icalendarContent != null) {

            Optional<String> dateAttribute = Optional.ofNullable(readString(attribute));
            
            if (dateAttribute.isPresent()) {
                try {

                    return DateUtils.stringToDate(dateAttribute.get(), "yyyyMMdd'T'HHmmss");
                } catch (Exception e) {
                    LOGGER.error(String.format("Failed to parse attribute %s %s", attribute, dateAttribute.get()), e);
                }
            }
        }
        return null;
    }

    private String readString(String attribute) {

        String attributeMatcher = String.format("^%s.*:.*$", attribute);
        String splitPattern = String.format("%s.*:", attribute);
        
        if (icalendarContent != null) {
            
            String event = icalendarContent.substring(icalendarContent.lastIndexOf("BEGIN:VEVENT"));
            
            Optional<String> stringAttribute = event.lines()
                                                    .collect(() -> new LinkedList<String>(), 
                                                            (set, line) -> {
                                                                                if (line.startsWith(" ")) {
                                                                                    String fullLine = set.removeLast().concat(line.trim());
                                                                                    set.add(fullLine);
                                                                                } else {
                                                                                    set.add(line);
                                                                                }

                                                                            }, 
                                                            (set1, set2) -> set1.addAll(set2))
                                                   .stream()
                                                   .filter(line -> line.matches(attributeMatcher))
                                                   .map(attrib -> attrib.split(splitPattern))
                                                   .filter(results -> results.length>1)
                                                   .map(results -> results[1])
                                                   .findFirst();

            if (stringAttribute.isPresent()) {
                return stringAttribute.get();
            }

        }
        return null;
    }

}
