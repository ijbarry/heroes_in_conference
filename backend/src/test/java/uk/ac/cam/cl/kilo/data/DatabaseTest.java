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
 * DatabaseTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {
  @Mock private DataSource ds;
  @Mock private Connection conc;
  @Mock private PreparedStatement stmt;
  @Mock private ResultSet rs;

  @Before
  public void setUp() throws Exception {
    when(ds.getConnection()).thenReturn(conc);
    when(conc.prepareStatement(any(String.class))).thenReturn(stmt);
    when(stmt.executeQuery()).thenReturn(rs);
  }

  @Test(expected = RuntimeException.class)
  public void database_throwsException_notConfigured() {
    Database.getInstance();
  }

  @Test(expected = RuntimeException.class)
  public void database_throwsException_configuredWithNullSource() {
    Database.configure(null);
  }

  @Test
  public void database_notNull_configuredCorrectly() {
    Database.configure(ds);

    Database database = Database.getInstance();

    assertThat(database).isNotNull();
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
