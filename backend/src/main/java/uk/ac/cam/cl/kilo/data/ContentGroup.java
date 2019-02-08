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

/**
 * ContentGroup.java
 *
 * @author Nathan Corbyn
 */
public class ContentGroup {
  public static final String TABLE = "groups",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      ENABLED_FIELD = "enabled";

  private long id;
  private boolean enabled;
  private String name;

  /**
   * Create a new content group with the given name.
   *
   * @param name the name of the new content group
   * @throws IllegalArgumentException if name is null or name is empty
   * @throws DatabaseException if the content group could not be added to the database
   */
  public ContentGroup(String name) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    this.name = name;
    enabled = true;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("INSERT INTO " + TABLE + "(" + NAME_FIELD + ") VALUES (?)");
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.first()) throw new DatabaseException("Failed to generate ID for content group");
      id = rs.getLong(ID_FIELD);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private ContentGroup(long id, String name, boolean enabled) {
    assert (name != null && !name.equals(""));
    this.id = id;
    this.name = name;
    this.enabled = enabled;
  }

  /** @return the ID of the content group */
  public long getID() {
    return this.id;
  }

  /**
   * @param enabled the new status of the content group
   * @throws DatabaseException if the status could not be updated
   */
  public void setEnabled(boolean enabled) throws DatabaseException {
    if (enabled == this.enabled) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE " + TABLE + " SET " + ENABLED_FIELD + " = ? WHERE " + ID_FIELD + " = ?");
      stmt.setBoolean(1, enabled);
      stmt.setLong(2, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.enabled = enabled;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /** @return if the content group is enabled */
  public boolean isEnabled() {
    return enabled;
  }

  /** @return the name of the content group */
  public String getName() {
    return name;
  }

  /**
   * @param name the new name for the content group
   * @throws DatabaseException if the name could not be updated
   * @throws IllegalArgumentException if the name is empty or null
   */
  public void setName(String name) throws DatabaseException {
    if (name == null || name == "")
      throw new IllegalArgumentException("Name must not be null or empty");
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

  /**
   * Delete the content group.
   *
   * @throws DatabaseException if the group could not be deleted
   */
  public void delete() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("DELETE FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Get an existing content group by ID.
   *
   * @param id the id of the content group
   * @return the constructed content group
   * @throws DatabaseException if the content group could not be found
   */
  public static ContentGroup getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No content group with ID " + id);
      ContentGroup group = from(rs);
      return group;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct a content group from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed content group
   * @throws DatabaseException if the content group could not be constructed
   */
  static ContentGroup from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      long id = rs.getLong(ID_FIELD);
      String name = rs.getString(NAME_FIELD);
      boolean enabled = rs.getBoolean(ENABLED_FIELD);
      return new ContentGroup(id, name, enabled);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
