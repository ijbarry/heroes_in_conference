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

/**
 * DatabaseException.java
 *
 * @author Nathan Corbyn
 */
public class DatabaseException extends Exception {
  private static final long serialVersionUID = 1L;

  public DatabaseException(Exception e) {
    super(e);
  }

  public DatabaseException(String msg) {
    super(msg);
  }
}
