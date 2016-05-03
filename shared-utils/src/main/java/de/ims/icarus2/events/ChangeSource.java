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

 * $Revision: 407 $
 * $Date: 2015-06-24 02:12:18 +0200 (Mi, 24 Jun 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/events/ChangeSource.java $
 *
 * $LastChangedDate: 2015-06-24 02:12:18 +0200 (Mi, 24 Jun 2015) $
 * $LastChangedRevision: 407 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.events;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import de.ims.icarus2.util.Changeable;

/**
 * @author Markus Gärtner
 * @version $Id: ChangeSource.java 407 2015-06-24 00:12:18Z mcgaerty $
 *
 */
public class ChangeSource implements Changeable {

	protected final Object source;

	public ChangeSource(Object source) {
		if (source == null)
			throw new NullPointerException("Invalid source");

		this.source = source;
	}

	public ChangeSource() {
		source = null;
	}

	protected EventListenerList listenerList = new EventListenerList();

	@Override
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}

	public Object getSource() {
		return source==null ? this : source;
	}

	public void fireStateChanged() {
		Object[] pairs = listenerList.getListenerList();

		ChangeEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == ChangeListener.class) {
				if (event == null) {
					event = new ChangeEvent(getSource());
				}

				((ChangeListener) pairs[i + 1]).stateChanged(event);
			}
		}
	}
}
