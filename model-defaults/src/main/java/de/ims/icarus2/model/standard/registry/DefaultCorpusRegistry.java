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

 * $Revision: 433 $
 * $Date: 2015-10-15 16:11:29 +0200 (Do, 15 Okt 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/standard/registry/DefaultCorpusRegistry.java $
 *
 * $LastChangedDate: 2015-10-15 16:11:29 +0200 (Do, 15 Okt 2015) $
 * $LastChangedRevision: 433 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.registry;

import static de.ims.icarus2.model.standard.util.CorpusUtils.getName;
import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.events.CorpusEvents;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.standard.util.CorpusUtils;
import de.ims.icarus2.util.Counter;
import de.ims.icarus2.util.events.EventListener;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.EventSource;
import de.ims.icarus2.util.events.WeakEventSource;

/**
 * @author Markus Gärtner
 * @version $Id: DefaultCorpusRegistry.java 433 2015-10-15 14:11:29Z mcgaerty $
 *
 */
public final class DefaultCorpusRegistry implements CorpusRegistry {

	private final Map<String, Manifest> templates = new LinkedHashMap<>();
	private final Map<String, LayerType> layerTypes = new LinkedHashMap<>();

	private final Map<String, CorpusManifest> corpora = new LinkedHashMap<>();

	private final EventSource eventSource = new WeakEventSource(this);

	private final AtomicInteger uidGenerator = new AtomicInteger(0);

	private final TemplateManifestLock templateManifestLock = new TemplateManifestLock();

	public DefaultCorpusRegistry() {
		// no-op
	}

	private void fireEvent(EventObject event) {
		eventSource.fireEvent(event);
	}

//	public void registerPluginTemplates(Collection<Extension> extensions) {
//		synchronized (templates) {
//			// Load default template file
////			loadTemplates(CorpusRegistry.class.getResource("model-templates.xml")); //$NON-NLS-1$
//			//FIXME
//
//			PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
//					.getPluginDescriptor(ModelPlugin.PLUGIN_ID);
//
//			// Load explicit template files
//			for(Extension extension : descriptor.getExtensionPoint("ModelTemplates").getConnectedExtensions()) { //$NON-NLS-1$
//				for(Extension.Parameter parameter : extension.getParameters("path")) { //$NON-NLS-1$
//					try {
//						URL url = parameter.valueAsUrl();
//						//FIXME
//					} catch (Exception e) {
//						LoggerFactory.error(this, "Failed to load templates for path: " //$NON-NLS-1$
//								+extension.getUniqueId()+" ("+parameter.rawValue()+")", e); //$NON-NLS-1$ //$NON-NLS-2$
//					}
//				}
//			}
//
//			// Process template folders
//			for(Extension extension : descriptor.getExtensionPoint("ModelFolder").getConnectedExtensions()) { //$NON-NLS-1$
//
//				Path pluginFolder = PluginUtil.getPluginFolder(extension);
//
//				for(Extension.Parameter parameter : extension.getParameters("folder")) { //$NON-NLS-1$
//					Path folder = pluginFolder.resolve(parameter.valueAsString());
//
//					if(Files.notExists(folder)) {
//						LoggerFactory.warning(this, "Missing template folder: "+folder); //$NON-NLS-1$
//						continue;
//					}
//
//			        // Recursively traverse content of folder till we find plug-ins
//			        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folder, xmlPathFilter)) {
//
//			        	int numEntries = 0;
//
//						for(Path file : stream) {
//							try {
//								//FIXME
//							} catch (Exception e) {
//								LoggerFactory.error(this, "Failed to load file from template folder: " //$NON-NLS-1$
//										+extension.getUniqueId()+" ("+file+")", e); //$NON-NLS-1$ //$NON-NLS-2$
//							} finally {
//								numEntries++;
//							}
//						}
//
//						if(numEntries==0) {
//							LoggerFactory.warning(this, "Template folder is empty: "+folder); //$NON-NLS-1$
//							continue;
//						}
//			        } catch (IOException e) {
//			            LoggerFactory.error(this, "Failed traversing files in plug-in folder " + folder, e); //$NON-NLS-1$
//					}
//				}
//			}
//		}
//	}

//	public void registerLayerTypes() {
//
//		PluginDescriptor descriptor = PluginUtil.getPluginRegistry()
//				.getPluginDescriptor(ModelPlugin.PLUGIN_ID);
//
//		// Load explicit template files
//		for(Extension extension : descriptor.getExtensionPoint("LayerType").getConnectedExtensions()) { //$NON-NLS-1$
//
//			LayerType layerType = null;
//
//			Extension.Parameter classParam = extension.getParameter("class"); //$NON-NLS-1$
//			if(classParam!=null) {
//				layerType = new LayerTypeWrapper(extension.getId(), extension);
//			} else {
//				layerType = new LazyExtensionLayerType(extension);
//			}
//
//			try {
//				registerLayerType(layerType);
//			} catch(Exception e) {
//				LoggerFactory.error(this, "Failed to register layer type: "+extension, e); //$NON-NLS-1$
//			}
//		}
//	}

	@Override
	public int createUID() {
		int uid = uidGenerator.incrementAndGet();
		if(uid<0)
			throw new ModelException(ModelErrorCode.ILLEGAL_STATE, "Integer overflow in UID generation");

		return uid;
	}

	@Override
	public void resetUIDs() {
		uidGenerator.set(0);
	}

	@Override
	public boolean isLocked(Manifest manifest) {
		return templateManifestLock.isLocked(manifest);
	}

	@Override
	public Set<ManifestLocation> getTemplateSources() {
		Set<ManifestLocation> result = new HashSet<>(templates.size());

		synchronized (templates) {
			templates.forEach((s, m) -> result.add(m.getManifestLocation()));
		}

		return result;
	}

	@Override
	public Set<ManifestLocation> getCorpusSources() {
		Set<ManifestLocation> result = new HashSet<>(corpora.size());

		synchronized (corpora) {
			corpora.values().forEach(m -> result.add(m.getManifestLocation()));
		}

		return result;
	}

	@Override
	public LayerType getLayerType(String name) {
		checkNotNull(name);

		synchronized (layerTypes) {
			LayerType layerType = layerTypes.get(name);

			if(layerType==null)
				throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
						"No such layer-type: "+name); //$NON-NLS-1$

			return layerType;
		}
	}

	@Override
	public void addLayerType(LayerType layerType) {
		checkNotNull(layerType);

		String id = layerType.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Missing id on layer type"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Invaid layer id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.ADD_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		synchronized (layerTypes) {
			LayerType currentType = layerTypes.get(id);
			if(currentType!=null && currentType!=layerType)
				throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
						"Type id already in use: "+id); //$NON-NLS-1$

			layerTypes.put(id, layerType);
		}

		fireEvent(new EventObject(CorpusEvents.ADDED_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$
	}

	@Override
	public void removeLayerType(LayerType layerType) {
		checkNotNull(layerType);

		String id = layerType.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Missing id on layer type"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Invaid layer id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.REMOVE_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		LayerManifest manifest = layerType.getSharedManifest();
		if(manifest!=null && isLocked(manifest))
			throw new ModelException(ModelErrorCode.MANIFEST_LOCKED,
					"Cannot remove layer type while underlying manifest is locked");

		synchronized (layerTypes) {
			if(!layerTypes.containsKey(id))
				throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown layer type id: "+id); //$NON-NLS-1$

			if(!layerTypes.remove(id, layerType))
				throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified layer type id is assigned to another type");
		}

		fireEvent(new EventObject(CorpusEvents.REMOVED_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$
	}

	@Override
	public void forEachLayerType(Consumer<? super LayerType> action) {
		synchronized (layerTypes) {
			layerTypes.values().forEach(action);
		}
	}

	@Override
	public void addCorpus(CorpusManifest manifest) {
		checkNotNull(manifest);

		String id = manifest.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Missing corpus id"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Invaid corpus id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.ADD_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (corpora) {
			if(corpora.containsKey(id))
				throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
						"Corpus id already in use: "+id); //$NON-NLS-1$

			corpora.put(id, manifest);

			templateManifestLock.lockTemplates(manifest);
		}

		fireEvent(new EventObject(CorpusEvents.ADDED_CORPUS, "corpus", manifest)); //$NON-NLS-1$
	}

	@Override
	public void removeCorpus(CorpusManifest manifest) {
		checkNotNull(manifest);

		String id = manifest.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Missing corpus id"); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.REMOVE_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (corpora) {
			if(corpora.containsKey(id))
				throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown corpus id: "+id); //$NON-NLS-1$

			if(!corpora.remove(id, manifest))
				throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified corpus id is assigned to another manifest");

			templateManifestLock.unlockTemplates(manifest);
		}

		fireEvent(new EventObject(CorpusEvents.REMOVED_CORPUS, "corpus", manifest)); //$NON-NLS-1$
	}

	@Override
	public CorpusManifest getCorpus(String id) {
		checkNotNull(id);

		CorpusManifest manifest = null;
		synchronized (corpora) {
			manifest = corpora.get(id);
		}
		if(manifest==null)
			throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
					"No such corpus: "+id); //$NON-NLS-1$

		return manifest;
	}

	@Override
	public void forEachCorpus(Consumer<? super CorpusManifest> action) {
		synchronized (corpora) {
			corpora.values().forEach(action);
		}
	}

	@Override
	public boolean hasTemplate(String id) {
		checkNotNull(id);

		synchronized (templates) {
			return templates.containsKey(id);
		}
	}

	@Override
	public void forEachTemplate(Consumer<? super Manifest> action) {
		synchronized (templates) {
			templates.values().forEach(action);
		}
	}

	@Override
	public Manifest getTemplate(String id) {
		checkNotNull(id);

		Manifest template = null;
		synchronized (templates) {
			template = templates.get(id);
		}

		if(template==null)
			throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
					"No template registered for id: "+id); //$NON-NLS-1$
		if(!id.equals(template.getId()))
			throw new ModelException(ModelErrorCode.MANIFEST_CORRUPTED_STATE,
					"Illegal modification of template id detected. Expected "+id+" - got "+template.getId()); //$NON-NLS-1$ //$NON-NLS-2$

		return template;
	}

	@Override
	public void addContext(CorpusManifest corpus, ContextManifest context) {
		checkNotNull(corpus);
		checkNotNull(context);

		if(context.isTemplate())
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Cannot add a context template to a live corpus: "+context); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.ADD_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$

		corpus.addCustomContextManifest(context);

		templateManifestLock.lockTemplates(context);

		fireEvent(new EventObject(CorpusEvents.ADDED_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void removeContext(CorpusManifest corpus, ContextManifest context) {
		checkNotNull(corpus);
		checkNotNull(context);

		if(context.isTemplate())
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Attempting to remove a context template: "+context); //$NON-NLS-1$

		fireEvent(new EventObject(CorpusEvents.REMOVE_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$

		corpus.removeCustomContextManifest(context);

		templateManifestLock.unlockTemplates(context);

		fireEvent(new EventObject(CorpusEvents.REMOVED_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void corpusChanged(CorpusManifest corpus) {
		checkNotNull(corpus);

		fireEvent(new EventObject(CorpusEvents.CHANGED_CORPUS,
				"corpus", corpus)); //$NON-NLS-1$
	}

	@Override
	public void contextChanged(ContextManifest context) {
		checkNotNull(context);

		fireEvent(new EventObject(CorpusEvents.CHANGED_CONTEXT,
				"corpus", context.getCorpusManifest(), //$NON-NLS-1$
				"context", context)); //$NON-NLS-1$
	}

	/**
	 * Must be called under 'templates' lock!
	 */
	private void checkTemplate(Manifest template) {
		if(!template.isTemplate())
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Template does not declare valid identifier"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Invalid template id: "+id); //$NON-NLS-1$
		Manifest current = templates.get(id);
		if(current!=null && current!=template)
			throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
					"Template id already in use: "+id); //$NON-NLS-1$
	}

	@Override
	public void addTemplate(Manifest template) {
		checkNotNull(template);

		fireEvent(new EventObject(CorpusEvents.ADD_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (templates) {
			checkTemplate(template);
			templates.put(template.getId(), template);

			templateManifestLock.lockTemplates(template);
		}

		fireEvent(new EventObject(CorpusEvents.ADDED_TEMPLATE, "template", template)); //$NON-NLS-1$
	}

	@Override
	public void addTemplates(Collection<? extends Manifest> templates) {
		checkNotNull(templates);

		Object[] templatesArray = templates.toArray();

		fireEvent(new EventObject(CorpusEvents.ADD_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$

		synchronized (this.templates) {

			// First pass: check templates
			for(Manifest template : templates) {
				checkTemplate(template);
			}

			// Second pass: add templates
			for(Manifest template : templates) {
				this.templates.put(template.getId(), template);
			}

			// Third pass: update locks

			// Second pass: add templates
			for(Manifest template : templates) {
				templateManifestLock.lockTemplates(template);
			}
		}

		fireEvent(new EventObject(CorpusEvents.ADDED_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$
	}

	@Override
	public void removeTemplate(Manifest template) {
		if (template == null)
			throw new NullPointerException("Invalid template"); //$NON-NLS-1$

		if(!template.isTemplate())
			throw new ModelException(ModelErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId();
		if(id==null)
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Template does not declare valid identifier"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new ModelException(ModelErrorCode.MANIFEST_INVALID_ID,
					"Invalid template id: "+id); //$NON-NLS-1$

		if(isLocked(template))
			throw new ModelException(ModelErrorCode.MANIFEST_LOCKED,
					"Cannot remove template while it is locked: "+getName(template));

		fireEvent(new EventObject(CorpusEvents.REMOVE_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (templates) {
			if(templates.containsKey(id))
				throw new ModelException(ModelErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown template id: "+id); //$NON-NLS-1$

			if(!templates.remove(id, template))
				throw new ModelException(ModelErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified template id is assigned to another manifest");

			templateManifestLock.unlockTemplates(template);
		}

		fireEvent(new EventObject(CorpusEvents.REMOVED_TEMPLATE, "template", template)); //$NON-NLS-1$
	}

	/**
	 * @param eventName
	 * @param listener
	 * @see de.ims.icarus2.util.events.EventSource#addListener(java.lang.String, de.ims.icarus2.util.events.EventListener)
	 */
	@Override
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @param listener
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.EventListener)
	 */
	@Override
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @param listener
	 * @param eventName
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.EventListener, java.lang.String)
	 */
	@Override
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	@Override
	public LayerType getOverlayLayerType() {
		return getLayerType(DefaultLayerTypeIds.ITEM_LAYER_OVERLAY);
	}

	public static class TemplateManifestLock {

		private final Counter<Manifest> manifestRefCounter = new Counter<>();

		public boolean isLocked(Manifest manifest) {
			checkNotNull(manifest);
			return manifestRefCounter.hasCount(manifest);
		}

		public void lockTemplates(Manifest manifest) {
			checkNotNull(manifest);
			walkTemplate(manifest, false, 1);
		}

		public void unlockTemplates(Manifest manifest) {
			checkNotNull(manifest);
			walkTemplate(manifest, false, -1);
		}

		private void walkTemplate(Manifest manifest, boolean checkOwn, int delta) {
			if(manifest==null) {
				return;
			}

			// Traverse template hierarchy
			Manifest template = manifest.getTemplate();
			if(template!=null) {
				if(!template.isTemplate())
					throw new ModelException(ModelErrorCode.MANIFEST_ERROR,
							"Non-template manifest assigned as template: "+getName(template));

				walkTemplate(template, true, delta);
			}

			// Traverse options manifest
			if(manifest instanceof MemberManifest) {
				MemberManifest memberManifest = (MemberManifest) manifest;
				walkTemplate(memberManifest.getOptionsManifest(), true, delta);
			}

			// For layer manifests lock the underlying shared manifest
			if(manifest instanceof LayerManifest) {
				LayerManifest layerManifest = (LayerManifest) manifest;
				LayerType layerType = layerManifest.getLayerType();
				if(layerType!=null) {
					walkTemplate(layerType.getSharedManifest(), true, delta);
				}
			}

			// Now do some manifest type sensitive handling
			switch (manifest.getManifestType()) {

			case CORPUS_MANIFEST: {
				CorpusManifest corpusManifest = (CorpusManifest) manifest;
				walkTemplate(corpusManifest.getRootContextManifest(), true, delta);
				corpusManifest.forEachCustomContextManifest(c -> walkTemplate(c, true, delta));
			} break;

			case CONTEXT_MANIFEST: {
				ContextManifest contextManifest = (ContextManifest) manifest;
				contextManifest.forEachLayerManifest(l -> walkTemplate(l, true, delta));
				walkTemplate(contextManifest.getDriverManifest(), true, delta);
			} break;

			default:
				break;
			}

			// Finally modify reference counter in case we got a real template
			if(checkOwn && manifest.isTemplate()) {
				manifestRefCounter.add(manifest, delta);
			}
		}
	}
}
