/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.util.icon;

import static java.util.Objects.requireNonNull;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import de.ims.icarus2.util.strings.NamedObject;
import de.ims.icarus2.util.strings.StringResource;

public class IconWrapper implements Icon, StringResource, NamedObject {
	private transient Icon source;
	private final String name;

	public IconWrapper(String name) {
		this.name = requireNonNull(name);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof IconWrapper) {
			return name.equals(((IconWrapper)obj).name);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IconWrapper@"+name; //$NON-NLS-1$
	}

	/**
	 *
	 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return name;
	}

	/**
	 * @see de.ims.icarus2.util.strings.NamedObject#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	private Icon getSource() {
		if(source==null) {
			source = IconRegistry.getGlobalRegistry().getIcon(name);
		}
		return source;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(getSource()!=null) {
			source.paintIcon(c, g, x, y);
		}
	}

	/**
	 * @see javax.swing.Icon#getIconWidth()
	 */
	@Override
	public int getIconWidth() {
		return getSource()==null ? 0 : source.getIconWidth();
	}

	/**
	 * @see javax.swing.Icon#getIconHeight()
	 */
	@Override
	public int getIconHeight() {
		return getSource()==null ? 0 : source.getIconHeight();
	}
}