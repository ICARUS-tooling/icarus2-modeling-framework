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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;

/**
 * Utility class with methods to convert between images/icons and a Base64 string representation.
 *
 * @author Markus Gärtner
 *
 */
public class ImageSerializer {

	private static final Logger log = LoggerFactory.getLogger(ImageSerializer.class);

	private ImageSerializer() {
		throw new IcarusRuntimeException(GlobalErrorCode.UNSUPPORTED_OPERATION, "Instantiation not supported");
	}

	/**
	 * Helper component used when painting an icon onto
	 * a temporary {@link BufferedImage}.
	 *
	 * @see Icon#paintIcon(java.awt.Component, Graphics, int, int)
	 */
	private static final Canvas DUMMY_COMPONENT = new Canvas();

	private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
	private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

	private static BufferedImage createBasicImage(Icon icon) {
		return new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
	}

	private static void paintIconOnImage(BufferedImage image, Icon icon) {
		Graphics g = image.createGraphics();

		synchronized (DUMMY_COMPONENT) {
			DUMMY_COMPONENT.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
			icon.paintIcon(DUMMY_COMPONENT, g, 0, 0);
		}

		g.dispose();
	}

	private static void paintImage(Image source, BufferedImage target) {
		Graphics g = target.createGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
	}

	public static BufferedImage icon2Image(Icon icon) {
		if(icon instanceof ImageIcon) {
			Image image = ((ImageIcon)icon).getImage();

			if(image instanceof BufferedImage) {
				return (BufferedImage) image;
			}

			BufferedImage bimg = createBasicImage(icon);
			paintImage(image, bimg);
			return bimg;
		}

		BufferedImage image = createBasicImage(icon);
		paintIconOnImage(image, icon);
		return image;
	}

	public static String icon2String(Icon icon) {
		if(icon==null) {
			return null;
		}

		BufferedImage image = icon2Image(icon);

		return image2String(image);
	}

	public static String image2String(BufferedImage image) {
		String result = null;

		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(image, "PNG", out);
			result = BASE64_ENCODER.encodeToString(out.toByteArray());
		} catch (IOException e) {
			log.error("Unexpected internal error while converting image to Base64 string", e);
		}

		return result;
	}

	public static Image string2Image(String s) {
		if(s==null || s.isEmpty()) {
			return null;
		}

		Image result = null;

		try(InputStream in = new ByteArrayInputStream(BASE64_DECODER.decode(s))) {
			result = ImageIO.read(in);
		} catch (IOException e) {
			log.error("Unexpected internal error while converting Base64 string to image", e);
		}

		return result;
	}

	public static Icon string2Icon(String s) {
		Image image = string2Image(s);

		Icon result = null;

		if(image!=null) {
			result = new ImageIcon(image);
		}

		return result;
	}
}
