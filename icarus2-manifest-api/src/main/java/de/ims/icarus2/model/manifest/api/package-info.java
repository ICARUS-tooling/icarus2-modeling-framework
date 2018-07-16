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
/**
 * Defines all the high level members of the ICARUS2 manifest framework.
 * <p>
 * Manifests are descriptive objects that define the composition of a "real"
 * corpus or its individual parts. Their purpose is to decouple representation
 * (read as implementation) of corpus elements from their respective linguistic
 * semantics.
 * This way it is for example possible to model all types of tree like structures
 * ever to occur in corpora with the exact same collection of data structures, while
 * carrying information about their individual meaning (dependency syntax tree,
 * discourse tree, etc...) in the accompanying manifest objects.
 * <p>
 * Manifests can be defined directly by instantiating the desired java implementations
 * or externally via XML, JSON or other supported serialization formats.
 * The {@code de.ims.icarus2.model.manifest.xml} package defines a default XML
 * serialization scheme for all members of the manifest framework.
 * <p>
 * Since manifests in general support templating and therefore inheritance, there are a
 * few important guidelines to consider when implementing their interfaces or even
 * when using them programmatically:
 *
 * Whenever a manifest defines an <i>internal iterator</i> method that takes a {@link java.util.function.Consumer}
 * instance, like {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachOption(java.util.function.Consumer)},
 * it should obey the following rule: To ensure consistency in the case of templating the implementation should first
 * forward the iteration to its template, if defined, and only then perform the iteration on locally declared data.
 *
 * Usually a manifest interface that can be subject to templating defines a derivation of each such iterator method
 * with a restriction to only access local data
 * (e.g. {@link de.ims.icarus2.model.manifest.api.OptionsManifest#forEachLocalOption(java.util.function.Consumer)} for
 * the method mentioned initially).
 */
package de.ims.icarus2.model.manifest.api;

