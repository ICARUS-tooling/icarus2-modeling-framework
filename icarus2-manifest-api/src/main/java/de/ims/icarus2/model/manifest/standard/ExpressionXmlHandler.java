/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
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
 */
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
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
 *
 */
public class ExpressionXmlHandler implements ManifestXmlHandler {

	private final ExpressionFactory factory;

	public ExpressionXmlHandler(ExpressionFactory factory) {
		requireNonNull(factory);

		this.factory = factory;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case ManifestXmlTags.EVAL: {
			// no-op
		} break;

		case ManifestXmlTags.CODE: {
			// no-op
		} break;

		case ManifestXmlTags.VARIABLE: {
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME);
			String classname = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CLASS);
			String pluginId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PLUGIN_ID);

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


			factory.addInputVariable(name, namespace);
		} break;

		default:
			throw new UnexpectedTagException(qName, true, ManifestXmlTags.EVAL);
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
		case ManifestXmlTags.EVAL: {
			return null;
		}

		case ManifestXmlTags.CODE: {
			factory.setCode(text);
		} break;

		case ManifestXmlTags.VARIABLE: {
			// no-op
		} break;

		default:
			throw new UnexpectedTagException(qName, false, ManifestXmlTags.EVAL);
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
		throw new UnsupportedNestingException(qName, ManifestXmlTags.EVAL);
	}

	public Expression createExpression() {
		try {
			/*
			 *  Fail fast policy:
			 *
			 *  We directly let the factory compile an expression instead of
			 *  doing it lazily and risking delayed problems.
			 */
			return factory.compile();
		} catch (Exception e) {
			throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_ERROR, "Failed to compile custom expression", e);
		}
	}
}
