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

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import uk.ac.cam.cl.kilo.data.Achievement;
import uk.ac.cam.cl.kilo.data.ConferenceMap;
import uk.ac.cam.cl.kilo.data.ContentGroup;
import uk.ac.cam.cl.kilo.data.Database;
import uk.ac.cam.cl.kilo.data.DatabaseException;
import uk.ac.cam.cl.kilo.data.Event;
import uk.ac.cam.cl.kilo.data.MapMarker;
import uk.ac.cam.cl.kilo.data.Session;
import uk.ac.cam.cl.kilo.data.UsageStatistic;
import uk.ac.cam.cl.kilo.data.User;

/**
 * Server.java
 *
 * @author Nathan Corbyn
 */
public class Server {
  // Initialises the server log
  private static Logger log = LoggerFactory.getLogger(Server.class);
  // Configuration file for the database, containing five fields:
  //   `driver` - the JDBC driver class
  //   `url` - the url of the database
  //   `database` - the name of the datbase
  //   `user` - the datbase username
  //   `pass` - the database password
  private static String DATABASE_PROPERTIES = "database.properties";
  // Configuration file for the Facebook graph API:
  //   `authorisation_url` - the Facebook OAuth dialogue URL
  //   `graph_url` - the URL of the Facebook graph API server
  //   `client_id` - our client ID
  //   `client_secret` - out client secret
  private static String OAUTH_PROPERTIES = "facebook.properties";
  // Configuration file for the administrator password hash:
  //   `passhash` - the administrator password hash (SHA-256)
  private static String ADMIN_PROPERTIES = "admin.properties";

  private static String oauthAuthorisationURL,
      facebookGraphURL,
      oauthID,
      oauthSecret,
      adminPasshash;
  private static File uploadDir;
  private static Map<String, Instant> adminSessions = new HashMap<>();
  private static int requestCount = 0;
  private static final Lock lock = new ReentrantLock(true);

  /** Configures the {@link javax.sql#DataSource} required by the database. */
  public static void configureDatabase() {
    log.info("Configuring pooled data source...");
    try (FileInputStream input = new FileInputStream(DATABASE_PROPERTIES)) {
      Properties properties = new Properties();
      properties.load(input);
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

  /** Configures the OAuth 2 parameters. */
  public static void configureOAuth() {
    try (FileInputStream input = new FileInputStream(OAUTH_PROPERTIES)) {
      log.info("Configuring OAuth parameters...");
      Properties properties = new Properties();
      properties.load(input);
      oauthAuthorisationURL = (String) properties.get("authorisation_url");
      facebookGraphURL = (String) properties.get("graph_url");
      oauthID = (String) properties.get("client_id");
      oauthSecret = (String) properties.get("client_secret");
      log.info("OAuth configured!");
    } catch (IOException e) {
      // We can't start the server without the OAuth information, so we should just crash here too
      log.error("Failed to configure OAuth!");
      throw new RuntimeException(e);
    }
  }

  /** Configures admin password hash. */
  public static void configureAdminProperties() {
    try (FileInputStream input = new FileInputStream(ADMIN_PROPERTIES)) {
      log.info("Configuring administrator properties...");
      Properties properties = new Properties();
      properties.load(input);
      adminPasshash = (String) properties.get("passhash");
      log.info("Administrator properties configured!");
    } catch (IOException e) {
      // It's possible that we could carry on with now administrator password, but that also seems
      // like something worth crashing over
      log.error("Failed to configure administrator properties!");
      throw new RuntimeException(e);
    }
  }

  public static void configureUploadDirectory() {
    log.info("Configuring upload directory...");
    uploadDir = new File("upload");
    uploadDir.mkdir();
    log.info("Upload directory configured!");
  }

  /**
   * Returns the {@link uk.ac.cam.cl.kilo.data#User user} associated with the authenticated request
   * and throws an exception if the request is not authenticated.
   *
   * @param request the request to authenticate
   * @return the user authenticated with the rqeuest
   * @throws UnauthenticatedException if the request is not authenticated
   */
  public static User authenticatedUserFor(Request request) throws UnauthenticatedException {
    String sessionToken = request.queryParamOrDefault("session", "");
    if (sessionToken == null) throw new UnauthenticatedException("No session token");
    try {
      Session session = Session.getByID(sessionToken);
      if (!session.isActivated()) throw new UnauthenticatedException("Session is not active");
      if (session.hasExpired()) throw new UnauthenticatedException("Sesssion has expired");
      return session.getUser();
    } catch (DatabaseException e) {
      throw new UnauthenticatedException(e);
    }
  }

  /**
   * Checks if the given request is authenticated as an administrator.
   *
   * @param request the request to authenticate
   * @throws UnauthenticatedException if the request is not authenticated
   */
  public static void authenticateAdmin(Request request) throws UnauthenticatedException {
    String token = request.cookie("admin_session");
    // If we don't have a session token, it's definitely not authenticatable
    if (token == null) throw new AdminUnauthenticatedException("No session token");
    // If we don't know about the token, it must be invalid
    if (!adminSessions.containsKey(token))
      throw new AdminUnauthenticatedException("Unrecognised session token");
    // Delete any expired tokens and report the error
    if (adminSessions.get(token).isBefore(Instant.now())) {
      adminSessions.remove(token);
      throw new AdminUnauthenticatedException("Admin session token expired");
    }
    // If we make it to here, we can assume the session token is valid
  }

  /**
   * Accepts an uploaded image from the given request and returns the path to the images location on
   * disk.
   *
   * @param request the request to accept an image from
   * @return the path to the image after upload
   * @throws IOException if the image could not be written
   * @throws ServletException if the upload could not be completed
   */
  public static Path acceptUploadedImage(Request request) throws IOException, ServletException {
    Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");
    request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
    try (InputStream input = request.raw().getPart("image").getInputStream()) {
      Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
    }
    return tempFile;
  }

  /** Starts the thread that writes back usage data. */
  public static void startUsageThread() {
    Thread usageThread =
        new Thread(
            () -> {
              while (true) {
                try {
                  // Wait two minutes for a reading
                  Thread.sleep(120000);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  break;
                }
                // Take a reading
                Instant now = Instant.now();
                lock.lock();
                int requestCountCopy = requestCount;
                requestCount = 0;
                lock.unlock();
                try {
                  // Try to add the reading to the database
                  new UsageStatistic(now, requestCount);
                } catch (DatabaseException e) {
                  // If we fail, then we need to restore those measurements
                  lock.lock();
                  requestCount += requestCountCopy;
                  lock.unlock();
                }
              }
            });
    usageThread.setDaemon(true);
    usageThread.start();
  }

  /**
   * Entry point.
   *
   * @param args
   */
  public static void main(String[] args) {
    configureDatabase();
    configureUploadDirectory();
    configureOAuth();
    configureAdminProperties();

    startUsageThread();

    staticFiles.externalLocation("upload");

    Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();

    internalServerError(gson.toJson(err("Inteneral server error")).toString());
    notFound(gson.toJson(err("Not a valid route")).toString());

    before((request, response) -> response.type("application/json"));

    exception(
        DatabaseException.class,
        (exception, request, response) -> {
          // Handle database exception
          log.error("DatabaseException:", exception);
          response.body(gson.toJson(err(exception.getMessage())));
        });
    exception(
        AdminUnauthenticatedException.class,
        (exception, request, response) -> {
          // Handle authentication exception by redirecting to authentication handler
          response.body(gson.toJson(err(exception.getMessage())));
        });
    exception(
        UnauthenticatedException.class,
        (exception, request, response) -> {
          // Handle authentication exception by redirecting to authentication handler
          response.redirect("/api/oauth");
        });
    exception(
        IllegalArgumentException.class,
        (exception, request, response) -> {
          // Handle illegal argument exception by sending error
          log.error("IllegalArgumentException:", exception);
          response.body(gson.toJson(err(exception.getMessage())));
        });
    exception(
        DateTimeParseException.class,
        (exception, request, response) -> {
          // Handle illegal argument exception by sending error
          response.body(gson.toJson(err(exception.getMessage())));
        });
    // API path
    path(
        "/api",
        () -> {
          // Admin API path
          path(
              "/admin",
              () -> {
                get(
                    "/authenticate",
                    (request, response) -> {
                      try {
                        authenticateAdmin(request);
                        return ok(null);
                      } catch (AdminUnauthenticatedException e1) {
                        String password = request.queryParamOrDefault("password", "");
                        try {
                          MessageDigest digest = MessageDigest.getInstance("SHA-256");
                          byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                          String hash = Session.bytesToHex(bytes);
                          if (hash.equals(adminPasshash)) {
                            // Generate a session
                            String adminSession = Session.generateID(64);
                            // Insert the admin session with a TTL of 4 hours
                            adminSessions.put(adminSession, Instant.now().plusSeconds(14400));
                            // Set a secure cookie for the admin session token
                            response.cookie("/", "admin_session", adminSession, 14400, false);
                            return ok(null);
                          } else throw new AdminUnauthenticatedException("Password incorrect");
                        } catch (NoSuchAlgorithmException e2) {
                          return err(e2.getMessage());
                        }
                      }
                    },
                    gson::toJson);
                get(
                    "/usage",
                    (request, response) -> {
                      authenticateAdmin(request);
                      return ok(Database.getInstance().getUsage());
                    },
                    gson::toJson);
                get(
                    "/users",
                    (request, response) -> {
                      authenticateAdmin(request);
                      return ok(Database.getInstance().getUserCount());
                    },
                    gson::toJson);
                path(
                    "/groups/:group",
                    () -> {
                      get(
                          "enable",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":group"));
                            ContentGroup.getByID(id).setEnabled(true);
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "disable",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":group"));
                            ContentGroup.getByID(id).setEnabled(false);
                            return ok(null);
                          },
                          gson::toJson);
                    });
                path(
                    "/maps",
                    () -> {
                      post(
                          "/create/:name",
                          (request, response) -> {
                            authenticateAdmin(request);
                            return ok(
                                new ConferenceMap(
                                    request.params("name"), acceptUploadedImage(request)));
                          },
                          gson::toJson);
                      get(
                          "/setimage/:map",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":map"));
                            ConferenceMap map = ConferenceMap.getByID(id);
                            map.setImage(acceptUploadedImage(request));
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/rename/:map/:name",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":map"));
                            String name = request.params(":name");
                            ConferenceMap.getByID(id).setName(name);
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/mark/:map/:name/:desc/:x/:y",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":map"));
                            ConferenceMap map = ConferenceMap.getByID(id);
                            String name = request.params(":name");
                            String desc = request.params(":desc");
                            int x = Integer.parseInt(request.params(":x"));
                            int y = Integer.parseInt(request.params(":y"));
                            MapMarker marker = map.addMarker(name, desc, x, y);
                            return ok(marker);
                          },
                          gson::toJson);
                      get(
                          "/remove/:map",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":map"));
                            ConferenceMap.getByID(id).delete();
                            return ok(null);
                          },
                          gson::toJson);
                    });
                path(
                    "/markers",
                    () -> {
                      get(
                          "/update/:marker/:name/:desc/:x/:y",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":marker"));
                            String name = request.params(":name");
                            String desc = request.params(":desc");
                            int x = Integer.parseInt(request.params(":x"));
                            int y = Integer.parseInt(request.params(":y"));
                            MapMarker marker = MapMarker.getByID(id);
                            marker.set(name, desc, x, y);
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/remove/:marker",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":marker"));
                            MapMarker.getByID(id).delete();
                            return ok(null);
                          },
                          gson::toJson);
                    });
                path(
                    "/events",
                    () -> {
                      get(
                          "/create/:name/:desc/:start/:end",
                          (request, response) -> {
                            authenticateAdmin(request);
                            String name = request.params("name");
                            String desc = request.params("desc");
                            Instant start =
                                Instant.ofEpochSecond(Long.parseLong(request.params(":start")));
                            Instant end =
                                Instant.ofEpochSecond(Long.parseLong(request.params(":end")));
                            Event event = new Event(name, desc, start, end);
                            return ok(event);
                          },
                          gson::toJson);
                      get(
                          "/update/:event/:name/:desc/:start/:end",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":event"));
                            String name = request.params(":name");
                            String desc = request.params(":desc");
                            Instant start =
                                Instant.ofEpochSecond(Long.parseLong(request.params(":start")));
                            Instant end =
                                Instant.ofEpochSecond(Long.parseLong(request.params(":end")));
                            Event event = Event.getByID(id);
                            event.set(name, desc, start, end);
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/remove/:event",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":event"));
                            Event event = Event.getByID(id);
                            event.delete();
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/interest/:event",
                          (request, response) -> {
                            authenticateAdmin(request);
                            long id = Long.parseLong(request.params(":event"));
                            Event event = Event.getByID(id);
                            return ok(event.getInterestedCount());
                          },
                          gson::toJson);
                    });
              });
          // User facing API calls
          path(
              "/oauth",
              () -> {
                get(
                    "/callback",
                    (request, response) -> {
                      String id = request.queryParamOrDefault("state", "");
                      String code = request.queryParamOrDefault("code", "");
                      if (id == null || code == null)
                        throw new UnauthenticatedException("Could not get access token");
                      Session session = Session.getByID(id);
                      Type type = new TypeToken<Map<String, Object>>() {}.getType();
                      Map<String, Object> json =
                          gson.fromJson(
                              HttpRequest.get(
                                      facebookGraphURL + "oauth/access_token",
                                      true,
                                      "client_id",
                                      oauthID,
                                      "client_secret",
                                      oauthSecret,
                                      "code",
                                      code,
                                      "redirect_uri",
                                      "https://" + request.host() + "/api/oauth/callback")
                                  .accept("application/json")
                                  .body(),
                              type);
                      if (json.containsKey("access_token")) {
                        String token = (String) json.get("access_token");
                        json = graphRequest("me", token);
                        String name = (String) json.get("name");
                        long facebookID = Long.parseLong((String) json.get("id"));
                        User user;
                        if (!User.existsWithID(facebookID)) user = new User(facebookID, name);
                        else user = User.getByID(facebookID);
                        session.activateFor(user, token);
                        return gson.toJson(ok(facebookID));
                      } else throw new UnauthenticatedException("Could not get access token");
                    });
                get(
                    "/",
                    (request, response) -> {
                      String id = request.queryParams("session");
                      if (id == null) {
                        Session session = new Session();
                        return gson.toJson(ok(session.getID()));
                      } else {
                        response.redirect(
                            oauthAuthorisationURL
                                + "?client_id="
                                + oauthID
                                + "&redirect_uri=https://"
                                + request.host()
                                + "/api/oauth/callback&scope=&state="
                                + id);
                        return null;
                      }
                    });
              });
          path(
              "/",
              () -> {
                before(
                    "/*",
                    (request, response) -> {
                      lock.lock();
                      requestCount += 1;
                      lock.unlock();
                    });
                get(
                    "/maps",
                    (request, response) -> ok(Database.getInstance().getMaps()),
                    gson::toJson);
                get(
                    "/markers/:map",
                    (request, response) -> {
                      long id = Long.parseLong(request.params(":map"));
                      ConferenceMap map = ConferenceMap.getByID(id);
                      return ok(map.getMarkers());
                    },
                    gson::toJson);
                get(
                    "/events",
                    (request, response) -> ok(Database.getInstance().getEvents()),
                    gson::toJson);
                get(
                    "/achievements",
                    (request, response) -> ok(Database.getInstance().getAchievements()),
                    gson::toJson);
                get(
                    "/groups",
                    (request, response) -> ok(Database.getInstance().getContentGroups()),
                    gson::toJson);
                path(
                    "/user",
                    () -> {
                      get(
                          "/",
                          (request, response) -> {
                            User user = authenticatedUserFor(request);
                            return ok(user.getID());
                          },
                          gson::toJson);
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
                          "/interested",
                          (request, response) -> {
                            User user = authenticatedUserFor(request);
                            return ok(user.getMarkedEvents());
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
                      get(
                          "/score",
                          (request, response) -> {
                            User user = authenticatedUserFor(request);
                            return ok(user.getScore());
                          },
                          gson::toJson);
                    });
                path(
                    "/users",
                    () -> {
                      get(
                          "/query/:query",
                          (request, response) -> {
                            authenticatedUserFor(request);
                            // TODO
                            return ok(null);
                          },
                          gson::toJson);
                      get(
                          "/:user/profile",
                          (request, response) -> {
                            authenticatedUserFor(request);
                            // TODO
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

  // Utility method for making requests to the Facebook graph API
  private static Map<String, Object> graphRequest(String url, String token, Object... fields) {
    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<Map<String, Object>>() {}.getType();
    Map<String, Object> json =
        gson.fromJson(
            HttpRequest.get(facebookGraphURL + url, true, "access_token", token)
                .accept("application/json")
                .body(),
            type);
    return json;
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
      else status = "error";
      this.error = error;
      this.payload = payload;
    }
  }
}
