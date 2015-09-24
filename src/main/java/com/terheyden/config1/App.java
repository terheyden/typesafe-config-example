package com.terheyden.config1;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Typesafe Config / ConfigFactory tutorial by example.
 */
public class App {

    private static final Logger log = getLogger(App.class);

    private App() {}

    public static void main(String... args) throws IOException {

        // Loads in this order (later resources take highest priority):
        //   - reference.conf (all resources on classpath with this name)
        //   - application.properties (all resources on classpath with this name)
        //   - application.json (all resources on classpath with this name)
        //   - application.conf (all resources on classpath with this name)
        //   - system properties
        //
        // A library should ship with a reference.conf in its jar.
        // Apps that use it can provide an application.conf with overrides
        // or just load their own via: ConfigFactory.load("myapp") (uses "myapp.conf")

        // This example loads the default config (using files listed above),
        // but then provides some defaults in case the important login / password
        // config entries are missing:
        Config config = ConfigFactory.load()
            .withFallback(ConfigFactory.parseString("config1.login = root"))
            .withFallback(ConfigFactory.parseString("config1.password = root"));

        // Example of changing a config value.
        // Configs are immutable so changes always create a new instance.
        config = config.withValue("config1.password", ConfigValueFactory.fromAnyRef("abc123"));

        log.info("The login is: {}, password: {}",
            config.getString("config1.login"), config.getString("config1.password"));

        // Java system props and -Dargs are also loaded in
        // but not environment vars (e.g. not "PATH" or "USERHOME"):
        log.info("Some system props: {}, {}, ...",
            config.getString("user.dir"), config.getString("file.separator"));

        // My config save helper. This version saves EVERYTHING:
        ConfigUtils.save(config);

        // This version saves just sections so all those sys props aren't in there.
        // Though, remember, sys props take highest precedence anyway so it doesn't really matter.
        // This is really just for clarity.
        //ConfigUtils.save(config, "config1", "font");

        ////////////////////////////////////////////////////////////////////////////////
        // GETTING VALUES (check out 'reference.conf' for this part):

        log.info("Font name: {}", config.getString("font.name"));

        List<String> colors = config.getStringList("colors");

        Map<String, Object> font = config.getObject("font").unwrapped();

        log.info("Font size: {}", font.get("size"));

        // Accessing a key that doesn't exist will throw.
        // This is BY DESIGN - best practice is to provide
        // defaults in reference.conf.

        try {
            log.info("THIS WILL THROW: {}", config.getString("jabberwocky"));
        } catch (ConfigException.Missing e) {
            // Oops.
        }

        // Instead:

        if (config.hasPath("myApp.port")) {
            log.info("App port number: {}", config.getInt("myApp.port"));
        }

        // Or how about:
        log.info("App port number: {}",
            config.withFallback(ConfigFactory.parseString("myApp.port=8888"))
                .getInt("myApp.port")
        );
    }
}
