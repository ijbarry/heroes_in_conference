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
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Event.java
 *
 * @author Nathan Corbyn
 */
public class Event {
  public static final String TABLE = "events",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      DESC_FIELD = "description",
      START_FIELD = "start",
      END_FIELD = "end",
      LOC_FIELD = "location";
  // Auxiliary table parameters for 'is interested in' relation
  public static final String INTERESTED_TABLE = "interested",
      INTERESTED_USER_ID_FIELD = "user_id",
      INTERESTED_EVENT_ID_FIELD = "event_id";

  private long id;
  private String name;
  private String desc;
  private Instant start, end;

  /**
   * Create a new event with the given name, description, and timing.
   *
   * @param name the name of the new event
   * @param desc the description of the new event
   * @param start the start time of the event
   * @param end the end time of the event
   * @throws IllegalArgumentException if name is null or empty, or the timings are invalid
   * @throws DatabaseException if the event could not be added to the database
   */
  public Event(String name, String desc, Instant start, Instant end) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    if (desc == null) desc = "";
    if (start == null || end == null)
      throw new IllegalArgumentException("The start and end times must not be null");
    if (start.isAfter(end)) throw new IllegalArgumentException("Event ends before it starts");
    this.name = name;
    this.desc = desc;
    this.start = start;
    this.end = end;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO "
                  + TABLE
                  + "("
                  + NAME_FIELD
                  + ", "
                  + DESC_FIELD
                  + ", "
                  + START_FIELD
                  + ", "
                  + END_FIELD
                  + ") VALUES (?, ?, ?, ?)",
              PreparedStatement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.setString(2, desc);
      stmt.setTimestamp(3, Timestamp.from(start));
      stmt.setTimestamp(4, Timestamp.from(end));
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.first()) throw new DatabaseException("Failed to generate ID for event");
      id = rs.getLong(1);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Event(long id, String name, String desc, Instant start, Instant end) {
    assert (name != null);
    assert (!name.equals(""));
    assert (desc != null);
    assert (start != null);
    assert (end != null);
    this.id = id;
    this.name = name;
    this.desc = desc;
    this.start = start;
    this.end = end;
  }

  /** @return the ID of the event */
  public long getID() {
    return this.id;
  }

  /** @return the name of the event */
  public String getName() {
    return name;
  }

  /**
   * @param name the new name for the event
   * @throws DatabaseException if the name could not be updated
   * @throws IllegalArgumentException if the name is null or empty
   */
  public void setName(String name) throws DatabaseException {
    if (name == null || name.length() < 4)
      throw new IllegalArgumentException("Name must not be null or less than four characters");
    if (name.equals(this.name)) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE " + TABLE + " SET " + NAME_FIELD + " = ? WHERE " + ID_FIELD + " = ?");
      stmt.setString(1, name);
      stmt.setLong(2, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.name = name;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /** @return the description of the event */
  public String getDescription() {
    return desc;
  }

  /**
   * @param desc the new description for the event
   * @throws DatabaseException if the description could not be updated
   */
  public void setDescription(String desc) throws DatabaseException {
    if (desc == null) desc = "";
    if (desc.equals(this.desc)) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE " + TABLE + " SET " + DESC_FIELD + " = ? WHERE " + ID_FIELD + " = ?");
      stmt.setString(1, desc);
      stmt.setLong(2, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.desc = desc;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /** @return the start time of the event */
  public Instant getStart() {
    return start;
  }

  /** @return the end time of the event */
  public Instant getEnd() {
    return end;
  }

  /**
   * @param start the new start time for the event
   * @param end the new end time for the event
   * @throws IllegalArgumentException if the timings are null, or the end is before the start time
   * @throws DatabaseException if the event could not be updated
   */
  public void setTiming(Instant start, Instant end) throws DatabaseException {
    if (start == null || end == null)
      throw new IllegalArgumentException("The start and end times must not be null");
    if (start.isAfter(end)) throw new IllegalArgumentException("Event ends before it starts");
    if (start.equals(this.start) && end.equals(this.end)) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE "
                  + TABLE
                  + " SET "
                  + START_FIELD
                  + " = ?, "
                  + END_FIELD
                  + " = ? WHERE "
                  + ID_FIELD
                  + " = ?");
      stmt.setTimestamp(1, Timestamp.from(start));
      stmt.setTimestamp(2, Timestamp.from(end));
      stmt.setLong(3, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.start = start;
      this.end = end;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @param name the new name for the event
   * @param desc the new description for the event
   * @param start the new start time for the event
   * @param end the new end time for the event
   * @throws IllegalArgumentException if the name is null or empty, or the timings are invalid
   * @throws DatabaseException if the event could not be updated
   */
  public void set(String name, String desc, Instant start, Instant end) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    if (desc == null) desc = "";
    if (start == null || end == null)
      throw new IllegalArgumentException("The start and end times must not be null");
    if (start.isAfter(end)) throw new IllegalArgumentException("Event ends before it starts");
    if (this.name.equals(name)
        && this.desc.equals(desc)
        && this.start.equals(start)
        && this.end.equals(end)) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE "
                  + TABLE
                  + " SET "
                  + NAME_FIELD
                  + " = ?, "
                  + DESC_FIELD
                  + " = ?, "
                  + START_FIELD
                  + " = ?, "
                  + END_FIELD
                  + " = ? WHERE "
                  + ID_FIELD
                  + " = ?");
      stmt.setString(1, name);
      stmt.setString(2, desc);
      stmt.setTimestamp(3, Timestamp.from(start));
      stmt.setTimestamp(4, Timestamp.from(end));
      stmt.setLong(5, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.name = name;
      this.desc = desc;
      this.start = start;
      this.end = end;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the number of users interested in the event
   * @throws DatabaseException if the database could not be accessed
   */
  public int getInterestedCount() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "SELECT COUNT(*) FROM "
                  + INTERESTED_TABLE
                  + " WHERE "
                  + INTERESTED_EVENT_ID_FIELD
                  + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      // We assert here because a COUNT(*) query should always return a single row
      assert (rs.first());
      return rs.getInt(1);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Delete the event. This will also delete all records of users marking themselves as interested
   * in this event.
   *
   * @throws DatabaseException if the event could not be deleted
   */
  public void delete() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      try {
        // Need to execute two transactions atomically, so disable auto-commit
        conc.setAutoCommit(false);
        PreparedStatement stmt =
            conc.prepareStatement("DELETE FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
        stmt.setLong(1, id);
        stmt.executeUpdate();
        stmt =
            conc.prepareStatement(
                "DELETE FROM " + INTERESTED_TABLE + " WHERE " + INTERESTED_EVENT_ID_FIELD + " = ?");
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
   * Get an existing event by ID.
   *
   * @param id the ID of the event
   * @return the constructed event
   * @throws DatabaseException if the event could not be found
   */
  public static Event getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No event with ID " + id);
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct an event from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed achievement
   * @throws DatabaseException if the achievement could not be constructed
   */
  static Event from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      long id = rs.getLong(ID_FIELD);
      String name = rs.getString(NAME_FIELD);
      String desc = rs.getString(DESC_FIELD);
      Instant start = rs.getTimestamp(START_FIELD).toInstant();
      Instant end = rs.getTimestamp(END_FIELD).toInstant();
      return new Event(id, name, desc, start, end);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
