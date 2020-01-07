/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import javax.xml.stream.XMLStreamException;

import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.util.xml.XmlSerializer;


/**
 * @author Markus Gärtner
 *
 */
public interface ManifestXmlDelegate<M extends TypedManifest> extends ManifestXmlHandler {

	//TODO factory method for creating exceptions (SAXException ??)

	/**
	 * Intended for use in constructors, this method should check
	 * for the internal reference to the manifest framework member to be {@code null}
	 * and change it to the provided one if it is so. In case it is non-{@code null}
	 * an exception is thrown.
	 *
	 * @throws IllegalStateException if the internal reference to the
	 * manifest framework member has already been set.
	 */
	void setInstance(M instance);

	/**
	 * Returns the current member of the manifest framework assigned to this delegate
	 * or throws an exception if no such member is set.
	 *
	 * @return
	 */
	M getInstance();

	/**
	 * Erases any internal state and resets the reference to the manifest framework member
	 * managed by this delegate back to {@code null}. This method is mainly intended for
	 * delegates of commonly used primitive framework members.
	 */
	void reset();

	/**
	 * {@link #reset() Reset} this delegate and then {@link #setInstance(Object) change} the
	 * currently used instance to the supplied {@code instance} parameter.
	 * @param instance
	 * @return this delegate
	 */
	default ManifestXmlDelegate<M> reset(M instance) {
		reset();
		setInstance(instance);

		return this;
	}

	/**
	 * Serialize the currently set member of the manifest framework using the provided
	 * {@code serializer}. All checked exceptions encountered will be wrapped into
	 * {@link XMLStreamException}.
	 *
	 * @param serializer
	 * @throws XMLStreamException
	 */
	void writeXml(XmlSerializer serializer) throws XMLStreamException;
}
