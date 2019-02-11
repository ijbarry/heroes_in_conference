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
 * ContentGroupTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentGroupTest {
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
    when(rs.getBoolean(any(String.class))).thenReturn(true);
    when(rs.getString(any(String.class))).thenReturn("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void contentGroup_throwsException_withNullName() throws Exception {
    new ContentGroup(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void contentGroup_throwsException_withEmptyName() throws Exception {
    new ContentGroup("");
  }

  @Test
  public void contentGroup_created_withValidName() throws Exception {
    ContentGroup group = new ContentGroup("test");

    assertThat(group.getName()).isEqualTo("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void contentGroup_throwsException_renamedWithNullName() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void contentGroup_throwsException_renamedWithEmptyName() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setName("");
  }

  @Test
  public void contentGroup_renamed_renamedWithValidName() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(group.getName()).isEqualTo("other");
  }

  @Test
  public void contentGroup_notRenamed_renamedWithCurrentName() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setName("test");

    verify(stmt, times(1)).executeUpdate();
  }

  @Test
  public void contentGroup_notUpdated_statusNotChanged() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setEnabled(true);
    group.setEnabled(false);
    group.setEnabled(false);

    verify(stmt, times(2)).executeUpdate();
    assertThat(group.isEnabled()).isFalse();
  }

  @Test
  public void contentGroup_updated_statusChanged() throws Exception {
    ContentGroup group = new ContentGroup("test");

    group.setEnabled(false);
    group.setEnabled(true);
    group.setEnabled(false);

    verify(stmt, times(4)).executeUpdate();
    assertThat(group.isEnabled()).isFalse();
  }

  @Test
  public void contentGroup_constructed_gotByID() throws Exception {
    ContentGroup group = ContentGroup.getByID(1L);

    assertThat(group.getID()).isEqualTo(1L);
    assertThat(group.getName()).isEqualTo("test");
    assertThat(group.isEnabled()).isTrue();
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
