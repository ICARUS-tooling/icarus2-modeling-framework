/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.standard.CorpusManifestImpl.NoteImpl;
import de.ims.icarus2.model.manifest.xml.ManifestXmlAttributes;
import de.ims.icarus2.model.manifest.xml.ManifestXmlHandler;
import de.ims.icarus2.model.manifest.xml.ManifestXmlTags;
import de.ims.icarus2.model.manifest.xml.ManifestXmlUtils;
import de.ims.icarus2.util.date.DateUtils;
import de.ims.icarus2.util.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusManifestXmlDelegate extends AbstractMemberManifestXmlDelegate<CorpusManifest> {

	private NoteImpl note;
	private ContextManifestXmlDelegate contextManifestXmlDelegate;

	private ContextManifestXmlDelegate getContextManifestXmlDelegate() {
		if(contextManifestXmlDelegate==null) {
			contextManifestXmlDelegate = new ContextManifestXmlDelegate();
		}
		return contextManifestXmlDelegate;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.delegates.AbstractMemberManifestXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		super.reset();

		note = null;

		if(contextManifestXmlDelegate!=null) {
			contextManifestXmlDelegate.reset();
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#writeAttributes(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws XMLStreamException {
		super.writeAttributes(serializer);

		CorpusManifest manifest = getInstance();

		// Write editable flag
		if(manifest.isEditable()!=CorpusManifest.DEFAULT_EDITABLE_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.EDITABLE, manifest.isEditable());
		}

		// Write parallel flag
		if(manifest.isParallel()!=CorpusManifest.DEFAULT_PARALLEL_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.PARALLEL, manifest.isParallel());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws XMLStreamException {
		super.writeElements(serializer);

		CorpusManifest manifest = getInstance();

		// Write notes
		List<Note> notes = manifest.getNotes();
		if(!notes.isEmpty()) {
			serializer.startElement(ManifestXmlTags.NOTES);
			for(Iterator<Note> it = notes.iterator(); it.hasNext();) {
				ManifestXmlUtils.writeNoteElement(serializer, it.next());
				if(it.hasNext()) {
					serializer.writeLineBreak();
				}
			}
			serializer.endElement(ManifestXmlTags.NOTES);
		}

		// Write contained root context manifests
		for(Iterator<ContextManifest> it = manifest.getRootContextManifests().iterator(); it.hasNext();) {
			ContextManifest contextManifest = it.next();
			getContextManifestXmlDelegate().reset(contextManifest);
			getContextManifestXmlDelegate().root(true).writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		// Write contained custom context manifests
		for(Iterator<ContextManifest> it = manifest.getCustomContextManifests().iterator(); it.hasNext();) {
			ContextManifest contextManifest = it.next();
			getContextManifestXmlDelegate().reset(contextManifest).writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		CorpusManifest manifest = getInstance();

		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.EDITABLE)
			.ifPresent(manifest::setEditable);
		ManifestXmlUtils.booleanValue(attributes, ManifestXmlAttributes.PARALLEL)
			.ifPresent(manifest::setParallel);
	}

	@Override
	public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.CORPUS: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.ROOT_CONTEXT: {
			handler = getContextManifestXmlDelegate().reset(getInstance()).root(true);
		} break;

		case ManifestXmlTags.CONTEXT: {
			handler = getContextManifestXmlDelegate().reset(getInstance()).root(false);
		} break;

		case ManifestXmlTags.NOTES: {
			// no-op
		} break;

		case ManifestXmlTags.NOTE: {
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME)
					.orElseThrow(ManifestXmlHandler.error("Missing name for note"));
			note = new NoteImpl(name);
			String date = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.DATE)
					.orElseThrow(ManifestXmlHandler.error("Missing date for note"));
			try {
				note.setModificationDate(DateUtils.parseDate(date));
			} catch (ParseException e) {
				throw new SAXException("Invalid modification date string: "+date, e); //$NON-NLS-1$
			}
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return Optional.ofNullable(handler);
	}

	@Override
	public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		ManifestXmlHandler handler = this;

		switch (localName) {
		case ManifestXmlTags.CORPUS: {
			handler = null;
		} break;

		case ManifestXmlTags.NOTES: {
			// no-op
		} break;

		case ManifestXmlTags.NOTE: {
			note.changeContent(text);
			getInstance().addNote(note);
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return Optional.ofNullable(handler);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ManifestXmlHandler handler)
			throws SAXException {
		switch (localName) {

		case ManifestXmlTags.ROOT_CONTEXT: {
			getInstance().addRootContextManifest(((ContextManifestXmlDelegate) handler).getInstance());
		} break;

		case ManifestXmlTags.CONTEXT: {
			getInstance().addCustomContextManifest(((ContextManifestXmlDelegate) handler).getInstance());
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return ManifestXmlTags.CORPUS;
	}
}
