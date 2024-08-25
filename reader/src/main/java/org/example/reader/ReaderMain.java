package org.example.reader;

import org.example.cli.Cli;
import org.example.engine.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

final class ReaderMain {

    private static final Logger LOG = LoggerFactory.getLogger(ReaderMain.class);

    public static void main(final String[] args) {
        LOG.info("starting reader");
        final long start = Clock.systemUTC().millis();
        if (args.length < 1)
        {
            LOG.warn("no file path provided, exiting now");
            return;
        }
        final String filePath = args[0];
        if (LOG.isDebugEnabled()) {
            LOG.debug("file path is {}", filePath);
        }
        final UserStore userStore = new UserStore();
        if (LOG.isDebugEnabled()) {
            LOG.info("reading data in from csv file");
        }
        CsvReader.readCsvFile(filePath, userStore);
        final long end = Clock.systemUTC().millis();
        LOG.info("took {} ms to load all info from csv", end - start);
        final Cli cli = new Cli(userStore);
        cli.awaitCommands();
    }
}
