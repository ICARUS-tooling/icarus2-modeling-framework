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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/manifest/ExpressionXmlHandler.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.standard;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.ExpressionFactory;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;

/**
 * @author Markus Gärtner
 * @version $Id: ExpressionXmlHandler.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
public class ExpressionXmlHandler implements ManifestXmlHandler, ManifestXmlAttributes, ManifestXmlTags {

	private final ExpressionFactory factory;

	public ExpressionXmlHandler(ExpressionFactory factory) {
		if (factory == null)
			throw new NullPointerException("Invalid factory"); //$NON-NLS-1$

		this.factory = factory;
	}

	/**
	 * Default constructor. Uses the basic {@link ExpressionFactory} implementation
	 * as factory to build the expression object.
	 */
	public ExpressionXmlHandler() {

		this.factory = new ExpressionFactory();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_EVAL: {
			// no-op
		} break;

		case TAG_CODE: {
			// no-op
		} break;

		case TAG_VARIABLE: {
			String name = ManifestXmlUtils.normalize(attributes, ATTR_NAME);
			String classname = ManifestXmlUtils.normalize(attributes, ATTR_CLASS);
			String pluginId = ManifestXmlUtils.normalize(attributes, ATTR_PLUGIN_ID);

			ClassLoader classLoader = getClass().getClassLoader();

			//FIXME introduce workaround to get plugin id information to the variable!!!
//			if(pluginId!=null) {
//				classLoader = PluginUtil.getClassLoader(pluginId);
//			}

			Class<?> namespace;
			try {
				namespace = classLoader.loadClass(classname);
			} catch (ClassNotFoundException e) {
				throw new SAXException("Unable to load namespace class for variable: "+name, e); //$NON-NLS-1$
			}


			factory.addVariable(name, namespace);
		} break;

		default:
			throw new UnexpectedTagException(qName, true, TAG_EVAL);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_EVAL: {
			return null;
		}

		case TAG_CODE: {
			factory.setCode(text);
		} break;

		case TAG_VARIABLE: {
			// no-op
		} break;

		default:
			throw new UnexpectedTagException(qName, false, TAG_EVAL);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		throw new UnsupportedNestingException(qName, TAG_EVAL);
	}

	public Expression createExpression() {
		return factory.build();
	}
}
