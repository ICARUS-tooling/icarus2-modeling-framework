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
package de.ims.icarus2.model.manifest.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.standard.AnnotationLayerManifestImpl;
import de.ims.icarus2.model.manifest.standard.AnnotationManifestImpl;
import de.ims.icarus2.model.manifest.standard.ContainerManifestImpl;
import de.ims.icarus2.model.manifest.standard.ContextManifestImpl;
import de.ims.icarus2.model.manifest.standard.CorpusManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleManifestImpl;
import de.ims.icarus2.model.manifest.standard.FragmentLayerManifestImpl;
import de.ims.icarus2.model.manifest.standard.HighlightLayerManifestImpl;
import de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl;
import de.ims.icarus2.model.manifest.standard.OptionsManifestImpl;
import de.ims.icarus2.model.manifest.standard.PathResolverManifestImpl;
import de.ims.icarus2.model.manifest.standard.RasterizerManifestImpl;
import de.ims.icarus2.model.manifest.standard.StructureLayerManifestImpl;
import de.ims.icarus2.model.manifest.standard.StructureManifestImpl;
import de.ims.icarus2.model.manifest.xml.delegates.DefaultManifestXmlDelegateFactory;
import de.ims.icarus2.util.id.Identity;

/**
 * Important constraints:
 * <ul>
 * <li>Templates will inherit all data unchanged from their ancestor template if they declare one</li>
 * <li>Templates will overwrite all data they explicitly declare</li>
 * <li>Only top-level manifests in a &lt;manifests&gt; context are considered manifests</li>
 * <li>A live corpus will clone <b>all</b> data from its inherited manifests and re-link them
 * to the new instances</li>
 * <li>A template must be completely loaded and fully resolved before it can be used for further inheritance</li>
 * </ul>
 *
 * Reading is done in 4 steps:
 * <ol>
 * <li>Parsing all template sources into intermediate builder states</li>
 * <li>Creating from every top-level builder a new template object (this is done recursively to ensure that
 * referenced manifests get fully resolved before being further processed)</li>
 * <li>Parsing all live corpora into intermediate builder states</li>
 * <li>Creating fully cloned manifest instances for each corpus, preserving template informations</li>
 * </ol>
 *
 * Not thread-safe!
 *
 * @author Markus Gärtner
 *
 */
public class ManifestXmlReader extends ManifestXmlProcessor implements ManifestXmlTags, ManifestXmlAttributes {

	private final Set<ManifestLocation> templateSources = new LinkedHashSet<>();
	private final Set<ManifestLocation> corpusSources = new LinkedHashSet<>();
	private final AtomicBoolean reading = new AtomicBoolean(false);

	private final ParseState state = new ParseState();

	private final ManifestRegistry registry;

	private volatile static Schema schema;

	public static Schema getDefaultSchema() {
		Schema result = schema;

		if(result==null) {
			synchronized (ManifestXmlReader.class) {
				result = schema;
				if(result==null) {

					SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

					try {
						schema = factory.newSchema(ManifestXmlReader.class.getResource("corpus.xsd"));
					} catch (SAXException e) {
						throw new ManifestException(GlobalErrorCode.UNKNOWN_ERROR, "Failed to load default manifest xml schema", e);
					}

					schema = result;
				}
			}
		}

		return result;
	}

	public ManifestXmlReader(ManifestRegistry registry, ManifestXmlDelegateFactory delegateFactory) {
		super(delegateFactory);

		requireNonNull(registry);

		this.registry = registry;
	}

	public ManifestXmlReader(ManifestRegistry registry) {
		this(registry, new DefaultManifestXmlDelegateFactory());
	}

	public void addSource(ManifestLocation source) {
		if (source == null)
			throw new NullPointerException("Invalid source");  //$NON-NLS-1$

		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		Set<ManifestLocation> storage = source.isTemplate() ? templateSources : corpusSources;

		if(!storage.add(source))
			throw new IllegalArgumentException("Source already registered: "+source.getUrl()); //$NON-NLS-1$
	}

	@Override
	public void reset() {
		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		super.reset();
	}

	private List<Manifest> parseSources(Set<ManifestLocation> sources, XMLReader reader, InputSource inputSource) throws IOException, SAXException {

		List<Manifest> manifests = new ArrayList<>();
		for(ManifestLocation source : sources) {
			try (Reader in = source.getInput()) {
				RootHandler handler = new RootHandler(source);

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				inputSource.setCharacterStream(in);

				reader.parse(inputSource);

				manifests.addAll(handler.getTopLevelManifests());
			}
		}

		return manifests;
	}

	/**
	 * Parses templates from all registered template sources and returns them.
	 * Does <b>not</b> register templates with the registry!
	 *
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<Manifest> parseTemplates() throws SAXException, IOException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		try {

			XMLReader reader = newReader();
			InputSource inputSource = new InputSource();

			return parseSources(templateSources, reader, inputSource);
		} finally {
			reading.set(false);
		}
	}

	/**
	 * Parses corpora from all registered corpora sources and returns them.
	 * Does <b>not</b> register those coropra with the registry!
	 *
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<Manifest> parseCorpora() throws SAXException, IOException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		try {

			XMLReader reader = newReader();
			InputSource inputSource = new InputSource();

			return parseSources(corpusSources, reader, inputSource);
		} finally {
			reading.set(false);
		}
	}

	//FIXME change dependency from java.util.logging to the slf4j!!!
	public void readAndRegisterAll() throws IOException, SAXException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		try {
			XMLReader reader = newReader();

			InputSource inputSource = new InputSource();

			// Read and register all template manifests (use batch method!!)
			registry.addTemplates(parseSources(templateSources, reader, inputSource));

			// Read in, build and register all live corpora
			for(Manifest manifest : parseSources(corpusSources, reader, inputSource)) {
				CorpusManifest corpusManifest = (CorpusManifest) manifest;

				//TODO instantiate a fresh new corpus manifest with proper linking!

				registry.addCorpusManifest(corpusManifest);
			}
		} finally {
			reading.set(false);
		}
	}

	protected XMLReader newReader() throws SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(false);
		parserFactory.setSchema(getDefaultSchema());

		//FIXME

		SAXParser parser = null;
		try {
			parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException("Parser creation failed", e);
		}

		return parser.getXMLReader();
	}

	@SuppressWarnings("unused")
	protected class ParseState {

		private final Stack<Object> stack = new Stack<>();

		private boolean isRoot() {
			return stack.isEmpty();
		}

		public Object push(Object item) {
			return stack.push(item);
		}

		public Object pop() {
			return stack.pop();
		}

		private String trace(String msg) {
			StringBuilder sb = new StringBuilder("<root>"); //$NON-NLS-1$

			for(int i=stack.size()-1; i>-1; i--) {
				String id = null;
				Object item = stack.get(i);

				if(item instanceof Manifest) {
					id = ((Manifest)item).getId();
				} else if(item instanceof Identity) {
					id = ((Identity)item).getId();
				}

				if(id==null) {
					id = item.getClass().getSimpleName();
//					id = "<unknown>"; //$NON-NLS-1$
				}

				sb.append('.');

				sb.append(id);
			}

			if(sb.length()>0) {
				sb.append(": "); //$NON-NLS-1$
			}

			sb.append(msg);

			return sb.toString();
		}
	}

	protected class RootHandler extends DefaultHandler {

		private final StringBuilder buffer = new StringBuilder();

		private final Stack<ManifestXmlHandler> handlers = new Stack<>();

		// List of all top-level handlers. Used to preserve order of appearance
		private final List<Manifest> topLevelManifests = new ArrayList<>();

		private final ManifestLocation manifestLocation;

		RootHandler(ManifestLocation manifestLocation) {
			this.manifestLocation = manifestLocation;
		}

		/**
		 * @return the topLevelManifests
		 */
		public List<Manifest> getTopLevelManifests() {
			if(manifestLocation.isTemplate()) {
				for(Manifest manifest : topLevelManifests) {
					manifest.setIsTemplate(true);
				}
			}

			return topLevelManifests;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			buffer.append(ch, start, length);
		}

		private void push(ManifestXmlHandler handler) {
			handlers.push(handler);
			state.push(handler);
		}

		private ManifestXmlHandler pop() {
			ManifestXmlHandler handler = handlers.pop();
			state.pop();
			return handler;
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(handlers.isEmpty()) {
				push(new ManifestCollector());
			}

			ManifestXmlHandler current = handlers.peek();

			ManifestXmlHandler future = current.startElement(manifestLocation, uri, localName, qName, attributes);

			// Delegate initial element handling to next builder
			if(future!=null && future!=current) {
				push(future);

				future.startElement(manifestLocation, uri, localName, qName, attributes);
			}
		}

		/**
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String text = getText();

			ManifestXmlHandler current = handlers.peek();
			ManifestXmlHandler future = current.endElement(manifestLocation, uri, localName, qName, text);

			// Discard current builder and switch to ancestor
			if(future==null) {
				pop();

				// Root level means we just add the manifests from the collector
				if(handlers.isEmpty()) {
					topLevelManifests.addAll(((ManifestCollector)current).getManifests());
				} else {
					// Allow ancestor to collect nested entries
					ManifestXmlHandler ancestor = handlers.peek();

					ancestor.endNestedHandler(manifestLocation, uri, localName, qName, current);
				}
			}
		}

		protected String toLogMsg(SAXParseException ex) {
			StringBuilder sb = new StringBuilder();
			sb.append(ex.getMessage()).append(":\n"); //$NON-NLS-1$
//			sb.append("Message: ").append(ex.getMessage()).append('\n'); //$NON-NLS-1$
			sb.append("Public ID: ").append(String.valueOf(ex.getPublicId())).append('\n'); //$NON-NLS-1$
			sb.append("System ID: ").append(String.valueOf(ex.getSystemId())).append('\n'); //$NON-NLS-1$
			sb.append("Line: ").append(ex.getLineNumber()).append('\n'); //$NON-NLS-1$
			sb.append("Column: ").append(ex.getColumnNumber()); //$NON-NLS-1$
//			if(ex.getException()!=null)
//				sb.append("\nEmbedded: ").append(ex.getException()); //$NON-NLS-1$

//			report.log(level, sb.toString(), ex);

			return sb.toString();
		}

		@Override
		public void error(SAXParseException ex) throws SAXException {
			throw ex;
		}

		@Override
		public void warning(SAXParseException ex) throws SAXException {
			throw ex;
		}

		@Override
		public void fatalError(SAXParseException ex) throws SAXException {
			throw ex;
		}

		private String getText() {
			String text = buffer.length()==0 ? null : buffer.toString().trim();
			buffer.setLength(0);

			return (text==null || text.isEmpty()) ? null : text;
		}
	}

	private final static Map<String, Object> templateHandlers = new HashMap<>();

	private final static Map<String, Object> liveHandlers = new HashMap<>();

	static {
		// Live manifests
		liveHandlers.put(TAG_CORPUS, CorpusManifestImpl.class);

		// Templates
		templateHandlers.put(TAG_ANNOTATION_LAYER, AnnotationLayerManifestImpl.class);
		templateHandlers.put(TAG_ANNOTATION, AnnotationManifestImpl.class);
		templateHandlers.put(TAG_CONTAINER, ContainerManifestImpl.class);
		templateHandlers.put(TAG_CONTEXT, ContextManifestImpl.class);
		templateHandlers.put(TAG_DRIVER, DriverManifestImpl.class);
		templateHandlers.put(TAG_MODULE, ModuleManifestImpl.class);
		templateHandlers.put(TAG_FRAGMENT_LAYER, FragmentLayerManifestImpl.class);
		templateHandlers.put(TAG_HIGHLIGHT_LAYER, HighlightLayerManifestImpl.class);
		templateHandlers.put(TAG_ITEM_LAYER, ItemLayerManifestImpl.class);
		templateHandlers.put(TAG_OPTIONS, OptionsManifestImpl.class);
		templateHandlers.put(TAG_PATH_RESOLVER, PathResolverManifestImpl.class);
		templateHandlers.put(TAG_RASTERIZER, RasterizerManifestImpl.class);
		templateHandlers.put(TAG_STRUCTURE_LAYER, StructureLayerManifestImpl.class);
		templateHandlers.put(TAG_STRUCTURE, StructureManifestImpl.class);
	}

	@SuppressWarnings("rawtypes")
	private final Class[] CONSTRUCTOR_TYPES = {
		ManifestLocation.class,
		ManifestRegistry.class,
	};

	protected Manifest newInstance(String tag, ManifestLocation manifestLocation) throws SAXException {
		synchronized (CONSTRUCTOR_TYPES) {
			Map<String, Object> handlerLut = manifestLocation.isTemplate() ? templateHandlers : liveHandlers;
			Object current = handlerLut.get(tag);

			if(current==null)
				throw new SAXException("No manifest for tag: "+tag+" - template context: "+manifestLocation.isTemplate()); //$NON-NLS-1$

			Constructor<?> constructor;

			if(current instanceof Constructor) {
				constructor = (Constructor<?>) current;
			} else {
				Class<?> clazz = (Class<?>) current;
				try {
					constructor = clazz.getConstructor(CONSTRUCTOR_TYPES);
					//TODO maybe save resolved constructor to prevent future lookups?
				} catch (NoSuchMethodException | SecurityException e) {
					throw new SAXException("Failed to access manifest constructur", e); //$NON-NLS-1$
				}
			}

			try {
				return (Manifest) constructor.newInstance(manifestLocation, registry);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new SAXException("Failed to invoke manifest constructur", e); //$NON-NLS-1$
			}
		}
	}

	protected class ManifestCollector implements ManifestXmlHandler {

		// List of all top-level handlers. Used to preserve order of appearance
		private final List<Manifest> manifests = new ArrayList<>();

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#startElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public ManifestXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			switch (qName) {
			case TAG_CORPORA: {
				if(manifestLocation.isTemplate())
					throw new SAXException("Illegal "+TAG_CORPORA+" tag in template manifest source"); //$NON-NLS-1$ //$NON-NLS-2$
				return this;
			}

			case TAG_TEMPLATES: {
				if(!manifestLocation.isTemplate())
					throw new SAXException("Illegal "+TAG_TEMPLATES+" tag in live corpus manifest source"); //$NON-NLS-1$ //$NON-NLS-2$
				return this;
			}

			default:
				// no-op
				break;
			}

			Manifest manifest = newInstance(qName, manifestLocation);
			@SuppressWarnings("rawtypes")
			ManifestXmlDelegate delegate = getDelegate(manifest);

			return delegate.reset(manifest);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ManifestXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			return null;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endNestedHandler(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus2.model.manifest.xml.ManifestXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation, String uri,
				String localName, String qName, ManifestXmlHandler handler)
				throws SAXException {
			manifests.add((Manifest) ((ManifestXmlDelegate<?>) handler).getInstance());
		}

		/**
		 * @return the manifests
		 */
		public List<Manifest> getManifests() {
			return manifests;
		}
	}
}
