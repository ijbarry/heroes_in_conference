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
 * MapMarkerTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class MapMarkerTest {
  @Mock private DataSource source;
  @Mock private Connection conc;
  @Mock private PreparedStatement stmt;
  @Mock private ResultSet rs;
  @Mock private ConferenceMap map;

  @Before
  public void setup() throws Exception {
    Database.configure(source);
    when(source.getConnection()).thenReturn(conc);
    when(conc.prepareStatement(any(String.class))).thenReturn(stmt);
    when(stmt.getGeneratedKeys()).thenReturn(rs);
    when(stmt.executeQuery()).thenReturn(rs);
    when(rs.first()).thenReturn(true);
    when(rs.getLong(any(String.class))).thenReturn(1L);
    when(rs.getString(MapMarker.NAME_FIELD)).thenReturn("test");
    when(rs.getString(MapMarker.DESC_FIELD)).thenReturn("example");
    when(rs.getLong(MapMarker.MAP_FIELD)).thenReturn(1L);
    when(rs.getInt(MapMarker.X_FIELD)).thenReturn(10);
    when(rs.getInt(MapMarker.Y_FIELD)).thenReturn(20);
    when(map.getID()).thenReturn(1L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapMarker_throwsException_withNullMap() throws Exception {
    new MapMarker(null, "test", "example", 10, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapMarker_throwsException_withNullName() throws Exception {
    new MapMarker(map, null, "example", 10, 20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapMarker_throwsException_withEmptyName() throws Exception {
    new MapMarker(map, "", "example", 10, 20);
  }

  @Test
  public void mapMarker_created_withValidArguments() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    assertThat(marker.getName()).isEqualTo("test");
    assertThat(marker.getDescription()).isEqualTo("example");
    assertThat(marker.getX()).isEqualTo(10);
    assertThat(marker.getY()).isEqualTo(20);
  }

  @Test
  public void mapMarker_createdWithEmptyDescription_withNullDescription() throws Exception {
    MapMarker marker = new MapMarker(map, "test", null, 10, 20);

    assertThat(marker.getDescription()).isEqualTo("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapMarker_throwsException_renamedWithNullName() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void mapMarker_throwsException_renamedWithEmptyName() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setName("");
  }

  @Test
  public void mapMarker_renamed_renamedWithValidName() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getName()).isEqualTo("other");
  }

  @Test
  public void mapMarker_notRenamed_renamedWithCurrentName() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setName("test");
    marker.setName("test");
    marker.setName("other");
    marker.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getName()).isEqualTo("other");
  }

  @Test
  public void mapMarker_updatedWithEmptyDescription_updatedWithNullDescription() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setDescription(null);

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getDescription()).isEqualTo("");
  }

  @Test
  public void mapMarker_notUpdated_updatedWithCurrentDescription() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setDescription("example");
    marker.setDescription("other");
    marker.setDescription("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getDescription()).isEqualTo("other");
  }

  @Test
  public void mapMarker_updated_updatedWithNewPosition() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setPosition(20, 30);

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getX()).isEqualTo(20);
    assertThat(marker.getY()).isEqualTo(30);
  }

  @Test
  public void mapMarker_notUpdated_updatedWithCurrentPosition() throws Exception {
    MapMarker marker = new MapMarker(map, "test", "example", 10, 20);

    marker.setPosition(10, 20);
    marker.setPosition(10, 20);
    marker.setPosition(20, 30);
    marker.setPosition(20, 30);

    verify(stmt, times(2)).executeUpdate();
    assertThat(marker.getX()).isEqualTo(20);
    assertThat(marker.getY()).isEqualTo(30);
  }

  @Test
  public void mapMarker_constructed_gotByID() throws Exception {
    MapMarker marker = MapMarker.getByID(1L);

    assertThat(marker.getID()).isEqualTo(1L);
    assertThat(marker.getName()).isEqualTo("test");
    assertThat(marker.getDescription()).isEqualTo("example");
    assertThat(marker.getX()).isEqualTo(10);
    assertThat(marker.getY()).isEqualTo(20);
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
