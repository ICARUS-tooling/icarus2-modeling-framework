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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
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
import de.ims.icarus2.util.events.EventObject;
import de.ims.icarus2.util.events.EventSource;
import de.ims.icarus2.util.events.SimpleEventListener;
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

	private final Object lock = new Object();

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

	/**
	 * {@inheritDoc}
	 *
	 * This method scans the current registry content for the largest uid amongst
	 * all registered templates and live corpora and then adds {@code +1} to this
	 * value as the new smallest uid to be returned for future {@link #createUID()}
	 * calls.
	 *
	 * @see de.ims.icarus2.model.manifest.api.ManifestRegistry#resetUIDs()
	 */
	@Override
	public void resetUIDs() {
		int minUid = -1;
		synchronized (lock) {
			minUid = templates.values()
					.parallelStream()
					.mapToInt(Manifest::getUID)
					.max()
					.orElse(minUid);

			minUid = corpora.values()
					.parallelStream()
					.mapToInt(CorpusManifest::getUID)
					.max()
					.orElse(minUid);
		}
		uidGenerator.set(minUid+1);
	}

	@Override
	public boolean isLocked(Manifest manifest) {
		return templateManifestLock.isLocked(manifest);
	}

	@Override
	public Set<ManifestLocation> getTemplateSources() {
		Set<ManifestLocation> result = new HashSet<>(templates.size());

		synchronized (lock) {
			templates.forEach((s, m) -> result.add(m.getManifestLocation()));
		}

		return result;
	}

	@Override
	public Set<ManifestLocation> getCorpusSources() {
		Set<ManifestLocation> result = new HashSet<>(corpora.size());

		synchronized (lock) {
			corpora.values().forEach(m -> result.add(m.getManifestLocation()));
		}

		return result;
	}

	@Override
	public Optional<LayerType> getLayerType(String name) {
		requireNonNull(name);

		synchronized (lock) {
			return Optional.ofNullable(layerTypes.get(name));
		}
	}

	@Override
	public void addLayerType(LayerType layerType) {
		requireNonNull(layerType);

		String id = layerType.getId().orElseThrow(Manifest.invalidId("Missing id on layer type"));

		ManifestUtils.checkId(id);

		fireEvent(new EventObject(ManifestEvents.ADD_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		synchronized (lock) {
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

		String id = layerType.getId().orElseThrow(Manifest.invalidId("Missing id on layer type"));

		ManifestUtils.checkId(id);

		fireEvent(new EventObject(ManifestEvents.REMOVE_LAYER_TYPE, "layerType", layerType)); //$NON-NLS-1$

		Optional<LayerManifest> manifest = layerType.getSharedManifest();
		if(manifest.isPresent() && isLocked(manifest.get()))
			throw new ManifestException(ManifestErrorCode.MANIFEST_LOCKED,
					"Cannot remove layer type while underlying manifest is locked");

		synchronized (lock) {
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
		synchronized (lock) {
			layerTypes.values().forEach(action);
		}
	}

	@Override
	public void addCorpusManifest(CorpusManifest manifest) {
		requireNonNull(manifest);

		String id = manifest.getId().orElseThrow(Manifest.invalidId("Missing corpus id"));

		ManifestUtils.checkId(id);

		fireEvent(new EventObject(ManifestEvents.ADD_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (lock) {
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

		String id = manifest.getId().orElseThrow(Manifest.invalidId("Missing corpus id"));

		fireEvent(new EventObject(ManifestEvents.REMOVE_CORPUS, "corpus", manifest)); //$NON-NLS-1$

		synchronized (lock) {
			if(!corpora.containsKey(id))
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
	public Optional<CorpusManifest> getCorpusManifest(String id) {
		requireNonNull(id);

		synchronized (lock) {
			return Optional.ofNullable(corpora.get(id));
		}
	}

	@Override
	public void forEachCorpus(Consumer<? super CorpusManifest> action) {
		synchronized (lock) {
			corpora.values().forEach(action);
		}
	}

	@Override
	public boolean hasTemplate(String id) {
		requireNonNull(id);

		synchronized (lock) {
			return templates.containsKey(id);
		}
	}

	@Override
	public void forEachTemplate(Consumer<? super Manifest> action) {
		synchronized (lock) {
			templates.values().forEach(action);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <M extends Manifest> Optional<M> getTemplate(String id) {
		requireNonNull(id);

		Optional<M> template;
		synchronized (lock) {
			template = Optional.ofNullable((M)templates.get(id));
		}

		// Sanity check against external modifications of the template manifest
		if(template.isPresent()) {
			String templateId = template.flatMap(Manifest::getId).orElseThrow(Manifest.invalidId("Missing template id"));

			if(!id.equals(templateId))
				throw new ManifestException(ManifestErrorCode.MANIFEST_CORRUPTED_STATE,
						"Illegal modification of template id detected. Expected "+id+" - got "+templateId); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return template;
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
				"corpus", context.getCorpusManifest().orElse(null), //$NON-NLS-1$
				"context", context)); //$NON-NLS-1$
	}

	/**
	 * Must be called under 'templates' lock!
	 *
	 * Verifies that given {@link Manifest} is a template and has a valid id
	 * and
	 */
	private void checkTemplate(Manifest template) {
		if(!template.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId().orElseThrow(Manifest.invalidId("Template does not declare valid identifier"));

		ManifestUtils.checkId(id);

		Manifest current = templates.get(id);
		if(current!=null /*&& current!=template*/)
			throw new ManifestException(ManifestErrorCode.MANIFEST_DUPLICATE_ID,
					"Template id already in use: "+id); //$NON-NLS-1$
	}

	@Override
	public void addTemplate(Manifest template) {
		requireNonNull(template);

		fireEvent(new EventObject(ManifestEvents.ADD_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (lock) {
			checkTemplate(template);
			// Above check makes sure that the template has a proper id
			templates.put(template.getId().get(), template);

			templateManifestLock.lockTemplates(template);
		}

		fireEvent(new EventObject(ManifestEvents.ADDED_TEMPLATE, "template", template)); //$NON-NLS-1$
	}

	@Override
	public void addTemplates(Collection<? extends Manifest> templates) {
		requireNonNull(templates);

		Object[] templatesArray = templates.toArray();

		fireEvent(new EventObject(ManifestEvents.ADD_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$

		synchronized (lock) {

			// First pass: check templates
			for(Manifest template : templates) {
				checkTemplate(template);
			}

			// Second pass: add templates (valid ids are ensured by above chek)
			for(Manifest template : templates) {
				this.templates.put(template.getId().get(), template);
			}

			// Third pass: update locks
			for(Manifest template : templates) {
				templateManifestLock.lockTemplates(template);
			}
		}

		fireEvent(new EventObject(ManifestEvents.ADDED_TEMPLATES, "templates", templatesArray)); //$NON-NLS-1$
	}

	@Override
	public void removeTemplate(Manifest template) {
		requireNonNull(template);

		if(!template.isTemplate())
			throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
					"Provided derivable object is not a proper template"); //$NON-NLS-1$

		String id = template.getId().orElseThrow(Manifest.invalidId("Template does not declare valid identifier"));

		ManifestUtils.checkId(id);

		if(isLocked(template))
			throw new ManifestException(ManifestErrorCode.MANIFEST_LOCKED,
					"Cannot remove template while it is locked: "+ManifestUtils.getName(template));

		fireEvent(new EventObject(ManifestEvents.REMOVE_TEMPLATE, "template", template)); //$NON-NLS-1$

		synchronized (lock) {
			if(!templates.containsKey(id))
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
	 * @see de.ims.icarus2.util.events.EventSource#addListener(de.ims.icarus2.util.events.SimpleEventListener, java.lang.String)
	 */
	@Override
	public void addListener(SimpleEventListener listener, String eventName) {
		eventSource.addListener(listener, eventName);
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
		return getLayerType(DefaultLayerTypeIds.ITEM_LAYER_OVERLAY)
				.orElseThrow(ManifestException.error(GlobalErrorCode.ILLEGAL_STATE,
						"No shared overlay layer type registered"));
	}

	public static class TemplateManifestLock {

		private final Counter<Manifest> manifestRefCounter = new Counter<>();

		private static final int UNLOCK = -1;
		private static final int LOCK = +1;

		public boolean isLocked(Manifest manifest) {
			requireNonNull(manifest);
			return manifestRefCounter.hasCount(manifest);
		}

		public void lockTemplates(Manifest manifest) {
			walkTemplate(Optional.of(manifest), false, LOCK);
		}

		public void unlockTemplates(Manifest manifest) {
			walkTemplate(Optional.of(manifest), false, UNLOCK);
		}

		private <M extends Manifest> void walkTemplate(final Optional<M> optManifest,
				final boolean checkOwn, final int delta) {
			if(!optManifest.isPresent()) {
				return;
			}

			M manifest = optManifest.get();

			// Traverse template hierarchy
			Optional<Manifest> template = manifest.tryGetTemplate();
			if(template.isPresent()) {
				if(!template.get().isTemplate())
					throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
							"Non-template manifest assigned as template: "+ManifestUtils.getName(template));

				walkTemplate(template, true, delta);
			}

			// Handle some meta types (instanceof check instead of ManifestType switch)

			// Traverse options manifest
			if(manifest instanceof MemberManifest) {
				MemberManifest memberManifest = (MemberManifest) manifest;
				walkTemplate(memberManifest.getOptionsManifest(), true, delta);
			}

			// For layer manifests lock the underlying shared manifest
			if(manifest instanceof LayerManifest) {
				LayerManifest layerManifest = (LayerManifest) manifest;
				Optional<LayerType> layerType = layerManifest.getLayerType();
				if(layerType.isPresent()) {
					walkTemplate(layerType.get().getSharedManifest(), true, delta);
				}
			}

			// Now do some manifest type sensitive handling
			switch (manifest.getManifestType()) {

			case CORPUS_MANIFEST: {
				CorpusManifest corpusManifest = (CorpusManifest) manifest;
				corpusManifest.forEachContextManifest(c -> walkTemplate(Optional.of(c), true, delta));
			} break;

			case CONTEXT_MANIFEST: {
				ContextManifest contextManifest = (ContextManifest) manifest;
				contextManifest.forEachLayerManifest(l -> walkTemplate(Optional.of(l), true, delta));
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
