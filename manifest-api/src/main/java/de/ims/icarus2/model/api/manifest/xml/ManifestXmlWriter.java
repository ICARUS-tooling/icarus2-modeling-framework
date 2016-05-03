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

 * $Revision: 447 $
 * $Date: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/xml/ManifestXmlWriter.java $
 *
 * $LastChangedDate: 2016-01-14 11:34:47 +0100 (Do, 14 Jan 2016) $
 * $LastChangedRevision: 447 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.manifest.xml;

import static de.ims.icarus2.model.util.Conditions.checkNotNull;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;

import de.ims.icarus2.model.api.manifest.Manifest;
import de.ims.icarus2.model.api.manifest.ManifestLocation;
import de.ims.icarus2.model.api.manifest.xml.delegates.DefaultManifestXmlDelegateFactory;
import de.ims.icarus2.model.xml.XmlSerializer;
import de.ims.icarus2.model.xml.stream.XmlStreamSerializer;

/**
 * Implements an xml writing facility for {@link Manifest} objects.
 * TODO explain usage patterns: fill writer with manifests, write and then reset!!!
 *
 * @author Markus Gärtner
 * @version $Id: ManifestXmlWriter.java 447 2016-01-14 10:34:47Z mcgaerty $
 *
 */
public class ManifestXmlWriter extends ManifestXmlProcessor implements ManifestXmlTags, ManifestXmlAttributes {

	private final ManifestLocation manifestLocation;

	private final List<Manifest> manifests = new ArrayList<>();

	public ManifestXmlWriter(ManifestLocation manifestLocation, ManifestXmlDelegateFactory delegateFactory) {
		super(delegateFactory);

		checkNotNull(manifestLocation);

		this.manifestLocation = manifestLocation;
	}

	public ManifestXmlWriter(ManifestLocation manifestLocation) {
		this(manifestLocation, new DefaultManifestXmlDelegateFactory());
	}

	protected void checkManifest(Manifest manifest) {
		checkNotNull(manifest);

		//FIXME enable flag to turn off template check, so we can serialize whatever manifest desired
		if(manifest.getManifestLocation().isTemplate()!=manifestLocation.isTemplate())
			throw new IllegalArgumentException("Manifest 'isTemplate' flag differs from value declared for writer: "+manifest.isTemplate()); //$NON-NLS-1$

		// Make sure we have a valid xml delegate saved for the manifest's type
		getDelegate(manifest);
	}

	public void addManifest(Manifest manifest) {
		checkManifest(manifest);

		synchronized (manifests) {
			manifests.add(manifest);
		}
	}

	public void addManifests(List<? extends Manifest> manifests) {
		checkNotNull(manifests);

		synchronized (manifests) {
			for(Manifest manifest : manifests) {
				addManifest(manifest);
			}
		}
	}

	public void writeAll() throws Exception {
		synchronized (manifests) {
			if(manifests.isEmpty()) {
				// Nothing to do here
				return;
			}

			XmlSerializer serializer = newSerializer(manifestLocation.getOutput());

			String rootTag = manifestLocation.isTemplate() ? TAG_TEMPLATES : TAG_CORPORA;

			serializer.startDocument();
			serializer.startElement(rootTag);

			writeXsiInfo(serializer);

			writeInline(serializer);

			serializer.endElement(rootTag);
			serializer.endDocument();
		}
	}

	public void writeInline(XmlSerializer serializer) throws Exception {
		synchronized (manifests) {
			if(manifests.isEmpty()) {
				// Nothing to do here
				return;
			}

			write0(serializer);
		}
	}

	@SuppressWarnings("unchecked")
	protected void write0(XmlSerializer serializer) throws Exception {
		for(Iterator<Manifest> it = manifests.iterator(); it.hasNext();) {
			Manifest manifest = it.next();

			@SuppressWarnings("rawtypes")
			ManifestXmlDelegate delegate = getDelegate(manifest);

			delegate.reset(manifest);

			delegate.writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}
	}

	@Override
	public void reset() {
		synchronized (manifests) {
			manifests.clear();

			super.reset();
		}
	}

	protected XmlSerializer newSerializer(Writer out) throws Exception {

		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		return new XmlStreamSerializer(factory.createXMLStreamWriter(out));
	}

	protected void writeXsiInfo(XmlSerializer serializer) throws Exception {

		//TODO verify what amount of xsi info we need to include on the top level node
//		serializer.writeAttribute("xsi:noNamespaceSchemaLocation", "corpus.xsd");
//		serializer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	}
}
