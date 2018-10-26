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
package de.ims.icarus2.model.manifest.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.logging.Level;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus2.model.manifest.api.ManifestLocation.VirtualManifestOutputLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.xml.ManifestXmlReader.RootHandlerProxy;
import de.ims.icarus2.test.DiffUtils;
import de.ims.icarus2.test.DiffUtils.Trace;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.lang.ClassUtils;
import de.ims.icarus2.util.xml.XmlSerializer;


/**
 * @author Markus Gärtner
 * @version $Id: ManifestXmlTestUtils.java 288 2014-08-13 11:38:45Z mcgaerty $
 *
 */
public class ManifestXmlTestUtils {


	/**
	 * Heavyweight assertion method. The given {@code Manifest} is first serialized via
	 * a freshly created {@link ManifestXmlWriter}, using a virtual {@link ManifestLocation}
	 * that stores the result of an xml serialization as a string. Then another virtual
	 * {@code ManifestLocation} is created from the xml string, this time to be used with
	 * an instance of {@link ManifestXmlReader} to deserialize the manifest. In case the
	 * reader's log contains entries with a log level of {@link Level#WARNING} or higher,
	 * the assertion will fail.
	 * <p>
	 * The original manifest and the result of the aforementioned processing chain are then
	 * compared in depth via the {@link ClassUtils#deepDiff(Object, Object)} method. In case
	 * the resulting {@link Trace  object contains any message entries that indicate differences
	 * between the two objects, an {@code AssertionError} is thrown.
	 *
	 * @param manifest
	 * @throws Exception
	 */
	public static void assertSerializationEquals(String msg, Manifest manifest) throws Exception {
		ClassLoader classLoader = manifest.getClass().getClassLoader();
		boolean isTemplate = manifest.isTemplate();

		VirtualManifestOutputLocation outputLocation = new VirtualManifestOutputLocation(classLoader, isTemplate);
		ManifestXmlWriter writer = new ManifestXmlWriter(outputLocation);

		writer.addManifest(manifest);

		writer.writeAll();

		String xml = outputLocation.getContent();

//		System.out.println(xml);

		VirtualManifestInputLocation inputLocation = new VirtualManifestInputLocation(xml, classLoader, isTemplate);

		ManifestRegistry registry = manifest.getRegistry();

		ManifestXmlReader reader = ManifestXmlReader.newBuilder().registry(registry).useImplementationDefaults().build();

		reader.addSource(inputLocation);

		List<Manifest> manifests;
		if(isTemplate) {
			manifests = reader.parseTemplates();
		} else {
			manifests = reader.parseCorpora();
		}

		assertEquals(1, manifests.size());
		Manifest newManifest = manifests.get(0);

		Trace trace = DiffUtils.deepDiff(manifest, newManifest);

		if(trace.hasMessages()) {
			failForTrace(msg, trace, manifest, xml);
		}
	}

	public static <M extends Object> void assertSerializationEquals(String msg, M instance, M newInstance, ManifestXmlDelegate<? super M> delegate) throws Exception {

		ClassLoader classLoader = instance.getClass().getClassLoader();
		boolean isTemplate = false;

		VirtualManifestOutputLocation outputLocation = new VirtualManifestOutputLocation(classLoader, isTemplate);
		XmlSerializer serializer = ManifestXmlWriter.defaultCreateSerializer(outputLocation.getOutput());

		delegate.reset(instance);

		delegate.writeXml(serializer);

		String xml = outputLocation.getContent();

//		System.out.println(xml);

		VirtualManifestInputLocation inputLocation = new VirtualManifestInputLocation(xml, classLoader, isTemplate);

		// Reset delegate so we write into the correct target object
		delegate.reset(newInstance);

		/*
		 *  Assume we shouldn't use validation for most "low level" components,
		 *  since they will be properly validated when used in a wider context
		 *  as part of higher components!
		 */
		boolean validate = false;

		/*
		 *  Special case: if we're testing a typed manifest and it actually
		 *  allows templating (i.e. top-level standalone mode), then we might
		 *  as well use validating parser mode here.
		 */
		if(instance instanceof TypedManifest) {
			ManifestType type = ((TypedManifest) instance).getManifestType();
			validate = type.isSupportTemplating();
		}

		// BEGIN XML boilerplate stuff
		RootHandlerProxy proxy = new RootHandlerProxy(inputLocation, delegate);
		XMLReader reader = ManifestXmlReader.defaultCreateReader(validate);

		InputSource inputSource = new InputSource();

		reader.setContentHandler(proxy);
		reader.setErrorHandler(proxy);
		reader.setEntityResolver(proxy);
		reader.setDTDHandler(proxy);

		inputSource.setCharacterStream(inputLocation.getInput());

		reader.parse(inputSource);
		// END XML boilerplate stuff

		Trace trace = DiffUtils.deepDiff(instance, newInstance);

		if(trace.hasMessages()) {
			failForTrace(msg, trace, instance, xml);
		}
	}

	public static void assertSerializationEquals(Manifest manifest) throws Exception {
		assertSerializationEquals(null, manifest);
	}

	private static String getId(Object manifest) {
		String id = null;

		if(manifest instanceof Manifest) {
			id = ((Manifest)manifest).getId().orElse(null);
		} else if(manifest instanceof Identity) {
			id = ((Identity)manifest).getId().orElse(null);
		}

		if(id==null) {
			id = manifest.getClass()+"@<unnamed>"; //$NON-NLS-1$
		}
		return id;
	}

	private static void failForXml(String msg, String xml, Manifest manifest) {
		String message = getId(manifest);

		message += "  deserialization failed: \n"; //$NON-NLS-1$
		message += xml;

		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		fail(message);
	}

	private static void failForEqual(String msg, Object original, Object created) {
		String message = "Expected result of deserialization to be different from original"; //$NON-NLS-1$
		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}
		fail(message);
	}

	private static void failForTrace(String msg, Trace trace, Object manifest, String xml) {
		String message = getId(manifest);

		message += " result of deserialization is different from original: \n"; //$NON-NLS-1$
		message += trace.getMessages();
		message += " {serialized form: "+xml.replaceAll("\\s{2,}", "")+"}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		fail(message);
	}
}
