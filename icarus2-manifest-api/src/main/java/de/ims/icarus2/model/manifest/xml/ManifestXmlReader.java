/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.xml;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.ims.icarus2.apiguard.Api;
import de.ims.icarus2.apiguard.Api.ApiType;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.apiguard.Mandatory;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
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
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.manifest.xml.delegates.DefaultManifestXmlDelegateFactory;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.IcarusUtils;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.lang.Lazy;
import de.ims.icarus2.util.xml.UnexpectedTagException;
import de.ims.icarus2.util.xml.XmlHandler;
import de.ims.icarus2.util.xml.XmlUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

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
public class ManifestXmlReader extends ManifestXmlProcessor {

	private final Set<ManifestLocation> templateSources = new LinkedHashSet<>();
	private final Set<ManifestLocation> corpusSources = new LinkedHashSet<>();
	//TODO add a buffer for sources for which the template state is unknown?
	private final AtomicBoolean reading = new AtomicBoolean(false);

	private final ManifestRegistry registry;

	private final String namespaceUri;
	private final String namespacePrefix;

	private final Consumer<Manifest> templateProcessor;
	private final Consumer<CorpusManifest> corpusProcessor;

	private static final Lazy<Schema> schema = XmlUtils.createShareableSchemaSource(
			ManifestXmlReader.class.getResource("corpus.xsd"));

	public static Schema getDefaultSchema() {
		return schema.value();
	}

	public static Builder builder() {
		return new Builder();
	}

	private ManifestXmlReader(Builder builder) {
		super(builder.getDelegateFactory());

		builder.validate();

		this.registry = builder.getRegistry();
		this.namespacePrefix = builder.getNamespacePrefix();
		this.namespaceUri = builder.getNamespaceUri();
		this.templateProcessor = builder.getTemplateProcessor();
		this.corpusProcessor = builder.getCorpusProcessor();

		List<ManifestLocation> sources = builder.getSources();
		sources.forEach(this::addSource);
	}

	public ManifestXmlReader addSource(ManifestLocation source) {
		requireNonNull(source);

		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		Set<ManifestLocation> storage = source.isTemplate() ? templateSources : corpusSources;

		if(!storage.add(source))
			throw new IllegalArgumentException("Source already registered: "+source.getUrl()); //$NON-NLS-1$

		return this;
	}

	public ManifestXmlReader reset() {
		if(reading.get())
			throw new IllegalStateException("Reading in progress, cannot add new sources"); //$NON-NLS-1$

		resetDelegates();

		return this;
	}

	private void parseSources(Set<ManifestLocation> sources, XMLReader reader, InputSource inputSource,
			Consumer<Manifest> action) throws IOException, SAXException {

		for(ManifestLocation source : sources) {
			try (Reader in = source.getInput()) {
				RootHandlerProxy handler = new RootHandlerProxy(source, new ManifestCollector());

				reader.setContentHandler(handler);
				reader.setErrorHandler(handler);
				reader.setEntityResolver(handler);
				reader.setDTDHandler(handler);

				inputSource.setCharacterStream(in);

				reader.parse(inputSource);

				handler.getTopLevelManifests().forEach(action);
			}
		}
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
			return parseTemplates0(reader, inputSource);
		} finally {
			reading.set(false);
		}
	}

	private List<Manifest> parseTemplates0(XMLReader reader, InputSource inputSource) throws SAXException, IOException {
		List<Manifest> results = new ObjectArrayList<>();
		Consumer<Manifest> action = results::add;
		if(templateProcessor!=null) {
			action = templateProcessor.andThen(action);
		}

		parseSources(templateSources, reader, inputSource, action);

		return results;
	}

	/**
	 * Parses corpora from all registered corpora sources and returns them.
	 * Does <b>not</b> register those coropra with the registry!
	 *
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<CorpusManifest> parseCorpora() throws SAXException, IOException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		try {
			XMLReader reader = newReader();
			InputSource inputSource = new InputSource();
			return parseCorpora0(reader, inputSource);
		} finally {
			reading.set(false);
		}
	}

	private List<CorpusManifest> parseCorpora0(XMLReader reader, InputSource inputSource) throws SAXException, IOException {
		List<CorpusManifest> results = new ObjectArrayList<>();
		Consumer<Manifest> action = m -> results.add((CorpusManifest) m);
		if(corpusProcessor!=null) {
			action = m -> {
				CorpusManifest cm = (CorpusManifest)m;
				corpusProcessor.accept(cm);
				results.add(cm);
			};
		}


		parseSources(corpusSources, reader, inputSource, action);

		return results;
	}

	public ManifestXmlReader readAndRegisterAll() throws IOException, SAXException {

		if(!reading.compareAndSet(false, true))
			throw new IllegalStateException("Reading already in progress"); //$NON-NLS-1$

		try {
			XMLReader reader = newReader();

			InputSource inputSource = new InputSource();

			// Read and register all template manifests (use batch method!!)
			registry.addTemplates(parseTemplates0(reader, inputSource));

			// Read in, build and register all live corpora
			for(CorpusManifest manifest : parseCorpora0(reader, inputSource)) {
				//TODO instantiate a fresh new corpus manifest with proper linking!

				registry.addCorpusManifest(manifest);
			}
		} finally {
			reading.set(false);
		}

		return this;
	}

	/**
	 * Delegates to {@link #defaultCreateReader()}.
	 * Subclasses can override this method to customize the
	 * actual reader implementation.
	 *
	 * @return
	 * @throws SAXException
	 */
	protected XMLReader newReader() throws SAXException {
		return defaultCreateReader(true);
	}

	/**
	 * Creates a default {@link XMLReader} that is namespace aware
	 * and using the {@link #getDefaultSchema() default schema} for
	 * manifest instances if the {@code validate} parameter is set
	 * to {@code true}.
	 *
	 * @return
	 * @throws SAXException
	 */
	public static XMLReader defaultCreateReader(boolean validate) throws SAXException {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
		parserFactory.setValidating(false);
		if(validate) {
			parserFactory.setSchema(getDefaultSchema());
		}

		SAXParser parser = null;
		try {
			parser = parserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			throw new SAXException("Parser creation failed", e);
		}

		return parser.getXMLReader();
	}

	@SuppressWarnings("unused")
	protected static class ParseState {

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
					id = ((Manifest)item).getId().orElse(null);
				} else if(item instanceof Identity) {
					id = ((Identity)item).getId().orElse(null);
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

	public static class RootHandlerProxy extends XmlHandler {

		private final ManifestXmlHandler root;

		private final Stack<ManifestXmlHandler> handlers = new Stack<>();

		private final ParseState state = new ParseState();

		// List of all top-level handlers. Used to preserve order of appearance
		private final List<Manifest> topLevelManifests = new ArrayList<>();

		private final ManifestLocation manifestLocation;

		public RootHandlerProxy(ManifestLocation manifestLocation, ManifestXmlHandler root) {
			this.manifestLocation = requireNonNull(manifestLocation);
			this.root = requireNonNull(root);
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
				push(root);
			}

			ManifestXmlHandler current = handlers.peek();

			ManifestXmlHandler future =
					current.startElement(manifestLocation, uri, localName, qName, attributes)
					.filter(IcarusUtils.notEq(current))
					.orElse(null);

			// Delegate initial element handling to next builder
			if(future!=null) {
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
			Optional<ManifestXmlHandler> future = current.endElement(manifestLocation, uri, localName, qName, text);

			// Discard current builder and switch to ancestor
			if(!future.isPresent()) {
				pop();

				// Root level means we just add the manifests from the collector
				if(handlers.isEmpty()) {

					// If we're used as part of the default reading procedure, collect the top-level manifests
					if(current instanceof ManifestCollector) {
						topLevelManifests.addAll(((ManifestCollector)current).getManifests());
					}
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
			sb.append("Public ID: ").append(String.valueOf(ex.getPublicId())).append('\n'); //$NON-NLS-1$
			sb.append("System ID: ").append(String.valueOf(ex.getSystemId())).append('\n'); //$NON-NLS-1$
			sb.append("Line: ").append(ex.getLineNumber()).append('\n'); //$NON-NLS-1$
			sb.append("Column: ").append(ex.getColumnNumber()); //$NON-NLS-1$
			if(ex.getException()!=null)
				sb.append("\nEmbedded: ").append(ex.getException()); //$NON-NLS-1$

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
	}

	private final static Map<String, Object> templateHandlers = new HashMap<>();

	private final static Map<String, Object> liveHandlers = new HashMap<>();

	static {
		// Live manifests
		liveHandlers.put(ManifestXmlTags.CORPUS, CorpusManifestImpl.class);

		// Templates
		templateHandlers.put(ManifestXmlTags.ANNOTATION_LAYER, AnnotationLayerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.ANNOTATION, AnnotationManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.CONTAINER, ContainerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.CONTEXT, ContextManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.DRIVER, DriverManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.MODULE, ModuleManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.FRAGMENT_LAYER, FragmentLayerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.HIGHLIGHT_LAYER, HighlightLayerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.ITEM_LAYER, ItemLayerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.OPTIONS, OptionsManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.PATH_RESOLVER, PathResolverManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.RASTERIZER, RasterizerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.STRUCTURE_LAYER, StructureLayerManifestImpl.class);
		templateHandlers.put(ManifestXmlTags.STRUCTURE, StructureManifestImpl.class);
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
				throw new SAXException("No manifest delegate for tag: "+tag+" - template context: "+manifestLocation.isTemplate()); //$NON-NLS-1$

			Constructor<?> constructor;

			if(current instanceof Constructor) {
				constructor = (Constructor<?>) current;
			} else {
				Class<?> clazz = (Class<?>) current;
				try {
					constructor = clazz.getConstructor(CONSTRUCTOR_TYPES);
					//TODO maybe save resolved constructor to prevent future lookups?
				} catch (NoSuchMethodException | SecurityException e) {
					throw new SAXException("Failed to access manifest constructur", e);
				}
			}

			try {
				return (Manifest) constructor.newInstance(manifestLocation, registry);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new SAXException("Failed to invoke manifest constructur", e);
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
		public Optional<ManifestXmlHandler> startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if(namespaceUri!=null && !namespaceUri.equals(uri))
				throw new SAXException(Messages.mismatch("Namespace URI mismatch", namespaceUri, uri));
			if(namespacePrefix!=null && !namespacePrefix.regionMatches(
					0, qName, 0, namespacePrefix.length()))
				throw new SAXException(Messages.mismatch("Namespace prefix mismatch", namespacePrefix, qName));

			ManifestXmlHandler handler = null;

			switch (localName) {

			case ManifestXmlTags.MANIFEST: {
				handler = this;
			} break;

			case ManifestXmlTags.CORPORA: {
				if(manifestLocation.isTemplate())
					throw new SAXException("Illegal "+ManifestXmlTags.CORPORA+" tag in template manifest source");
				handler = this;
			} break;

			case ManifestXmlTags.TEMPLATES: {
				if(!manifestLocation.isTemplate())
					throw new SAXException("Illegal "+ManifestXmlTags.TEMPLATES+" tag in live corpus manifest source");
				handler = this;
			} break;

			case ManifestXmlTags.TEST:
				throw new UnexpectedTagException(qName, true, "root");

			default:
				// no-op
				break;
			}

			if(handler==null) {
				Manifest manifest = newInstance(localName, manifestLocation);
				@SuppressWarnings("rawtypes")
				ManifestXmlDelegate delegate = getDelegate(manifest);

				handler = delegate.reset(manifest);
			}

			return Optional.ofNullable(handler);
		}

		/**
		 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlHandler#endElement(de.ims.icarus2.model.manifest.api.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public Optional<ManifestXmlHandler> endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {

			ManifestXmlHandler handler = null;

			switch (localName) {

			case ManifestXmlTags.CORPORA:
			case ManifestXmlTags.TEMPLATES: {
				handler = this;
			} break;

			case ManifestXmlTags.TEST:
				throw new UnexpectedTagException(qName, true, "root");

			default:
				// no-op
				break;
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
			manifests.add((Manifest) ((ManifestXmlDelegate<?>) handler).getInstance());
		}

		/**
		 * @return the manifests
		 */
		public List<Manifest> getManifests() {
			return manifests;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@Api(type=ApiType.BUILDER)
	public static class Builder extends AbstractBuilder<Builder, ManifestXmlReader> {

		/**
		 * Registry to store loaded manifest in
		 */
		private ManifestRegistry registry;

		/**
		 * Source for creating new XML delegates dynamically
		 */
		private ManifestXmlDelegateFactory delegateFactory;

		/**
		 * Namespace URI
		 */
		private String uri;

		/**
		 * Expected prefix for the manifest files
		 */
		private String prefix;

		private final List<ManifestLocation> sources = new ArrayList<>();

		private Consumer<Manifest> templateProcessor;
		private Consumer<CorpusManifest> corpusProcessor;

		private Builder() {
			// no-op
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ManifestRegistry getRegistry() {
			return registry;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public ManifestXmlDelegateFactory getDelegateFactory() {
			return delegateFactory;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public String getNamespaceUri() {
			return uri;
		}

		@Guarded(methodType=MethodType.GETTER)
		@Nullable
		public String getNamespacePrefix() {
			return prefix;
		}

		@Guarded(methodType=MethodType.GETTER)
		public List<ManifestLocation> getSources() {
			return Collections.unmodifiableList(sources);
		}

		@Guarded(methodType=MethodType.GETTER)
		public Consumer<Manifest> getTemplateProcessor() {
			return templateProcessor;
		}

		@Guarded(methodType=MethodType.GETTER)
		public Consumer<CorpusManifest> getCorpusProcessor() {
			return corpusProcessor;
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder registry(ManifestRegistry registry) {
			requireNonNull(registry);
			checkState("Registry already set", this.registry==null);

			this.registry = registry;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder delegateFactory(ManifestXmlDelegateFactory delegateFactory ) {
			requireNonNull(delegateFactory);
			checkState("Delegate factory already set", this.delegateFactory==null);

			this.delegateFactory = delegateFactory;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder namespacePrefix(String namespacePrefix) {
			requireNonNull(namespacePrefix);
			checkArgument(!namespacePrefix.isEmpty());
			checkState("Registry already set", this.prefix==null);

			this.prefix = namespacePrefix;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		@Mandatory
		public Builder namespaceUri(String namespaceUri) {
			requireNonNull(namespaceUri);
			checkArgument(!namespaceUri.isEmpty());
			checkState("Namespace URI already set", this.uri==null);

			this.uri = namespaceUri;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder source(ManifestLocation...locations) {
			requireNonNull(locations);
			checkArgument(locations.length>0);

			CollectionUtils.feedItems(sources, locations);

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder templateProcessor(Consumer<Manifest> templateProcessor) {
			requireNonNull(templateProcessor);
			checkState("Template processor already set", this.templateProcessor==null);

			this.templateProcessor = templateProcessor;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.BUILDER)
		public Builder corpusProcessor(Consumer<CorpusManifest> corpusProcessor) {
			requireNonNull(corpusProcessor);
			checkState("Corpus processor already set", this.corpusProcessor==null);

			this.corpusProcessor = corpusProcessor;

			return thisAsCast();
		}

		/**
		 * Configures the reader to use {@link ManifestXmlUtils#MANIFEST_NS_PREFIX}
		 * as {@link #namespacePrefix(String) namespace prefix} and
		 * {@link ManifestXmlUtils#MANIFEST_NAMESPACE_URI} as
		 * {@link #namespaceUri(String) namespace URI}, as well as
		 * setting a new instance of {@link DefaultManifestXmlDelegateFactory}
		 * as the {@link #delegateFactory(ManifestXmlDelegateFactory) delegate factory}.
		 *
		 * @return
		 *
		 * @see DefaultManifestXmlDelegateFactory
		 * @see ManifestXmlUtils#MANIFEST_NAMESPACE_URI
		 * @see ManifestXmlUtils#MANIFEST_NS_PREFIX
		 */
		public Builder useImplementationDefaults() {
			return delegateFactory(new DefaultManifestXmlDelegateFactory())
					.namespacePrefix(ManifestXmlUtils.MANIFEST_NS_PREFIX)
					.namespaceUri(ManifestXmlUtils.MANIFEST_NAMESPACE_URI);
		}

		@Override
		protected void validate() {
			super.validate();

			checkState("Missing registry", registry!=null);
			checkState("Missing delegate factory", delegateFactory!=null);
			checkState("Missing namespace uri", uri!=null);
			checkState("Missing namespace prefix", prefix!=null);
		}

		@Override
		protected ManifestXmlReader create() {
			validate();
			return new ManifestXmlReader(this);
		}

	}
}
