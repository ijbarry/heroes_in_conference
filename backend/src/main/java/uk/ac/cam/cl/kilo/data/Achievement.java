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
 * Achievement.java
 *
 * @author Nathan Corbyn
 */
public class Achievement {
  public static final String TABLE = "achievements",
      ID_FIELD = "id",
      NAME_FIELD = "name",
      REWARD_FIELD = "reward";
  // Auxiliary table parameters for 'has achieved' relation
  public static final String ACHIEVED_TABLE = "achieved",
      ACHIEVED_USER_ID_FIELD = "user_id",
      ACHIEVED_ACHIEVEMENT_ID_FIELD = "achievement_id";

  private long id;
  private String name;
  private int reward;

  /**
   * Create a new achievement with the given name.
   *
   * @param name the name of the new achievement
   * @param reward the reward for the achievement (must be non-negative)
   * @throws IllegalArgumentException if name is null or name is empty
   * @throws DatabaseException if the achievement could not be added to the database
   */
  public Achievement(String name, int reward) throws DatabaseException {
    if (name == null || name.equals(""))
      throw new IllegalArgumentException("Name must not be null or empty");
    if (reward < 0) throw new IllegalArgumentException("Reward must be non-negative");
    this.name = name;
    this.reward = reward;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO " + TABLE + "(" + NAME_FIELD + ", " + REWARD_FIELD + ") VALUES (?, ?)");
      stmt.setString(1, name);
      stmt.setInt(2, reward);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (!rs.first()) throw new DatabaseException("Failed to generate ID for achievement");
      id = rs.getLong(ID_FIELD);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private Achievement(long id, String name, int reward) {
    assert (name != null && !name.equals(""));
    assert (reward > 0);
    this.id = id;
    this.name = name;
    this.reward = reward;
  }

  /** @return the ID of the achievement */
  public long getID() {
    return this.id;
  }

  /**
   * @param reward the new reward for the achievement (must be non-negative)
   * @throws DatabaseException if the status could not be updated
   */
  public void setReward(int reward) throws DatabaseException {
    if (reward < 0) throw new IllegalArgumentException("Reward must be non-negative");
    if (reward == this.reward) return;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "UPDATE " + TABLE + " SET " + REWARD_FIELD + " = ? WHERE " + ID_FIELD + " = ?");
      stmt.setInt(1, reward);
      stmt.setLong(2, id);
      stmt.executeUpdate();
      // Only update if transaction is successful
      this.reward = reward;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /** @return the reward for the achievement */
  public int getReward() {
    return reward;
  }

  /** @return the name of the achievement */
  public String getName() {
    return name;
  }

  /**
   * @param name the new name for the achievement
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
   * Delete the achievement. This will also delete all records of users having completed this
   * achievement.
   *
   * @throws DatabaseException if the achievement could not be deleted
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
                "DELETE FROM "
                    + ACHIEVED_TABLE
                    + " WHERE "
                    + ACHIEVED_ACHIEVEMENT_ID_FIELD
                    + " = ?");
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
   * Get an existing achievement by ID.
   *
   * @param id the ID of the achievement
   * @return the constructed achievement
   * @throws DatabaseException if the achievement could not be found
   */
  public static Achievement getByID(long id) throws DatabaseException {
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement("SELECT * FROM " + TABLE + " WHERE " + ID_FIELD + " = ?");
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (!rs.first()) throw new DatabaseException("No achievement with ID " + id);
      return from(rs);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  /**
   * Construct an achievement from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed achievement
   * @throws DatabaseException if the achievement could not be constructed
   */
  static Achievement from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      long id = rs.getLong(ID_FIELD);
      String name = rs.getString(NAME_FIELD);
      int reward = rs.getInt(REWARD_FIELD);
      return new Achievement(id, name, reward);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
