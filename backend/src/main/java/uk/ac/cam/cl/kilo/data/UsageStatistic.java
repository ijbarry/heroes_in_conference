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
 * UsageStatistic.java
 *
 * @author Nathan Corbyn
 */
public class UsageStatistic {
  public static final String TABLE = "usagedata", TIME_FIELD = "taken", USAGE_FIELD = "requests";

  private Instant time;
  private int requestCount;

  /**
   * Create a new usage statistic for the given time with the given request count.
   *
   * @param time the time the measurement was taken
   * @param requestCount the number of requests made in the period up to the measurement
   * @throws IllegalArgumentException if the time is null or the request count is negative
   * @throws DatabaseException if the usage statistic could not be added to the database
   */
  public UsageStatistic(Instant time, int requestCount) throws DatabaseException {
    if (time == null) throw new IllegalArgumentException("The time must not be null");
    if (requestCount < 0)
      throw new IllegalArgumentException("The request count must be non-negative");
    this.time = time;
    this.requestCount = requestCount;
    try (Connection conc = Database.getInstance().getConnection()) {
      PreparedStatement stmt =
          conc.prepareStatement(
              "INSERT INTO " + TABLE + "(" + TIME_FIELD + ", " + USAGE_FIELD + ") VALUE (?, ?)");
      stmt.setTimestamp(1, Timestamp.from(time));
      stmt.setInt(2, requestCount);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }

  private UsageStatistic() {}

  /** @return the time at with this measurement was taken */
  public Instant getTime() {
    return time;
  }

  /** @return the number of requests made between this statistic and the last */
  public int getRequestCount() {
    return requestCount;
  }

  /**
   * Construct a usage statistic from a {@link java.sql#ResultSet}.
   *
   * @param rs the {@link java.sql#ResultSet} to construct from
   * @return the constructed usage statistic
   * @throws DatabaseException if the usage statistic could not be constructed
   */
  static UsageStatistic from(ResultSet rs) throws DatabaseException {
    assert (rs != null);
    try {
      UsageStatistic usage = new UsageStatistic();
      usage.time = rs.getTimestamp(TIME_FIELD).toInstant();
      usage.requestCount = rs.getInt(USAGE_FIELD);
      return usage;
    } catch (SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
