/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.manifest.api;

import java.util.Optional;

import de.ims.icarus2.util.access.AccessControl;
import de.ims.icarus2.util.access.AccessMode;
import de.ims.icarus2.util.access.AccessPolicy;
import de.ims.icarus2.util.access.AccessRestriction;
import de.ims.icarus2.util.strings.StringResource;



/**
 * Models the ability to describe foreign implementations for an interface.
 * Such implementations can originate from almost everywhere and are not limited
 * to previously registered plugin extensions. Essentially the location or origin
 * of the actual implementation in question is controlled by the {@code source}
 * and {@code classname} parameters. How to resolve the class on the other hand
 * is specified solely by the {@link SourceType} of this manifest.
 *
 * With the help of the optional {@code OptionsManifest}
 * within an implementation manifest the user can customize the implementation according
 * to his needs and the amount of modifiable properties.
 *
 * @author Markus Gärtner
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ImplementationManifest extends MemberManifest<ImplementationManifest>, Embedded {

	public static final boolean DEFAULT_USE_FACTORY_VALUE = false;
	public static final SourceType DEFAULT_SOURCE_TYPE = SourceType.DEFAULT;

	/**
	 * @see de.ims.icarus2.model.manifest.api.Manifest#getManifestType()
	 */
	@Override
	default ManifestType getManifestType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}

	/**
	 * Returns the surrounding 'host' manifest.
	 * @return
	 */
	default <M extends MemberManifest<M>> Optional<M> getHostManifest() {
		return getHost();
	}

	/**
	 * Returns the type of this implementation's source, defining
	 * how to interpret the {@code classname} and {@code source}
	 * parameters.
	 *
	 * @return
	 *
	 * @see #DEFAULT_SOURCE_TYPE
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<SourceType> getSourceType();

	boolean isLocalSourceType();

	/**
	 * Returns a string describing the source of the implementation. The meaning
	 * of this {@code source} parameter is depending on the {@link SourceType} of
	 * this manifest and in some cases might even be optional.
	 *
	 * @return
	 * @see SourceType
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getSource();

	boolean isLocalSource();

	/**
	 * Returns a string describing the actual class name of the implementation.
	 * The meaning of this {@code classname} parameter is depending on the
	 * {@link SourceType} of this manifest and in some cases might even be optional.
	 *
	 * @return
	 * @see SourceType
	 */
	@AccessRestriction(AccessMode.READ)
	Optional<String> getClassname();

	boolean isLocalClassname();

	/**
	 * Returns whether or not the targeted class is considered to be
	 * a direct implementation, that is to be instantiated using a no-args
	 * constructor. A return value of {@code true} indicates that the
	 * class must implement {@link Factory} and is able to handle customization
	 * in the form of properties assigned to a {@link ModifiableManifest}.
	 * Note that the factory gets told the type its result is expected to compatible with.
	 * It is the responsibility of the factory to run a final assignment check
	 * before returning the instantiated implementation, or, better yet, to perform
	 * such a check beforehand to prevent potentially unnecessary resource allocation.
	 * <p>
	 * Default is {@code false}.
	 *
	 * @return
	 *
	 * @see #DEFAULT_USE_FACTORY_VALUE
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isUseFactory();

	boolean isLocalUseFactory();

	// Modification methods

	ImplementationManifest setSourceType(SourceType sourceType);

	ImplementationManifest setSource(String source);

	ImplementationManifest setClassname(String classname);

	ImplementationManifest setUseFactory(boolean useFactory);

	public enum SourceType implements StringResource {

		/**
		 * Source is a globally unique extension uid in the form:<br>
		 * &lt;plugin-uid&gt;@&lt;extension-id&gt;
		 * <p>
		 * Note that in this case the {@code classname} parameter is optional
		 * (and in fact it would be redundant, since the extension in
		 * question is already required to contain a {@code class} parameter!
		 */
		EXTENSION,

		/**
		 * Source is the globally unique identifier of a plugin, used to
		 * fetch the class loader which has access to the implementation.
		 * The {@code classname} parameter defines the fully qualified name of the
		 * implementing class.
		 */
		PLUGIN,

		/**
		 * Since for simple additions creation of an entire plugin could easily
		 * be considered overkill, there is the option to provide the class file
		 * of an implementation in the {@code external} folder. The source would then
		 * be the file name. Per convention, if the referenced file is a mere class
		 * file, its name must equal the fully qualified name of the class contained.
		 * If the file is a jar archive, an additional class name must be specified,
		 * otherwise the jar's manifest file will be accessed to check for a main
		 * class declaration.
		 */
		EXTERN,

		/**
		 * When the target class is accessible via the class loader that loaded the
		 * model plugin, the only thing required is the {@code classname} parameter
		 * (the {@code source} is not needed any more!).
		 */
		DEFAULT;

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return name().toLowerCase();
		}

		public static SourceType parseSourceType(String s) {
			return valueOf(s.toUpperCase());
		}
	}

	/**
	 * Helper interface to delegate instantiation of new objects to a dedicated factory.
	 * Unlike the default instantiation process, the factory way gives access to the surrounding
	 * {@link ImplementationManifest manifest} and the active {@link ImplementationLoader loader}.
	 * In cases where the actual implementation of an object described by an {@link ImplementationManifest}
	 * relies on a builder as the construction process, there typically is no no-args constructor
	 * available and a dedicated factory implementation is required to wrap the builder process.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface Factory {

		/**
		 * Creates a new instance of whatever class this factory is managing and ensures assignment
		 * compatibility with the given {@code resultClass}.
		 *
		 * @param resultClass class the created object must be compatible with
		 * @param manifest the manifest describing the implementation
		 * @param environment the currently active loader that manages the loading process and which can
		 * provide additional information about the environment or corpus being used.
		 * @return
		 * @throws ClassNotFoundException if this factory is unable to locate and/or load the desired implementation
		 * @throws IllegalAccessException if this factory has no access to a suitable constructor of the target implementation
		 * @throws InstantiationException if invoking the desired constructor failed
		 * @throws ClassCastException if the specification in the supplied {@link ImplementationManifest}
		 * yields an incompatible object or the given {@code resultClass} is not compatible with the
		 * basic class and/or interface implied by the context.
		 */
		<T extends Object> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment) throws
				ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException;

	}
}
