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
  public static final String TABLE = "content",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      ENABLED_FIELD = "enabled";

  private long id;
  private boolean enabled;
  private String name;

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
