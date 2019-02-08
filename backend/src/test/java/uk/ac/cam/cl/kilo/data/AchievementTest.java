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

import static com.google.common.truth.Truth.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * AchievementTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class AchievementTest {
  @Mock private DataSource source;
  @Mock private Connection conc;
  @Mock private PreparedStatement stmt;
  @Mock private ResultSet rs;

  @Before
  public void setup() throws Exception {
    Database.configure(source);
    when(source.getConnection()).thenReturn(conc);
    when(conc.prepareStatement(any(String.class))).thenReturn(stmt);
    when(stmt.getGeneratedKeys()).thenReturn(rs);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getLong(1)).thenReturn(1L);
    when(rs.getLong(any(String.class))).thenReturn(1L);
    when(rs.getInt(any(String.class))).thenReturn(100);
    when(rs.getString(any(String.class))).thenReturn("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_withNullName() throws Exception {
    new Achievement(null, 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_withEmptyName() throws Exception {
    new Achievement("", 100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_withNegativeReward() throws Exception {
    new Achievement(null, -1);
  }

  @Test
  public void contentGroup_created_withValidNameAndReward() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    assertThat(achievement.getName()).isEqualTo("test");
    assertThat(achievement.getReward()).isEqualTo(100);
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_renamedWithNullName() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_renamedWithEmptyName() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setName("");
  }

  @Test
  public void achievement_renamed_renamedWithValidName() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(achievement.getName()).isEqualTo("other");
  }

  @Test
  public void achievement_notRenamed_renamedWithCurrentName() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setName("test");

    verify(stmt, times(1)).executeUpdate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void achievement_throwsException_updatedWithNegativeReward() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setReward(-1);
  }

  @Test
  public void achievement_updated_updatedWithValidReward() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setReward(200);
    achievement.setReward(300);
    achievement.setReward(400);

    verify(stmt, times(4)).executeUpdate();
    assertThat(achievement.getReward()).isEqualTo(400);
  }

  @Test
  public void achievement_notUpdate_updateWithCurrentReward() throws Exception {
    Achievement achievement = new Achievement("test", 100);

    achievement.setReward(100);
    achievement.setReward(200);
    achievement.setReward(200);

    verify(stmt, times(2)).executeUpdate();
    assertThat(achievement.getReward()).isEqualTo(200);
  }

  @Test
  public void achievement_constructed_gotByID() throws Exception {
    Achievement achievement = Achievement.getByID(1L);

    assertThat(achievement.getID()).isEqualTo(1L);
    assertThat(achievement.getName()).isEqualTo("test");
    assertThat(achievement.getReward()).isEqualTo(100);
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
