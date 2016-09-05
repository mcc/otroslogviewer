package pl.otros.logview.importer.bracketstyle;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.InitializationException;
import pl.otros.logview.api.importer.LogImporter;
import pl.otros.logview.api.model.LogData;
import pl.otros.logview.api.model.LogDataCollector;
import pl.otros.logview.api.parser.ParsingContext;
import pl.otros.logview.pluginable.AbstractPluginableElement;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by cc on 9/2/2016.
 */
public class ApplicationLogImporter extends AbstractPluginableElement implements LogImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationLogImporter.class.getName());

    private static final String DESCRIPTION = "Application Log Importer";
    private static final String NAME = "Application Log Importer";

    public ApplicationLogImporter() {
        super(NAME, DESCRIPTION);
    }

    @Override
    public int getApiVersion() {
        return LOG_IMPORTER_VERSION_1;
    }

    @Override
    public void init(Properties properties) throws InitializationException {

    }

    @Override
    public void initParsingContext(ParsingContext parsingContext) {

    }

    @Override
    public void importLogs(InputStream in, LogDataCollector dataCollector, ParsingContext parsingContext) {
        LOGGER.info("Start Import Log");
        try (AppLogParser appLogParser = new AppLogParser(in)) {
            LogData nextLog = appLogParser.nextLog();
            if (nextLog != null) dataCollector.add(nextLog);
            while (nextLog != null) {
                LOGGER.info("" + nextLog.getDate());
                nextLog = appLogParser.nextLog();
                nextLog.setFile(parsingContext.getLogSource());
                if (nextLog != null) {
                    dataCollector.add(nextLog);
                }
            }
        } catch (IOException ex) {
            LOGGER.warn(String.format("IOException when reading app log: %s", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(String.format("IllegalArgumentException when reading app log: %s", ex.getMessage()));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public String getKeyStrokeAccelelator() {
        return null;
    }

    @Override
    public int getMnemonic() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
