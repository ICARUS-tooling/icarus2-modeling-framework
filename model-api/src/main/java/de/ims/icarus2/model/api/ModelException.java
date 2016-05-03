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

 * $Revision: 392 $
 * $Date: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/ModelException.java $
 *
 * $LastChangedDate: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $LastChangedRevision: 392 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.model.api.corpus.Corpus;

/**
 * Defines an exception in the context of the model framework that is associated with
 * a {@link ModelErrorCode} and optionally has a live {@link Corpus} instance linked to it.
 * Note, however, that the corpus is only linked via a weak reference so that temporarily
 * kept instances of this exception type (e.g. for logging purposes) cannot prevent the
 * corpus in question from getting closed and {@code gc}ed.
 *
 * @author Markus Gärtner
 * @version $Id: ModelException.java 392 2015-04-29 10:56:11Z mcgaerty $
 *
 */
public class ModelException extends IcarusException {

	/**
	 * Extracts and returns the {@link Throwable#getCause() cause} of the given {@code Throwable}
	 * as a {@code ModelException}. If the cause is not assignment compatible then it will be wrapped
	 * in a new {@code ModelException} instance with a {@link ModelErrorCode#UNKNOWN_ERROR unknown}
	 * error type.
	 *
	 * @param t
	 * @return
	 */
	public static ModelException unwrap(Throwable t) {
		Throwable cause = t.getCause();
		if(cause instanceof ModelException) {
			return (ModelException) cause;
		} else {
			return new ModelException(GlobalErrorCode.UNKNOWN_ERROR, "Unexpected foreign exception", cause);
		}
	}

	private static final long serialVersionUID = -3508678907020081630L;

	private final Reference<Corpus> source;
	private final boolean corpusSet;

	public ModelException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);

		this.source = null;
		this.corpusSet = false;
	}

	public ModelException(ErrorCode error, String message) {
		this(error, message, null);
	}

	public ModelException(String message) {
		this(GlobalErrorCode.UNKNOWN_ERROR, message, null);
	}

	public ModelException(Corpus corpus, ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);

		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.source = new WeakReference<>(corpus);
		this.corpusSet = true;
	}

	public ModelException(Corpus corpus, ErrorCode errorCode, String message) {
		this(corpus, errorCode, message, null);
	}

	public ModelException(Corpus corpus, String message) {
		this(corpus, GlobalErrorCode.UNKNOWN_ERROR, message, null);
	}

	/**
	 * @return the corpus this exception is bound to
	 */
	public Corpus getCorpus() {
		return corpusSet ? source.get() : null;
	}

	public boolean isCorpusSet() {
		return corpusSet;
	}
}
