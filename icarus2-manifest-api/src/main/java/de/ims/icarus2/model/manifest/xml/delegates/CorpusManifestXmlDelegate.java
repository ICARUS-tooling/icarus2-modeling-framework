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
package de.ims.icarus2.model.manifest.xml.delegates;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

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
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		CorpusManifest manifest = getInstance();

		// Write editable flag
		if(manifest.isEditable()!=CorpusManifest.DEFAULT_EDITABLE_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.EDITABLE, manifest.isEditable());
		}

		// Write parallel flag
		if(manifest.isParallel()!=CorpusManifest.DEFAULT_PARALLEL_VALUE) {
			serializer.writeAttribute(ManifestXmlAttributes.EDITABLE, manifest.isParallel());
		}
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractModifiableManifest#writeElements(de.ims.icarus2.util.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
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
			getContextManifestXmlDelegate().reset(contextManifest).writeXml(serializer);

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

		String editable = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.EDITABLE);
		if(editable!=null) {
			manifest.setEditable(Boolean.parseBoolean(editable));
		}

		String parallel = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.PARALLEL);
		if(parallel!=null) {
			manifest.setParallel(Boolean.parseBoolean(parallel));
		}
	}

	@Override
	public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.CORPUS: {
			readAttributes(attributes);
		} break;

		case ManifestXmlTags.ROOT_CONTEXT: {
			return getContextManifestXmlDelegate().reset(getInstance()).root(true);
		}

		case ManifestXmlTags.CONTEXT: {
			return getContextManifestXmlDelegate().reset(getInstance()).root(false);
		}

		case ManifestXmlTags.NOTES: {
			// no-op
		} break;

		case ManifestXmlTags.NOTE: {
			String name = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.NAME);
			note = new NoteImpl(name);
			String date = ManifestXmlUtils.normalize(attributes, ManifestXmlAttributes.DATE);
			try {
				note.setModificationDate(DateUtils.parseDate(date));
			} catch (ParseException e) {
				throw new SAXException("Invalid modification date string", e); //$NON-NLS-1$
			}
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (localName) {
		case ManifestXmlTags.CORPUS: {
			return null;
		}

		case ManifestXmlTags.NOTES: {
			return this;
		}
		case ManifestXmlTags.NOTE: {
			note.changeContent(text);

			return this;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
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