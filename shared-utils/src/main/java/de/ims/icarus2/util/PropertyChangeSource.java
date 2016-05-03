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
 * $Revision: 380 $
 * $Date: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.core/source/de/ims/icarus2/util/PropertyChangeSource.java $
 *
 * $LastChangedDate: 2015-04-02 03:28:48 +0200 (Do, 02 Apr 2015) $ 
 * $LastChangedRevision: 380 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: PropertyChangeSource.java 380 2015-04-02 01:28:48Z mcgaerty $
 *
 */
public class PropertyChangeSource {

	protected PropertyChangeSupport changeSupport;
	
	protected PropertyChangeSource() {
		changeSupport = new PropertyChangeSupport(this);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, byte oldValue,
			byte newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, short oldValue,
			short newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, long oldValue,
			long newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, float oldValue,
			float newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, double oldValue,
			double newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}
