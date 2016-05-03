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

 * $Revision: 422 $
 * $Date: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/xml/XmlSerializer.java $
 *
 * $LastChangedDate: 2015-08-19 15:38:58 +0200 (Mi, 19 Aug 2015) $
 * $LastChangedRevision: 422 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.util.xml;



/**
 * @author Markus Gärtner
 * @version $Id: XmlSerializer.java 422 2015-08-19 13:38:58Z mcgaerty $
 *
 */
public interface XmlSerializer {

	/**
	 * Begins a document definition by writing meta data
	 * like xml version and implementation specific info.
	 */
	void startDocument() throws Exception;

	/**
	 * Starts a new element using the given {@code name} as tag.
	 * Note that this method will assume a non-empty element to be created
	 * and that there be at least one child element!
	 */
	void startElement(String name) throws Exception;

	/**
	 * Starts a new empty element using the given {@code name} as tag.
	 * An empty element is not allowed to contain nested elements,
	 * therefore only attributes can be written before the matching
	 * {@link #endElement(String)} call!
	 */
	void startEmptyElement(String name) throws Exception;

	/**
	 * Writes a new attribute, using the given {@code name} and
	 * {@code value}. If the {@code value} is {@code null}, this method
	 * does nothing.
	 */
	void writeAttribute(String name, String value) throws Exception;

	// PRIMITIVE ATTRIBUTES
	void writeAttribute(String name, int value) throws Exception;
	void writeAttribute(String name, long value) throws Exception;
	void writeAttribute(String name, float value) throws Exception;
	void writeAttribute(String name, double value) throws Exception;
	void writeAttribute(String name, boolean value) throws Exception;

	/**
	 * Closes the element with the given {@code name}. Note that the
	 * behavior of this method depends on whether or not the element
	 * has been started with either the {@link #startElement(String)}
	 * or {@link #startEmptyElement(String)} method!
	 */
	void endElement(String name) throws Exception;

	/**
	 * Writes the given {@code text} as content of the current element,
	 * doing nothing in case the {@code text} argument is {@code null}.
	 */
	void writeText(CharSequence text) throws Exception;

	/**
	 * Writes the given {@code text} as encoded character data in the
	 * content section of the current element. Note that unlike the
	 * {@link #writeText(CharSequence)} method this time the {@code text} argument
	 * is not allowed to be {@code null}!
	 *
	 * @throws Exception in case the given {@code text} is {@code null}
	 * @see #writeText(CharSequence)
	 */
	void writeCData(CharSequence text) throws Exception;

	/**
	 * Writes a line-break. It is implementation specific whether the characters written
	 * will be platform specific.
	 */
	void writeLineBreak() throws Exception;

	/**
	 * Finishes the document. It is implementation dependent whether pending
	 * opening tags will automatically be closed.
	 */
	void endDocument() throws Exception;

	/**
	 * Releases all previously acquired resources and closes underlying output streams.
	 */
	void close() throws Exception;
}
