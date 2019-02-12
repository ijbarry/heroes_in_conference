/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see http://www.gnu.org/licenses/
 */
package uk.ac.cam.cl.kilo;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import uk.ac.cam.cl.kilo.data.Achievement;
import uk.ac.cam.cl.kilo.data.Database;
import uk.ac.cam.cl.kilo.data.DatabaseException;
import uk.ac.cam.cl.kilo.data.Event;
import uk.ac.cam.cl.kilo.data.User;

/**
 * Server.java
 *
 * @author Nathan Corbyn
 */
public class Server {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  // Initialises the server log
  private static Logger log = LoggerFactory.getLogger(Server.class);
  // Configuration file for the database, containing five fields:
  //   `driver` - the JDBC driver class
  //   `url` - the url of the database
  //   `database` - the name of the datbase
  //   `user` - the datbase username
  //   `pass` - the database password
  private static String DATABASE_PROPERTIES = "database.properties";

  /** Configures the {@link javax.sql#DataSource} required by the database. */
  public static void configureDatabase() {
    log.info("Configuring pooled data source...");
    try {
      Properties properties = new Properties();
      properties.load(new FileInputStream(DATABASE_PROPERTIES));
      ComboPooledDataSource source = new ComboPooledDataSource();
      source.setJdbcUrl("jdbc:" + properties.get("url") + "/" + properties.get("database"));
      source.setUser((String) properties.get("user"));
      source.setPassword((String) properties.get("pass"));
      source.setDriverClass((String) properties.get("driver"));
      source.setIdleConnectionTestPeriod(100);
      source.setLoginTimeout(10);
      Database.configure(source);
      log.info("Data source configured!");
    } catch (SQLException | PropertyVetoException | IOException e) {
      // We can't start the server if we can't connect to the database so we should just crash
      log.error("Failed to configure database!");
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the {@link uk.ac.cam.cl.kilo.data#User user} associated with the authenticated request
   * and throws an exception if the request is not authenticated.
   *
   * @return the user authenticated with the rqeuest
   * @throws UnauthenticatedException if the request is not authenticated
   */
  public static User authenticatedUserFor(Request request) throws UnauthenticatedException {
    throw new UnauthenticatedException("not implemented");
  }

  /**
   * Securely generates a random hex string.
   *
   * @param length the number of bytes of hex to generate
   * @return the hex string
   */
  public static String generateID(int length) {
    final Random r = new SecureRandom();
    byte[] state = new byte[length];
    r.nextBytes(state);
    return bytesToHex(state);
  }

  /**
   * Converts a given byte array into a hex string.
   *
   * @param bytes The bytes to convert to hex
   * @return The resulting hex string
   */
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars).toLowerCase();
  }

  /**
   * Entry point
   *
   * @param args
   */
  public static void main(String[] args) {
    configureDatabase();
    Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
    ;
    exception(
        DatabaseException.class,
        (exception, request, response) -> {
          // Handle database exception
          response.type("application/json");
          response.body(gson.toJson(err(exception.getMessage())));
        });
    exception(
        UnauthenticatedException.class,
        (exception, request, response) -> {
          // Handle authentication exception by redirecting to authentication handler
          response.redirect("/api/oauth");
        });
    // API path
    path(
        "/api",
        () -> {
          // Admin API path
          path("/admin", () -> {});
          // User facing API calls
          path(
              "/",
              () -> {
                get(
                    "/oauth",
                    (request, response) -> {
                      throw new UnauthenticatedException("not implemented");
                    },
                    gson::toJson);
                get(
                    "/maps",
                    (request, response) -> {
                      response.type("application/json");
                      return ok(Database.getInstance().getMaps());
                    },
                    gson::toJson);
                get(
                    "/events",
                    (request, response) -> {
                      response.type("application/json");
                      return ok(Database.getInstance().getEvents());
                    },
                    gson::toJson);
                get(
                    "/achievements",
                    (request, response) -> {
                      response.type("application/json");
                      return ok(Database.getInstance().getAchievements());
                    },
                    gson::toJson);
                get(
                    "/groups",
                    (request, response) -> {
                      response.type("application/json");
                      return ok(Database.getInstance().getContentGroups());
                    },
                    gson::toJson);
                path(
                    "/user",
                    () -> {
                      get(
                          "/achieved/:achievement",
                          (request, response) -> {
                            long achievement = Long.parseLong(request.params(":achievement"));
                            User user = authenticatedUserFor(request);
                            user.achieve(Achievement.getByID(achievement));
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/interested/:event",
                          (request, response) -> {
                            long event = Long.parseLong(request.params(":event"));
                            User user = authenticatedUserFor(request);
                            user.markInterestIn(Event.getByID(event));
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/uninterested/:event",
                          (request, response) -> {
                            long event = Long.parseLong(request.params(":event"));
                            User user = authenticatedUserFor(request);
                            user.unmarkInterestIn(Event.getByID(event));
                            return ok(null);
                          },
                          gson::toJson);
                    });
              });
        });
  }

  // Utility method for wrapping OK responses
  private static ServerResponse ok(Object payload) {
    return new ServerResponse(true, null, payload);
  }

  // Utility method for wrapping error messages
  private static ServerResponse err(String error) {
    return new ServerResponse(false, error, null);
  }

  // Utility type used for JSON serialisation of server responses wrapped with response status
  // (unused fields are used by the serialiser so no need for warnings)
  private static class ServerResponse {
    @SuppressWarnings("unused")
    private String status;

    @SuppressWarnings("unused")
    private String error;

    @SuppressWarnings("unused")
    private Object payload;

    /**
     * @param ok true if the response is ok, false otherwise
     * @param error the error message for the wrapper
     * @param payload the object payload to serialise
     */
    public ServerResponse(boolean ok, String error, Object payload) {
      if (ok) status = "ok";
      else status = "false";
      this.error = error;
      this.payload = payload;
    }
  }
}
