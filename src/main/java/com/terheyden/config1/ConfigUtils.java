package com.terheyden.config1;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Typesafe Config-related util methods.
 */
public class ConfigUtils {

    private static final Logger log = getLogger(ConfigUtils.class);

    /**
     * Libs should be using reference.conf. We don't want to overwrite that,
     * so we'll save to the higher-priority application.conf file.
     */
    private static final String SAVE_FILENAME = "application.conf";

    private ConfigUtils() {}

    /**
     * Save a {@link Config} out to disk as: application.conf
     * @param config the config to save
     * @param pathsToSave if specified, you can save only certain config paths instead of the entire thing with all the system properties in it
     * @throws IOException if the file can't be saved
     */
    public static void save(Config config, String... pathsToSave) throws IOException {

        ConfigRenderOptions options = ConfigRenderOptions.defaults()
            .setComments(false)
            .setOriginComments(false)
            .setFormatted(true);

        // The dir the app was started in - should be in the loadable class path.
        File appDir = new File(System.getProperty("user.dir"));

        // Let's see if there's a /conf dir.
        File confDir = new File(appDir, "conf");

        // The dir we'll write to.
        File dir = confDir.isDirectory() ? confDir : appDir;

        StringBuilder bui = new StringBuilder();

        if (pathsToSave != null && pathsToSave.length > 0) {
            for (String path : pathsToSave) {
                bui.append(path).append(' ').append(config.getValue(path).render(options)).append('\n');
            }
        } else {
            bui.append(config.root().render(options));
        }

        File saveFile = new File(dir, SAVE_FILENAME);
        FileUtils.writeStringToFile(saveFile, bui.toString());

        log.info("Config saved: {}", saveFile.getAbsolutePath());
    }
}
