/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.events.ManifestEvents;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.Counter;
import de.ims.icarus2.util.events.SimpleEventListener;
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.EventSource;
import de.ims.icarus2.util.events.WeakEventSource;

/**
 * @author Markus Gärtner
 *
 */
public final class DefaultManifestRegistry implements ManifestRegistry {

	private final Map<String, Manifest> templates = new LinkedHashMap<>();
	private final Map<String, LayerType> layerTypes = new LinkedHashMap<>();

	private final Map<String, CorpusManifest> corpora = new LinkedHashMap<>();

	private final EventSource eventSource = new WeakEventSource(this);

	private final AtomicInteger uidGenerator = new AtomicInteger(0);

	private final TemplateManifestLock templateManifestLock = new TemplateManifestLock();

	public DefaultManifestRegistry() {
		// no-op
	}

	private void fireEvent(EventObject event) {
		eventSource.fireEvent(event);
	}

	@Override
	public int createUID() {
		int uid = uidGenerator.incrementAndGet();
		if(uid<0)
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE, "Integer overflow in UID generation");

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
		requireNonNull(name);

		synchronized (layerTypes) {
			LayerType layerType = layerTypes.get(name);

			if(layerType==null)
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"No such layer-type: "+name); //$NON-NLS-1$

			return layerType;
		}
	}

	@Override
	public void addLayerType(LayerType layerType) {
		requireNonNull(layerType);

		String id = layerType.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Missing id on layer type"); //$NON-NLS-1$
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Invaid layer id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.ADD_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		synchronized (layerTypes) {
			LayerType currentType = layerTypes.get(id);
			if(currentType!=null && currentType!=layerType)
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Type id already in use: "+id); //$NON-NLS-1$

			layerTypes.put(id, layerType);
		}

		fireEvent(new EventObject(ManifestEvents.ADDED_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$
	}

	@Override
	public void removeLayerType(LayerType layerType) {
		requireNonNull(layerType);

		String id = layerType.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Missing id on layer type"); //$NON-NLS-1$
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Invaid layer id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.REMOVE_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		LayerManifest manifest = layerType.getSharedManifest();
		if(manifest!=null && isLocked(manifest))
			throw new ManifestException(ManifestErrorCode.MANIFEST_LOCKED,
					"Cannot remove layer type while underlying manifest is locked");

		synchronized (layerTypes) {
			if(!layerTypes.containsKey(id))
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown layer type id: "+id); //$NON-NLS-1$

			if(!layerTypes.remove(id, layerType))
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified layer type id is assigned to another type");
		}

		fireEvent(new EventObject(ManifestEvents.REMOVED_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$
	}

	@Override
	public void forEachLayerType(Consumer<? super LayerType> action) {
		synchronized (layerTypes) {
			layerTypes.values().forEach(action);
		}
	}

	@Override
	public void addCorpusManifest(CorpusManifest manifest) {
		requireNonNull(manifest);

		String id = manifest.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Missing corpus id"); //$NON-NLS-1$
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Invaid corpus id: "+id); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.ADD_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (corpora) {
			if(corpora.containsKey(id))
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Corpus id already in use: "+id); //$NON-NLS-1$

			corpora.put(id, manifest);

			templateManifestLock.lockTemplates(manifest);
		}

		fireEvent(new EventObject(ManifestEvents.ADDED_CORPUS, "corpus", manifest)); //$NON-NLS-1$
	}

	@Override
	public void removeCorpusManifest(CorpusManifest manifest) {
		requireNonNull(manifest);

		String id = manifest.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Missing corpus id"); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.REMOVE_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (corpora) {
			if(corpora.containsKey(id))
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown corpus id: "+id); //$NON-NLS-1$

			if(!corpora.remove(id, manifest))
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified corpus id is assigned to another manifest");

			templateManifestLock.unlockTemplates(manifest);
		}

		fireEvent(new EventObject(ManifestEvents.REMOVED_CORPUS, "corpus", manifest)); //$NON-NLS-1$
	}

	@Override
	public CorpusManifest getCorpusManifest(String id) {
		requireNonNull(id);

		CorpusManifest manifest = null;
		synchronized (corpora) {
			manifest = corpora.get(id);
		}
		if(manifest==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
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
		requireNonNull(id);

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
		requireNonNull(id);

		Manifest template = null;
		synchronized (templates) {
			template = templates.get(id);
		}

		if(template==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
					"No template registered for id: "+id); //$NON-NLS-1$
		if(!id.equals(template.getId()))
			throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
					"Illegal modification of template id detected. Expected "+id+" - got "+template.getId()); //$NON-NLS-1$ //$NON-NLS-2$

		return template;
	}

	@Override
	public void addContextManifest(CorpusManifest corpus, ContextManifest context) {
		requireNonNull(corpus);
		requireNonNull(context);

		if(context.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Cannot add a context template to a live corpus: "+context); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.ADD_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$

		corpus.addCustomContextManifest(context);

		templateManifestLock.lockTemplates(context);

		fireEvent(new EventObject(ManifestEvents.ADDED_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void removeContextManifest(CorpusManifest corpus, ContextManifest context) {
		requireNonNull(corpus);
		requireNonNull(context);

		if(context.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Attempting to remove a context template: "+context); //$NON-NLS-1$

		fireEvent(new EventObject(ManifestEvents.REMOVE_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$

		corpus.removeCustomContextManifest(context);

		templateManifestLock.unlockTemplates(context);

		fireEvent(new EventObject(ManifestEvents.REMOVED_CONTEXT,
				"corpus", corpus, "context", context)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void corpusManifestChanged(CorpusManifest corpus) {
		requireNonNull(corpus);

		fireEvent(new EventObject(ManifestEvents.CHANGED_CORPUS,
				"corpus", corpus)); //$NON-NLS-1$
	}

	@Override
	public void contextManifestChanged(ContextManifest context) {
		requireNonNull(context);

		fireEvent(new EventObject(ManifestEvents.CHANGED_CONTEXT,
				"corpus", context.getCorpusManifest(), //$NON-NLS-1$
				"context", context)); //$NON-NLS-1$
	}

	/**
	 * Must be called under 'templates' lock!
	 */
	private void checkTemplate(Manifest template) {
		if(!template.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Template does not declare valid identifier"); //$NON-NLS-1$
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Invalid template id: "+id); //$NON-NLS-1$
		Manifest current = templates.get(id);
		if(current!=null && current!=template)
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Template id already in use: "+id); //$NON-NLS-1$
	}

	@Override
	public void addTemplate(Manifest template) {
		requireNonNull(template);

		fireEvent(new EventObject(ManifestEvents.ADD_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (templates) {
			checkTemplate(template);
			templates.put(template.getId(), template);

			templateManifestLock.lockTemplates(template);
		}

		fireEvent(new EventObject(ManifestEvents.ADDED_TEMPLATE, "template", template)); //$NON-NLS-1$
	}

	@Override
	public void addTemplates(Collection<? extends Manifest> templates) {
		requireNonNull(templates);

		Object[] templatesArray = templates.toArray();

		fireEvent(new EventObject(ManifestEvents.ADD_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$

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

		fireEvent(new EventObject(ManifestEvents.ADDED_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$
	}

	@Override
	public void removeTemplate(Manifest template) {
		if (template == null)
			throw new NullPointerException("Invalid template"); //$NON-NLS-1$

		if(!template.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId();
		if(id==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Template does not declare valid identifier"); //$NON-NLS-1$
		if(!ManifestUtils.isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Invalid template id: "+id); //$NON-NLS-1$

		if(isLocked(template))
			throw new ManifestException(ManifestErrorCode.MANIFEST_LOCKED,
					"Cannot remove template while it is locked: "+ManifestUtils.getName(template));

		fireEvent(new EventObject(ManifestEvents.REMOVE_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (templates) {
			if(templates.containsKey(id))
				throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
						"Unknown template id: "+id); //$NON-NLS-1$

			if(!templates.remove(id, template))
				throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
						"Removal failed - specified template id is assigned to another manifest");

			templateManifestLock.unlockTemplates(template);
		}

		fireEvent(new EventObject(ManifestEvents.REMOVED_TEMPLATE, "template", template)); //$NON-NLS-1$
	}

	/**
	 * @param eventName
	 * @param listener
	 * @see de.ims.icarus2.util.events.EventSource#addListener(java.lang.String, de.ims.icarus2.util.events.SimpleEventListener)
	 */
	@Override
	public void addListener(String eventName, SimpleEventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @param listener
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.SimpleEventListener)
	 */
	@Override
	public void removeListener(SimpleEventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @param listener
	 * @param eventName
	 * @see de.ims.icarus2.util.events.EventSource#removeListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)
	 */
	@Override
	public void removeListener(SimpleEventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}

	@Override
	public LayerType getOverlayLayerType() {
		return getLayerType(DefaultLayerTypeIds.ITEM_LAYER_OVERLAY);
	}

	public static class TemplateManifestLock {

		private final Counter<Manifest> manifestRefCounter = new Counter<>();

		public boolean isLocked(Manifest manifest) {
			requireNonNull(manifest);
			return manifestRefCounter.hasCount(manifest);
		}

		public void lockTemplates(Manifest manifest) {
			requireNonNull(manifest);
			walkTemplate(manifest, false, 1);
		}

		public void unlockTemplates(Manifest manifest) {
			requireNonNull(manifest);
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
					throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
							"Non-template manifest assigned as template: "+ManifestUtils.getName(template));

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
				corpusManifest.forEachContextManifest(c -> walkTemplate(c, true, delta));
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
