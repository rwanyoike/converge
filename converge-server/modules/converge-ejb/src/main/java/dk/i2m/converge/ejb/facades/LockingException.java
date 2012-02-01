/*
 *  Copyright (C) 2010 alc
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.i2m.converge.ejb.facades;

/**
 * Exception thrown when an error occurs related to the locking of an entity.
 *
 * @author Allan Lykke Christensen
 */
public class LockingException extends Exception {

    public LockingException(Throwable cause) {
        super(cause);
    }

    public LockingException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockingException(String message) {
        super(message);
    }

    public LockingException() {
        super();
    }
}
