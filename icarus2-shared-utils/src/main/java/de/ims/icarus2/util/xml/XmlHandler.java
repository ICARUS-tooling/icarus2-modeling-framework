/**
 *
 */
package de.ims.icarus2.util.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Markus
 *
 */
public class XmlHandler extends DefaultHandler {


	private final StringBuilder buffer = new StringBuilder();

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
	}

	protected String getText() {
		String text = buffer.length()==0 ? null : buffer.toString().trim();

		clearText();

		return (text==null || text.isEmpty()) ? null : text;
	}

	protected void clearText() {
		buffer.setLength(0);
	}
}
