package pl.otros.logview.importer.bracketstyle;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.model.LogData;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by cc on 9/2/2016.
 */
public class AppLogParser implements Closeable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppLogParser.class.getName());
    final LineNumberReader reader;
    String lastLine = null;
    long lineNumber = 0;
    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public AppLogParser(InputStream inputStream) throws IOException {
        reader = new LineNumberReader(new InputStreamReader(inputStream, "UTF-8"));
        lastLine = reader.readLine();
        lineNumber++;
        LOGGER.info("Finish Init" + new String(lastLine));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private String nextLogString() throws IOException {
        if (lastLine != null) {
            String currentLog = lastLine;
            String mutilLine = moveToNextLog();
            if (mutilLine != null) {
                currentLog = currentLog + "\n" + mutilLine;
            }
            return currentLog;
        } else {
            return null;
        }
    }

    public LogData nextLog() {
        try {
            LogData logData = null;
            String nextLogString = nextLogString();
            LOGGER.info("nextLogString = " + nextLogString);
            if (nextLogString != null) {
                logData = parseApplicationLog(nextLogString);
            }
            return logData;
        } catch (Exception ex) {
            //error torrent
            LOGGER.error("error parsing log, break at line = " + lineNumber, ex);
            LogData logData = new LogData();
            logData.setMessage("LOG Parsing ERROR");
            return logData;
        }
    }

    private LogData parseApplicationLog(String nextLogString) {
        String[] arrs = StringUtils.splitByWholeSeparatorPreserveAllTokens(nextLogString, "] [");
        LogData logData = null;
        if (arrs.length >= 9) {
            logData = new LogData();
            String logDateStr = arrs[0];
            Date logDate = null;
            try {
                SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat(dateFormat, Locale.US);
                logDate = ISO8601DATEFORMAT.parse(logDateStr.substring(1));
            } catch (ParseException e) {
                LOGGER.warn("Invalid Data Time Format at line " + lineNumber + ", expected=" + dateFormat + ", actual=" + logDateStr);
            }
            logData.setDate(logDate);
            Map<String, String> properties = new HashMap<>();
            properties.put("server", arrs[1]);

            String lvlStr = arrs[2];
            Level lvl = Level.ALL;
            try {
                lvl = Level.parse(lvlStr);
            } catch (Exception ex) {
                LOGGER.error("Unable to parse level " + lvlStr, ex);
            }
            logData.setLevel(lvl);

            logData.setProperties(properties);
            logData.setMessageId(arrs[3]);
            logData.setClazz(arrs[4]);
            logData.setThread(arrs[5]);
            properties.put("userId", arrs[6]);
            logData.setLoggerName(arrs[6]);
            properties.put("ecid", arrs[7]);
            logData.setLine(arrs[7]);
            LOGGER.info("7=" + arrs[7]);
            if (arrs.length == 9) {
                LOGGER.info("8=" + arrs[8]);
                int i = StringUtils.indexOf(arrs[8], ']');
                properties.put("application", StringUtils.substring(arrs[8], 0, i));
                String message = StringUtils.substring(arrs[8], i + 1);
                logData.setMessage(message);
            } else if (arrs.length > 9) {
                String message = arrs[9];
                for (int i = 10; i < arrs.length; i++) {
                    message += ("] [" + arrs[i]); //concat remaining arrays
                }
                message = message + "\n";
                logData.setMessage(message);
            }
            return logData;
        } else {
            LOGGER.error("Log Format is incorrect, insufficient element at line " + lineNumber + " , expected 9, actual " + arrs.length);
            logData = new LogData();
            String logDateStr = arrs[0];
            try {
                Date logDate = null;
                SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat(dateFormat, Locale.US);
                logDate = ISO8601DATEFORMAT.parse(logDateStr.substring(1));
                logData.setDate(logDate);
            } catch (ParseException e) {
                LOGGER.warn("Invalid Data Time Format at line " + lineNumber + ", expected=" + dateFormat + ", actual=" + logDateStr);
            }
            String message = arrs[1];
            for (int i = 2; i < arrs.length; i++) {
                message += ("] [" + arrs[i]); //concat remaining arrays
            }
            message = message + "\n";
            logData.setMessage(message);
            return logData;
            //throw new IllegalArgumentException("Log Format is incorrect, insufficient element at line " + lineNumber + " , expected 9, actual " + arrs.length);
        }
    }

    private String moveToNextLog() throws IOException {
        String message = null;
        lastLine = reader.readLine();
        lineNumber++;
        while (lastLine != null && !StringUtils.startsWith(lastLine, "[")) {
            if (message == null) {
                message = lastLine;
            } else {
                message = message + "\n" + lastLine;
            }
            lastLine = reader.readLine();
            lineNumber++;
        }
        return message;
    }

}
