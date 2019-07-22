/**
 * Utility classes for providing an abstraction layer between physical
 * storage and the usual "file-based" operations.
 * <p>
 * Almost all components
 * of the ICARUS2 framework use this facility instead of {@link java.io.File}
 * or {@link java.nio.file.Path} methods to increase flexibility and
 * improve testability. In addition this directly enables those components
 * to be used in a purely virtual setup, i.e. without actual interaction
 * with the local file system.
 *
 * @author Markus Gärtner
 *
 */
package de.ims.icarus2.util.io.resource;