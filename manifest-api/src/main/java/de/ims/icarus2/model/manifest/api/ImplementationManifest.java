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

 * $Revision: 445 $
 * $Date: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/ImplementationManifest.java $
 *
 * $LastChangedDate: 2016-01-11 17:33:05 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 445 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.manifest.api;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;
import de.ims.icarus2.model.util.StringResource;



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
 * @version $Id: ImplementationManifest.java 445 2016-01-11 16:33:05Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface ImplementationManifest extends MemberManifest {

	public static final boolean DEFAULT_USE_FACTORY_VALUE = false;
	public static final SourceType DEFAULT_SOURCE_TYPE = SourceType.DEFAULT;

	/**
	 * Returns the surrounding 'host' manifest.
	 * @return
	 */
	MemberManifest getHostManifest();

	@Override
	default public ManifestFragment getHost() {
		return getHostManifest();
	};

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
	SourceType getSourceType();

	/**
	 * Returns a string describing the source of the implementation. The meaning
	 * of this {@code source} parameter is depending on the {@link SourceType} of
	 * this manifest and in some cases might even be optional.
	 *
	 * @return
	 * @see SourceType
	 */
	@AccessRestriction(AccessMode.READ)
	String getSource();

	/**
	 * Returns a string describing the actual class name of the implementation.
	 * The meaning of this {@code classname} parameter is depending on the
	 * {@link SourceType} of this manifest and in some cases might even be optional.
	 *
	 * @return
	 * @see SourceType
	 */
	@AccessRestriction(AccessMode.READ)
	String getClassname();

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

	// Modification methods

	void setSourceType(SourceType sourceType);

	void setSource(String source);

	void setClassname(String classname);

	void setUseFactory(Boolean useFactory);

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
		 * @see de.ims.icarus2.model.util.StringResource#getStringValue()
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
	 *
	 * @author Markus Gärtner
	 * @version $Id: ImplementationManifest.java 445 2016-01-11 16:33:05Z mcgaerty $
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
		 * @throws ClassNotFoundException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 * @throws ClassCastException
		 */
		<T extends Object> T create(Class<T> resultClass, ImplementationManifest manifest, ImplementationLoader<?> environment) throws
				ClassNotFoundException, IllegalAccessException, InstantiationException, ClassCastException;

	}
}
