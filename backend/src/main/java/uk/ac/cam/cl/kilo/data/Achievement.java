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
      REWARD_FIELD = "reward",
      COUNT_FIELD = "count";
  // Auxiliary table parameters for 'has achieved' relation
  public static final String ACHIEVED_TABLE = "achieved",
      ACHIEVED_USER_ID_FIELD = "user_id",
      ACHIEVED_ACHIEVEMENT_ID_FIELD = "achievement_id";

  private long id;
  private String name;
  private int reward, count;

  private Achievement(long id, String name, int reward, int count) {
    assert (name != null && !name.equals(""));
    assert (reward > 0);
    assert (count > 0);
    this.id = id;
    this.name = name;
    this.reward = reward;
    this.count = count;
  }

  /** @return the ID of the achievement */
  public long getID() {
    return this.id;
  }

  /** @return the reward for the achievement */
  public int getReward() {
    return reward;
  }

  /** @return the name of the achievement */
  public String getName() {
    return name;
  }

  /** @return the number of time the achievement has been achieved */
  public int getCount() {
    return count;
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
          conc.prepareStatement(
              "SELECT *, COUNT("
                  + ACHIEVED_TABLE
                  + "."
                  + ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + ") AS "
                  + COUNT_FIELD
                  + " FROM "
                  + TABLE
                  + ", "
                  + ACHIEVED_TABLE
                  + " WHERE "
                  + TABLE
                  + "."
                  + ID_FIELD
                  + " = ? AND "
                  + ACHIEVED_TABLE
                  + "."
                  + ACHIEVED_ACHIEVEMENT_ID_FIELD
                  + " = "
                  + TABLE
                  + "."
                  + ID_FIELD
                  + " GROUP BY "
                  + TABLE
                  + "."
                  + ID_FIELD);
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
      int count = rs.getInt(COUNT_FIELD);
      return new Achievement(id, name, reward, count);
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
