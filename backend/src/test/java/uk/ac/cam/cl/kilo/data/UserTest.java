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
 * UserTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class UserTest {
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
  }

  @Test(expected = IllegalArgumentException.class)
  public void user_throwsException_withNullName() throws Exception {
    new User(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void user_throwsException_withEmptyName() throws Exception {
    new User("");
  }

  @Test
  public void user_created_withValidName() throws Exception {
    User user = new User("test");

    assertThat(user.getName()).isEqualTo("test");
  }

  @Test
  public void user_ignores_nullAchievement() throws Exception {
    User user = new User("test");

    user.achieve(null);
    user.revoke(null);

    verify(stmt, times(1)).executeUpdate();
  }

  @Test
  public void user_ignores_nullEvent() throws Exception {
    User user = new User("test");

    user.markInterestIn(null);
    user.unmarkInterestIn(null);

    verify(stmt, times(1)).executeUpdate();
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
