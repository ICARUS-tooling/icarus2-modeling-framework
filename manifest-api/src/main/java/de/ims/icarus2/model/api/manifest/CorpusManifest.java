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

 * $Revision: 443 $
 * $Date: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/manifest/CorpusManifest.java $
 *
 * $LastChangedDate: 2016-01-11 12:31:11 +0100 (Mo, 11 Jan 2016) $
 * $LastChangedRevision: 443 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.manifest;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import de.ims.icarus2.model.api.access.AccessControl;
import de.ims.icarus2.model.api.access.AccessMode;
import de.ims.icarus2.model.api.access.AccessPolicy;
import de.ims.icarus2.model.api.access.AccessRestriction;
import de.ims.icarus2.util.collections.LazyCollection;

/**
 * Note: A corpus manifest can only appear in non-template parsing context!
 *
 * @author Markus Gärtner
 * @version $Id: CorpusManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface CorpusManifest extends MemberManifest {

	public static final boolean DEFAULT_EDITABLE_VALUE = false;

	@AccessRestriction(AccessMode.READ)
	ContextManifest getRootContextManifest();

	@AccessRestriction(AccessMode.READ)
	void forEachCustomContextManifest(Consumer<? super ContextManifest> action);

	/**
	 * Returns all the {@link ContextManifest context manifests} hosted in this corpus
	 * including the one for the {@link #getRootContextManifest() root context}.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<ContextManifest> getCustomContextManifests() {
		LazyCollection<ContextManifest> result = LazyCollection.lazyList();

		result.add(getRootContextManifest());

		forEachCustomContextManifest(result);

		return result.getAsList();
	}

	@Override
	default public ManifestFragment getHost() {
		return null;
	};

	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest(String id);

	/**
	 * Returns {@code true} if the corpus described by this manifest can
	 * be edited by the user.
	 *
	 * @return
	 */
	boolean isEditable();

	@AccessRestriction(AccessMode.READ)
	void forEachNote(Consumer<? super Note> action);

	/**
	 * Returns the notes added to this corpus. The order is not specified and
	 * may be random. However, most times it will be convenient to have the
	 * notes sorted in lexicographical order of their titles or in chronological
	 * order according to the dates of their last individual modifications.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	default List<Note> getNotes() {
		LazyCollection<Note> result = LazyCollection.lazyList();

		forEachNote(result);

		return result.getAsList();
	}

	// Modification methods

	void setRootContextId(String rootContextId);

	void addCustomContextManifest(ContextManifest manifest);

	void removeCustomContextManifest(ContextManifest manifest);

	/**
	 *
	 * @param note
	 *
	 * @throws NullPointerException if the {@code note} argument is {@code null}
	 * @throws IllegalArgumentException if the content of the given {@code note}
	 * 			exceeds the {@link Note#MAX_CHARACTER_LIMIT} limit
	 */
	void addNote(Note note);
	void removeNote(Note note);

	/**
	 *
	 * @param value
	 */
	void setEditable(boolean value);

	/**
	 * Notes are user made textual additions that are saved together with the corpus manifest.
	 * They allow the storage of information outside predefined options and/or properties and
	 * can hold arbitrary text, but are limited to 10.000 characters. Each note is given a name
	 * that serves as a title for the content text. Those names do not have to be unique, however,
	 * they have to be non-empty.
	 *
	 * @author Markus Gärtner
	 * @version $Id: CorpusManifest.java 443 2016-01-11 11:31:11Z mcgaerty $
	 *
	 */
	public interface Note {

		/**
		 * The maximum allowed number of characters a single note object can hold
		 */
		public static final int MAX_CHARACTER_LIMIT = 10_000;

		/**
		 * Returns the date of the last modification. If there have not been any modifications since
		 * the note was created, this method will return the date of the note's creation.
		 * @return
		 */
		Date getModificationDate();

		/**
		 * Returns the title of this note. The returned {@code String} is always non-null and never empty.
		 * @return
		 */
		String getName();

		/**
		 * Returns the (potentially) empty content of this note.
		 * @return
		 */
		String getContent();

		// Modification methods

		/**
		 * Changes the textual content of the note and updates its {@link #getModificationDate() modification date}
		 * so that it shows the current timestamp.
		 *
		 * @param content
		 */
		void setContent(String content);
	}
}
