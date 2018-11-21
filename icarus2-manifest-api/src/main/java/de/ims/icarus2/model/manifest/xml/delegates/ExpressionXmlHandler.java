/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.xml.delegates;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.eval.Expression;
import de.ims.icarus2.util.eval.ExpressionFactory;
import de.ims.icarus2.util.eval.spi.ExpressionFactoryProvider;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.UnsupportedNestingException;

/**
 * @author Markus Gärtner
 *
 */
public class ExpressionXmlHandler implements ManifestXmlHandler {

	private final Class<?> returnType;

	private ExpressionFactory factory;

	public ExpressionXmlHandler(Class<?> returnType) {
		this.returnType = requireNonNull(returnType);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case ManifestXmlTags.EVAL: {
			String type = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.TYPE)
					.orElse(ExpressionFactoryProvider.GENERIC_JAVA_TYPE);

			// Instantiate fresh factory, this might throw an unchecked exception
			try {
				factory = ExpressionFactoryProvider.newFactory(type);
			} catch (IllegalArgumentException e) {
				//TODO is it really a good idea to catch unchecked exception here?
				throw new SAXException("Not a valid expression type: "+type, e);
			}

			factory.setReturnType(returnType);
		} break;

		case ManifestXmlTags.CODE: {
			// no-op
		} break;

		case ManifestXmlTags.VARIABLE: {
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME)
					.orElseThrow(ManifestXmlHandler.error("Missing variable name"));
			String classname = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.CLASS)
					.orElseThrow(ManifestXmlHandler.error("Missing variable class name"));
			String pluginId = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PLUGIN_ID)
					.orElse(null);

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

		return Optional.of(this);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
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

		return Optional.of(this);
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
			throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_ERROR,
					"Failed to compile custom expression", e);
		}
	}
}
