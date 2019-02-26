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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ConferenceMap.java
 *
 * @author Nathan Corbyn
 */
public class ConferenceMap {
  public static final String TABLE = "maps",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      IMAGE_FIELD = "image";

  private long id;
  private String name;
  private Path image;

  /**
   * Create a new map with the given name and image.
   *
   * @param name the name of the new map
   * @param image the path the to the map image
   * @throws IllegalArgumentException if the name is null or empty, or the image is null
   * @throws DatabaseException if the map could not be added to the database
   */
  public ConferenceMap(String name, Path image) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    if (image == null) throw new IllegalArgumentException("Image must not be null");
    this.name = name;
    this.image = image;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO " + TABLE + "(" + NAME_FIELD + ", " + IMAGE_FIELD + ") VALUES (?, ?)",
              PreparedStatement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.setString(2, image.toString());
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.first()) throw new DatabaseException("Failed to generate ID for map");
      id = rs.getLong(ID_FIELD);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private ConferenceMap(long id, String name, Path image) {
    assert (name != null);
    assert (!name.equals(""));
    assert (image != null);
    this.id = id;
    this.name = name;
    this.image = image;
  }

  /** @return the ID of the map */
  public long getID() {
    return this.id;
  }

  /** @return the name of the map */
  public String getName() {
    return name;
  }

  /**
   * @param name the new name for the map
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

  /** @return the path of the map image */
  public Path getImage() {
    return image;
  }

  /**
   * @param image the new image for the map
   * @throws DatabaseException if the image could not be updated
   * @throws IllegalArgumentException if the image is null
   */
  public void setImage(Path image) throws DatabaseException {
    if (image == null) throw new IllegalArgumentException("Image must not be null");
    if (image.equals(this.image)) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE " + TABLE + " SET " + IMAGE_FIELD + " = ? WHERE " + ID_FIELD + " = ?");
      stmt.setString(1, image.toString());
      stmt.setLong(2, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.image = image;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the markers of the map
   * @throws DatabaseException if the database could not be accessed
   */
  public List<MapMarker> getMarkers() throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      List<MapMarker> result = new ArrayList<>();
      PreparedStatement stmt =
          conc.prepareStatement(
              "SELECT * FROM " + MapMarker.TABLE + " WHERE " + MapMarker.MAP_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(MapMarker.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Add a map marker to the map.
   *
   * @return the newly constructed map marker
   * @throws DatabaseException if the database could not be accessed
   */
  public MapMarker addMarker(String name, String desc, int x, int y) throws DatabaseException {
    return new MapMarker(this, name, desc, x, y);
  }

  /**
   * Delete the map. This will also delete all markers attached to the map.
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
                "DELETE FROM " + MapMarker.TABLE + " WHERE " + MapMarker.MAP_FIELD + " = ?");
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
   * Get an existing map by ID.
   *
   * @param id the ID of the map
   * @return the constructed map
   * @throws DatabaseException if the map could not be found
   */
  public static ConferenceMap getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No map with ID " + id);
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct a map from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed map
   * @throws DatabaseException if the map could not be constructed
   */
  static ConferenceMap from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      long id = rs.getLong(ID_FIELD);
      String name = rs.getString(NAME_FIELD);
      Path image = Paths.get(rs.getString(IMAGE_FIELD));
      return new ConferenceMap(id, name, image);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
