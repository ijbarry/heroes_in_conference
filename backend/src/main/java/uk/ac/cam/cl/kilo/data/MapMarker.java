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
 * MapMarker.java
 *
 * @author Nathan Corbyn
 */
public class MapMarker {
  public static final String TABLE = "markers",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      DESC_FIELD = "desc",
      X_FIELD = "x",
      Y_FIELD = "y",
      MAP_FIELD = "map_id";
  private long id, map;
  private String name, desc;
  private int x, y;

  /**
   * Create a new map marker for the given map with the given name, description and position.
   *
   * @param map the map to mark
   * @param name the name of the marker
   * @param desc the description of the marker
   * @param x the x coordinate of the marker
   * @param y the y coordinate of the marker
   * @throws IllegalArgumentException if the map is null or the name is null or empty
   * @throws DatabaseException if the marker could not be added to the database
   */
  public MapMarker(ConferenceMap map, String name, String desc, int x, int y)
      throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    if (desc == null) desc = "";
    if (map == null) throw new IllegalArgumentException("Map must bot be null");
    this.name = name;
    this.desc = desc;
    this.map = map.getID();
    this.x = x;
    this.y = y;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO "
                  + TABLE
                  + "("
                  + MAP_FIELD
                  + ", "
                  + NAME_FIELD
                  + ", "
                  + DESC_FIELD
                  + ", "
                  + X_FIELD
                  + ", "
                  + Y_FIELD
                  + ") VALUES (?, ?, ?, ? , ?)");
      stmt.setLong(1, this.map);
      stmt.setString(2, name);
      stmt.setString(3, desc);
      stmt.setInt(4, x);
      stmt.setInt(5, y);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.first()) throw new DatabaseException("Failed to generate ID for marker");
      id = rs.getLong(ID_FIELD);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private MapMarker(long id, long map, String name, String desc, int x, int y) {
    assert (name != null);
    assert (!name.equals(""));
    assert (desc != null);
    this.id = id;
    this.map = map;
    this.name = name;
    this.desc = desc;
    this.x = x;
    this.y = y;
  }

  /** @return the ID of the marker */
  public long getID() {
    return id;
  }

  /** @return the name of the marker */
  public String getName() {
    return name;
  }

  /**
   * @param name the new name for the marker
   * @throws DatabaseException if the name could not be updated
   * @throws IllegalArgumentException if the name is null or empty
   */
  public void setName(String name) throws DatabaseException {
    if (name == null || name.equals(""))
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

  /** @return the description of the marker */
  public String getDescription() {
    return desc;
  }

  /**
   * @param desc the new description for the marker
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

  /** @return the x-coordinate of the marker */
  public int getX() {
    return x;
  }

  /** @return the y-coordinate of the marker */
  public int getY() {
    return y;
  }

  /**
   * @param x the new x-coordinate for the marker
   * @param y the new y-coordinate for the marker
   * @throws DatabaseException if the position could not be updated
   */
  public void setPosition(int x, int y) throws DatabaseException {
    if (x == this.x && y == this.y) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE "
                  + TABLE
                  + " SET "
                  + X_FIELD
                  + " = ?, "
                  + Y_FIELD
                  + " = ? WHERE "
                  + ID_FIELD
                  + " = ?");
      stmt.setInt(1, x);
      stmt.setInt(2, y);
      stmt.setLong(3, id);
      stmt.executeUpdate();
      this.x = x;
      this.y = y;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the map being marker
   * @throws DatabaseException if the database could not be accessed
   */
  public ConferenceMap getMap() throws DatabaseException {
    return ConferenceMap.getByID(map);
  }

  /**
   * Delete the marker.
   *
   * @throws DatabaseException if the marker could not be deleted
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
   * Get an existing marker by ID.
   *
   * @param id the ID of the marker
   * @return the constructed marker
   * @throws DatabaseException if the marker could not be found
   */
  public static MapMarker getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No marker with ID " + id);
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct a marker from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed marker
   * @throws DatabaseException if the marker could not be constructed
   */
  static MapMarker from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      long id = rs.getLong(ID_FIELD);
      long map = rs.getLong(MAP_FIELD);
      String name = rs.getString(NAME_FIELD);
      String desc = rs.getString(DESC_FIELD);
      int x = rs.getInt(X_FIELD);
      int y = rs.getInt(Y_FIELD);
      return new MapMarker(id, map, name, desc, x, y);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
