/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Models a collection of icons based on a single base icon
 * and resized versions of it.
 *
 * @author Markus Gärtner
 * @param <I> type of the raw base icon
 */
public class IconSet<I extends Icon> {

	/**
	 * Buffer for resized instances
	 */
	private Map<Resolution, Icon> resizedIcons;

	private final I rawIcon;

	public IconSet(I rawIcon) {
		this.rawIcon = requireNonNull(rawIcon);
	}

	public I getRawIcon() {
		return rawIcon;
	}

	public Icon getIcon(Resolution resolution) {
		requireNonNull(resolution);

		// In case we do not need any resizing simply return the raw icon
		if(rawIcon.getIconWidth()==resolution.getWidth()
				&& rawIcon.getIconHeight()==resolution.getHeight()) {
			return rawIcon;
		}

		Icon resizedIcon = lookupResizedIcon(resolution);
		if(resizedIcon==null) {
			resizedIcon = resize(resolution);

			addResizedIcon(resolution, resizedIcon);
		}

		return resizedIcon;
	}

	private synchronized Icon lookupResizedIcon(Resolution resolution) {
		if(resizedIcons==null) {
			resizedIcons = new HashMap<>();
		}

		return resizedIcons.get(resolution);
	}

	private synchronized void addResizedIcon(Resolution resolution, Icon icon) {
		if(resizedIcons==null) {
			resizedIcons = new HashMap<>();
		}

		resizedIcons.put(resolution, icon);
	}

	/**
	 * Returns the raw icon of this set as an {@link Image}.
	 * If the icon is not derived from {@code Image} class or
	 * {@link ImageIcon}, then this method will create a temporary
	 * {@link BufferedImage} and use it to paint the icon once
	 * before returning the image.
	 *
	 * @return
	 */
	private Image getRawImage() {
		if(rawIcon instanceof Image) {
			return (Image) rawIcon;
		} else if(rawIcon instanceof ImageIcon) {
			return ((ImageIcon)rawIcon).getImage();
		} else {
			// Use a utility image that we can draw the icon on
			BufferedImage tmp = new BufferedImage(
					rawIcon.getIconWidth(), rawIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

			Graphics2D g = tmp.createGraphics();
			rawIcon.paintIcon(null, g, 0, 0);
			g.dispose();

			return tmp;
		}
	}

	private String getDescription() {
		return (rawIcon instanceof ImageIcon) ?
				((ImageIcon)rawIcon).getDescription() : null;
	}

	/**
	 * Creates a resized version of this set's raw icon.
	 *
	 * @param resolution
	 * @return
	 */
	private Icon resize(Resolution resolution) {
		Image rawImage = getRawImage();

		Image resizedImage = rawImage.getScaledInstance(
				resolution.getWidth(), resolution.getHeight(), Image.SCALE_SMOOTH);

		return new ImageIcon(resizedImage, getDescription());
	}

	/**
	 * Overwrites the cache entry for a certain resolution with the given
	 * icon. The icon's size is taken as the resolution of the cache entry.
	 *
	 * @param icon
	 */
	void add(Icon icon) {
		Resolution resolution = Resolution.forSize(icon.getIconWidth(), icon.getIconHeight());
		addResizedIcon(resolution, icon);
	}

	public void dispose() {
		Map<Resolution, Icon> cache = resizedIcons;

		if(cache!=null) {
			cache.clear();
		}

		resizedIcons = null;
	}
}
