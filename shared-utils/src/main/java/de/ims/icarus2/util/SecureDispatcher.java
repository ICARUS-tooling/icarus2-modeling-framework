/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 400 $
 * $Date: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/SecureDispatcher.java $
 *
 * $LastChangedDate: 2015-05-29 15:06:46 +0200 (Fr, 29 Mai 2015) $
 * $LastChangedRevision: 400 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util;

/**
 * Helper class to perform unsafe operations that might throw
 * exceptions on other threads and to be able to receive those
 * exceptions later on the original thread. A typical usage
 * is the delegation of task execution to the {@code EventDispatchThread},
 * to wait for execution and then handle errors.
 *
 *
 * @author Markus Gärtner
 * @version $Id: SecureDispatcher.java 400 2015-05-29 13:06:46Z mcgaerty $
 *
 */
public abstract class SecureDispatcher implements Runnable {

	private Exception exception;

	/**
	 * @param runnable
	 */
	public SecureDispatcher() {
		// no-op
	}

	/**
	 * Executes the {@link #runUnsafe()} method and catches all
	 * exceptions for later processing.
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		try {
			runUnsafe();
		} catch(Exception e) {
			exception = e;
		}
	}

	/**
	 * Wraps the execution of unsafe code.
	 * @throws Exception
	 */
	protected abstract void runUnsafe() throws Exception;

	/**
	 * Returns the {@code Exception} object that was caught during
	 * the execution of {@link #runUnsafe()} or {@code null} if the
	 * method finished without errors.
	 * @return
	 */
	public Exception getException() {
		return exception;
	}

	public boolean hasException() {
		return exception!=null;
	}
}
