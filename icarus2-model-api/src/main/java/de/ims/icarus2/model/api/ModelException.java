/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api;

import static java.util.Objects.requireNonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.model.api.corpus.Corpus;

/**
 * Defines an exception in the context of the model framework that is associated with
 * a {@link ModelErrorCode} and optionally has a live {@link Corpus} instance linked to it.
 * Note, however, that the corpus is only linked via a weak reference so that temporarily
 * kept instances of this exception type (e.g. for logging purposes) cannot prevent the
 * corpus in question from getting closed and {@code gc}ed.
 *
 * @author Markus Gärtner
 *
 */
public class ModelException extends IcarusRuntimeException {

	public static Supplier<ModelException> create(ErrorCode errorCode, String message) {
		return () -> new ModelException(errorCode, message);
	}

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
		}

		return new ModelException(GlobalErrorCode.UNKNOWN_ERROR, "Unexpected foreign exception", cause);
	}

	private static final long serialVersionUID = -3508678907020081630L;

	private final Reference<Corpus> source;
	private final boolean corpusSet;

	public ModelException(ErrorCode errorCode, String message, @Nullable Throwable cause) {
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

		requireNonNull(corpus);

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
