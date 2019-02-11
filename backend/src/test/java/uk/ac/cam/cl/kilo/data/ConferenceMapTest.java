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
import java.nio.file.Path;
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
 * ConferenceMapTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class ConferenceMapTest {
  @Mock private DataSource source;
  @Mock private Connection conc;
  @Mock private PreparedStatement stmt;
  @Mock private ResultSet rs;
  @Mock private Path image, other;

  @Before
  public void setup() throws Exception {
    Database.configure(source);
    when(source.getConnection()).thenReturn(conc);
    when(conc.prepareStatement(any(String.class))).thenReturn(stmt);
    when(stmt.getGeneratedKeys()).thenReturn(rs);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getLong(any(String.class))).thenReturn(1L);
    when(rs.getString(any(String.class))).thenReturn("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_withNullName() throws Exception {
    new ConferenceMap(null, image);
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_withEmptyName() throws Exception {
    new ConferenceMap("", image);
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_withNullImage() throws Exception {
    new ConferenceMap("test", null);
  }

  @Test
  public void conferenceMap_created_withValidArguments() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    assertThat(map.getName()).isEqualTo("test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_renamedWithNullName() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_renamedWithEmptyName() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setName("");
  }

  @Test
  public void conferenceMap_renamed_renamedWithValidName() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(map.getName()).isEqualTo("other");
  }

  @Test
  public void conferenceMap_notRenamed_renamedWithCurrentName() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setName("test");
    map.setName("test");
    map.setName("other");
    map.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(map.getName()).isEqualTo("other");
  }

  @Test(expected = IllegalArgumentException.class)
  public void conferenceMap_throwsException_updatedWithNullImage() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setImage(null);
  }

  @Test
  public void conferenceMap_notUpdated_updatedWithCurrentImage() throws Exception {
    ConferenceMap map = new ConferenceMap("test", image);

    map.setImage(image);
    map.setImage(image);
    map.setImage(other);
    map.setImage(other);

    verify(stmt, times(2)).executeUpdate();
  }

  @Test
  public void conferenceMap_constructed_gotByID() throws Exception {
    ConferenceMap map = ConferenceMap.getByID(1L);

    assertThat(map.getID()).isEqualTo(1L);
    assertThat(map.getName()).isEqualTo("test");
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
