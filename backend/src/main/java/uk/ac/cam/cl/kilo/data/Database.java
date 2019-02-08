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
import javax.sql.DataSource;

/**
 * Database.java
 *
 * @author Nathan Corbyn
 */
public class Database {
  private static Database instance;

  private DataSource source;

  /**
   * Configure the database to work with the given source of data. Please note, calling {@link
   * #getInstance()} before calling {@link #configure(DataSource)} is a logic error and will result
   * in a runtime exception.
   *
   * @param dataSource the JDBC data source to work with
   */
  public static void configure(DataSource dataSource) {
    if (instance == null) instance = new Database(dataSource);
    else throw new RuntimeException("Database may not be reconfigured at runtime");
  }

  /** @return the singleton instance */
  public static Database getInstance() {
    if (instance == null) throw new RuntimeException("Database has not been configured");
    return instance;
  }

  private Database(DataSource source) {
    if (source == null) throw new RuntimeException("Data source must be non null");
    this.source = source;
  }

  /**
   * @return the list of achievements
   * @throws DatabaseException if the database could not be accessed
   */
  public List<Achievement> getAchievements() throws DatabaseException {
    try (Connection conc = getConnection()) {
      List<Achievement> result = new ArrayList<>();
      PreparedStatement stmt = conc.prepareStatement("SELECT * FROM " + Achievement.TABLE);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(Achievement.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the list of events
   * @throws DatabaseException if the database could not be accessed
   */
  public List<Event> getEvents() throws DatabaseException {
    try (Connection conc = getConnection()) {
      List<Event> result = new ArrayList<>();
      PreparedStatement stmt = conc.prepareStatement("SELECT * FROM " + Event.TABLE);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(Event.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the list of content groups
   * @throws DatabaseException if the database could not be accessed
   */
  public List<ContentGroup> getContentGroups() throws DatabaseException {
    try (Connection conc = getConnection()) {
      List<ContentGroup> result = new ArrayList<>();
      PreparedStatement stmt = conc.prepareStatement("SELECT * FROM " + ContentGroup.TABLE);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(ContentGroup.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the list of content groups
   * @throws DatabaseException if the database could not be accessed
   */
  public List<ContentGroup> getEnabledContentGroups() throws DatabaseException {
    try (Connection conc = getConnection()) {
      List<ContentGroup> result = new ArrayList<>();
      PreparedStatement stmt =
          conc.prepareStatement(
              "SELECT * FROM "
                  + ContentGroup.TABLE
                  + " WHERE "
                  + ContentGroup.ENABLED_FIELD
                  + " = ?");
      stmt.setBoolean(1, true);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(ContentGroup.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the list of maps
   * @throws DatabaseException if the database could not be accessed
   */
  public List<ConferenceMap> getMaps() throws DatabaseException {
    try (Connection conc = getConnection()) {
      List<ConferenceMap> result = new ArrayList<>();
      PreparedStatement stmt = conc.prepareStatement("SELECT * FROM " + ConferenceMap.TABLE);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) result.add(ConferenceMap.from(rs));
      return result;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * @return the database connection
   * @throws DatabaseException if no connection could be made
   */
  Connection getConnection() throws DatabaseException {
    try {
      return source.getConnection();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
