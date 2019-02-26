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
package uk.ac.cam.cl.kilo.data;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

/**
 * Session.java
 *
 * @author Nathan Corbyn
 */
public class Session {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  private static final int LENGTH = 64, LIFETIME = 14400000;
  public static final String TABLE = "sessions",
      ID_FIELD = "id",
      TOKEN_FIELD = "token",
      USER_FIELD = "user_id",
      ACTIVATED_FIELD = "activated",
      EXPIRES_FIELD = "expires";
  private String id, token;
  private long user;
  private boolean activated;
  private Instant expires;

  /**
   * Create a new unactivated session.
   *
   * @throws DatabaseException if the session could not be added to the database
   */
  public Session() throws DatabaseException {
    id = generateID(LENGTH);
    activated = false;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO " + TABLE + "(" + ID_FIELD + ", " + ACTIVATED_FIELD + ") VALUES (?, ?)");
      stmt.setString(1, id);
      stmt.setBoolean(2, false);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Session(String id, String token, long user, boolean activated, Instant expires) {
    assert (id != null);
    assert (!activated || (token != null && expires != null));
    this.id = id;
    this.token = token;
    this.user = user;
    this.activated = activated;
    this.expires = expires;
  }

  /**
   * Activates the session for the given user.
   *
   * @param user the user to activate the session for
   * @throws DatabaseException if the database could not be accessed
   * @throws IllegalArgumentException if the give user of token is null
   */
  public void activateFor(User user, String token) throws DatabaseException {
    if (user == null || token == null)
      throw new IllegalArgumentException("User and token must be non-null");
    if (!activated) {
      Instant expires = Instant.now().plusMillis(LIFETIME);
      try (Connection conc = Database.getInstance().getConnection()) {
        PreparedStatement stmt =
            conc.prepareStatement(
                "UPDATE "
                    + TABLE
                    + " SET "
                    + USER_FIELD
                    + " = ?, "
                    + TOKEN_FIELD
                    + " = ?, "
                    + ACTIVATED_FIELD
                    + " = ?, "
                    + EXPIRES_FIELD
                    + " = ? WHERE "
                    + ID_FIELD
                    + " = ?");
        stmt.setLong(1, user.getID());
        stmt.setString(2, token);
        stmt.setBoolean(3, true);
        stmt.setTimestamp(4, Timestamp.from(expires));
        stmt.setString(5, id);
        stmt.executeUpdate();
        // Only update if transaction is successful
        this.user = user.getID();
        this.token = token;
        this.expires = expires;
        activated = true;
      } catch (SQLException e) {
        throw new DatabaseException(e);
      }
    }
  }

  /** @return true if the session has expired, false otherwise */
  public boolean hasExpired() {
    return expires.isBefore(Instant.now());
  }

  /** @return true if the session has been activated, false otherwise */
  public boolean isActivated() {
    return activated;
  }

  /** @return the ID for the session */
  public String getID() {
    return id;
  }

  /** @return the Facebook token for the session */
  public String getToken() {
    return token;
  }

  /**
   * @return the user corresponding to the session
   * @throws DatabaseException if the database could not be accessed
   */
  public User getUser() throws DatabaseException {
    if (activated) {
      return User.getByID(user);
    } else {
      return null;
    }
  }

  /**
   * Get an existing session by ID.
   *
   * @param id the ID of the session
   * @return the constructed session
   * @throws DatabaseException if the session could not be found
   */
  public static Session getByID(String id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setString(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No session with ID '" + id + "'");
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct a session from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed session
   * @throws DatabaseException if the session could not be constructed
   */
  static Session from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      String id = rs.getString(ID_FIELD);
      String token = rs.getString(TOKEN_FIELD);
      long user = rs.getLong(USER_FIELD);
      boolean activated = rs.getBoolean(ACTIVATED_FIELD);
      Instant expires = rs.getTimestamp(EXPIRES_FIELD).toInstant();
      return new Session(id, token, user, activated, expires);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
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
}
