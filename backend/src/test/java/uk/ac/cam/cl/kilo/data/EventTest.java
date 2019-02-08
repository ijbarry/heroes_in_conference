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
import java.sql.Timestamp;
import java.time.Instant;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * EventTest.java
 *
 * @author Nathan Corbyn
 */
@RunWith(MockitoJUnitRunner.class)
public class EventTest {
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
    when(rs.getLong(any(String.class))).thenReturn(1L);
    when(rs.getInt(any(String.class))).thenReturn(100);
    when(rs.getString(Event.NAME_FIELD)).thenReturn("test");
    when(rs.getString(Event.DESC_FIELD)).thenReturn("example");
    when(rs.getTimestamp(Event.START_FIELD)).thenReturn(Timestamp.from(Instant.ofEpochSecond(100)));
    when(rs.getTimestamp(Event.END_FIELD)).thenReturn(Timestamp.from(Instant.ofEpochSecond(200)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_withNullName() throws Exception {
    new Event(null, "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_withEmptyName() throws Exception {
    new Event("", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_withNullStart() throws Exception {
    new Event("test", "example", null, Instant.ofEpochSecond(200));
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_withNullEnd() throws Exception {
    new Event("test", "example", Instant.ofEpochSecond(100), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_withEndBeforeStart() throws Exception {
    new Event("test", "example", Instant.ofEpochSecond(200), Instant.ofEpochSecond(100));
  }

  @Test
  public void event_created_withValidArguments() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    assertThat(event.getName()).isEqualTo("test");
    assertThat(event.getDescription()).isEqualTo("example");
    assertThat(event.getStart()).isEqualTo(Instant.ofEpochSecond(100));
    assertThat(event.getEnd()).isEqualTo(Instant.ofEpochSecond(200));
  }

  @Test
  public void event_createdWithEmptyDescription_withNullDescription() throws Exception {
    Event event = new Event("test", null, Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    assertThat(event.getDescription()).isEqualTo("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_renamedWithNullName() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setName(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_renamedWithEmptyName() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setName("");
  }

  @Test
  public void event_renamed_renamedWithValidName() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setName("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(event.getName()).isEqualTo("other");
  }

  @Test
  public void event_notRenamed_renamedWithCurrentName() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setName("test");
    event.setName("test");
    event.setName("other");
    event.setName("other");

    verify(stmt, times(2)).executeUpdate();
  }

  @Test
  public void event_updatedWithEmptyDescription_updatedWithNullDescription() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setDescription(null);

    assertThat(event.getDescription()).isEqualTo("");
  }

  @Test
  public void event_notUpdate_updatedWithCurrentDescription() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setDescription("example");
    event.setDescription("other");
    event.setDescription("other");

    verify(stmt, times(2)).executeUpdate();
    assertThat(event.getDescription()).isEqualTo("other");
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_updatedWithNullStart() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setTiming(null, Instant.ofEpochSecond(200));
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_updatedWithNullEnd() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setTiming(Instant.ofEpochSecond(100), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void event_throwsException_updatedWithEndBeforeStart() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setTiming(Instant.ofEpochSecond(200), Instant.ofEpochSecond(100));
  }

  @Test
  public void event_updated_updatedWithValidTiming() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setTiming(Instant.ofEpochSecond(200), Instant.ofEpochSecond(300));

    verify(stmt, times(2)).executeUpdate();
    assertThat(event.getStart()).isEqualTo(Instant.ofEpochSecond(200));
    assertThat(event.getEnd()).isEqualTo(Instant.ofEpochSecond(300));
  }

  @Test
  public void event_notUpdated_updatedWithCurrentTiming() throws Exception {
    Event event =
        new Event("test", "example", Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    event.setTiming(Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));
    event.setTiming(Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));
    event.setTiming(Instant.ofEpochSecond(200), Instant.ofEpochSecond(300));
    event.setTiming(Instant.ofEpochSecond(200), Instant.ofEpochSecond(300));

    verify(stmt, times(2)).executeUpdate();
    assertThat(event.getStart()).isEqualTo(Instant.ofEpochSecond(200));
    assertThat(event.getEnd()).isEqualTo(Instant.ofEpochSecond(300));
  }

  @Test
  public void event_constructed_gotByID() throws Exception {
    Event event = Event.getByID(1L);

    assertThat(event.getID()).isEqualTo(1L);
    assertThat(event.getName()).isEqualTo("test");
    assertThat(event.getDescription()).isEqualTo("example");
    assertThat(event.getStart()).isEqualTo(Instant.ofEpochSecond(100));
    assertThat(event.getEnd()).isEqualTo(Instant.ofEpochSecond(200));
  }

  @After
  public void cleanup() throws Exception {
    Field instance = Database.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }
}
