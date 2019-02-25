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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User.java
 *
 * @author Nathan Corbyn
 */
public class User {
  public static final String TABLE = "users", ID_FIELD = "id", NAME_FIELD = "name";

  private long id;
  private String name;

  /**
   * Create a new user with the given name.
   *
   * @param id the ID for the new user
   * @param name the name of the new user
   * @throws IllegalArgumentException if name is null or empty
   * @throws DatabaseException if the user could not be added to the database
   */
  public User(long id, String name) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    this.name = name;
    this.id = id;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO " + TABLE + "(" + ID_FIELD + ", " + NAME_FIELD + ") VALUES (?, ?)");
      stmt.setLong(1, id);
      stmt.setString(2, name);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private User() {}

  /** @return the ID of the user */
  public long getID() {
    return this.id;
  }

  /**
   * @return the score for the user
   * @throws DatabaseException if the database could not be accessed
   */
  public int getScore() throws DatabaseException {
    int score = 0;
    for (Achievement achievement : getAchievements()) score += achievement.getReward();
    return score;
  }

  /**
   * @return the achievements of the user
   * @throws DatabaseException if the database could not be accessed
   */
  public List<Achievement> getAchievements() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      List<Achievement> result = new ArrayList<>();
      PreparedStatement stmt =
          conc.prepareStatement(
              "SELECT "
                  + Achievement.ACHIEVED_TABLE
                  + "."
                  + Achievement.ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + ", "
                  + Achievement.TABLE
                  + "."
                  + Achievement.NAME_FIELD
                  + " FROM "
                  + Achievement.ACHIEVED_TABLE
                  + " JOIN "
                  + Achievement.TABLE
                  + " ON "
                  + Achievement.ACHIEVED_TABLE
                  + "."
                  + Achievement.ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + " = "
                  + Achievement.TABLE
                  + "."
                  + Achievement.ID_FIELD
                  + " WHERE "
                  + Achievement.ACHIEVED_TABLE
                  + "."
                  + Achievement.ACHIEVED_USER_ID_FIELD
                  + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(Achievement.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Have the user achieve the give achievement.
   *
   * @param achievement the achievement to achieve
   * @throws DatabaseException if the database could not be accessed
   */
  public void achieve(Achievement achievement) throws DatabaseException {
    if (achievement == null) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO "
                  + Achievement.ACHIEVED_TABLE
                  + "("
                  + Achievement.ACHIEVED_USER_ID_FIELD
                  + ", "
                  + Achievement.ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + ") VALUES (?, ?)");
      stmt.setLong(1, id);
      stmt.setLong(2, achievement.getID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Revoke the given achievement from the user.
   *
   * @param achievement the achievement to revoke
   * @throws DatabaseException if the database could not be accessed
   */
  public void revoke(Achievement achievement) throws DatabaseException {
    if (achievement == null) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "DELETE FROM "
                  + Achievement.ACHIEVED_TABLE
                  + " WHERE "
                  + Achievement.ACHIEVED_USER_ID_FIELD
                  + " = ? AND "
                  + Achievement.ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + " = ?");
      stmt.setLong(1, id);
      stmt.setLong(2, achievement.getID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the list of events that the user has marked their interest in
   * @throws DatabaseException if the database could not be accessed
   */
  public List<Event> getMarkedEvents() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      List<Event> result = new ArrayList<>();
      PreparedStatement stmt =
          conc.prepareStatement(
              "SELECT "
                  + Event.INTERESTED_TABLE
                  + "."
                  + Event.INTERESTED_EVENT_ID_FIELD
                  + ", "
                  + Event.TABLE
                  + "."
                  + Event.NAME_FIELD
                  + ", "
                  + Event.TABLE
                  + "."
                  + Event.DESC_FIELD
                  + ", "
                  + Event.TABLE
                  + "."
                  + Event.START_FIELD
                  + ", "
                  + Event.TABLE
                  + "."
                  + Event.END_FIELD
                  + " FROM "
                  + Event.INTERESTED_TABLE
                  + " JOIN "
                  + Event.TABLE
                  + " ON "
                  + Event.INTERESTED_TABLE
                  + "."
                  + Event.INTERESTED_EVENT_ID_FIELD
                  + " = "
                  + Event.TABLE
                  + "."
                  + Event.ID_FIELD
                  + " WHERE "
                  + Event.INTERESTED_TABLE
                  + "."
                  + Event.INTERESTED_USER_ID_FIELD
                  + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(Event.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Mark interest in the given event.
   *
   * @param event the event to mark interest in
   * @throws DatabaseException if the database could not be accessed
   */
  public void markInterestIn(Event event) throws DatabaseException {
    if (event == null) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO "
                  + Event.INTERESTED_TABLE
                  + "("
                  + Event.INTERESTED_USER_ID_FIELD
                  + ", "
                  + Event.INTERESTED_EVENT_ID_FIELD
                  + ") VALUES (?, ?)");
      stmt.setLong(1, id);
      stmt.setLong(2, event.getID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Unmark interest in the given event.
   *
   * @param event the event to unmark interest in
   * @throws DatabaseException if the database could not be accessed
   */
  public void unmarkInterestIn(Event event) throws DatabaseException {
    if (event == null) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "DELETE FROM "
                  + Event.INTERESTED_TABLE
                  + " WHERE "
                  + Event.INTERESTED_USER_ID_FIELD
                  + " = ? AND "
                  + Event.INTERESTED_EVENT_ID_FIELD
                  + " = ?");
      stmt.setLong(1, id);
      stmt.setLong(2, event.getID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /** @return the name of the achievement */
  public String getName() {
    return name;
  }

  /**
   * Delete the user. This will also delete all of the user's achievements.
   *
   * @throws DatabaseException if the user could not be deleted
   */
  public void delete() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      try {
        // Need to execute several transactions atomically, so disable auto-commit
        conc.setAutoCommit(false);
        PreparedStatement stmt =
            conc.prepareStatement(
                "DELETE FROM "
                    + Achievement.ACHIEVED_TABLE
                    + " WHERE "
                    + Achievement.ACHIEVED_USER_ID_FIELD
                    + " = ?");
        stmt.setLong(1, id);
        stmt.executeUpdate();
        stmt =
            conc.prepareStatement(
                "DELETE FROM "
                    + Event.INTERESTED_TABLE
                    + " WHERE "
                    + Event.INTERESTED_USER_ID_FIELD
                    + " = ?");
        stmt.setLong(1, id);
        stmt.executeUpdate();
        stmt =
            conc.prepareStatement(
                "DELETE FROM " + Session.TABLE + " WHERE " + Session.USER_FIELD + " = ?");
        stmt.setLong(1, id);
        stmt.executeUpdate();

        stmt = conc.prepareStatement("DELETE FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
        stmt.setLong(1, id);
        stmt.executeUpdate();
        // Commit if both transactions were successful
        conc.commit();
      } catch (SQLException e) {
        conc.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Get an existing user by ID.
   *
   * @param id the ID of the user
   * @return the constructed user
   * @throws DatabaseException if the user could not be found
   */
  public static User getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No user with ID " + id);
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Check if a record of a user with the given ID exists.
   *
   * @param id the ID of the user
   * @return true if the user exists, false otherwise
   * @throws DatabaseException if the database could not be accessed
   */
  public static boolean existsWithID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      return rs.first();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct a user from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed user
   * @throws DatabaseException if the user could not be constructed
   */
  static User from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      User user = new User();
      user.id = rs.getLong(ID_FIELD);
      user.name = rs.getString(NAME_FIELD);
      return user;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
