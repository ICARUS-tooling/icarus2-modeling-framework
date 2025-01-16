/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.query.api.QueryException;
import de.ims.icarus2.query.api.engine.result.Match;
import de.ims.icarus2.query.api.engine.result.PayloadReader;
import de.ims.icarus2.query.api.engine.result.ResultEntry;
import de.ims.icarus2.query.api.engine.result.ResultSink;
import de.ims.icarus2.util.io.resource.IOResource;

/**
 * Implements a {@link ResultSink} that streams a simple textual representation of each
 * match to a given {@link PrintWriter}, one match per line.
 *
 * @author Markus Gärtner
 *
 */
public class IdPrintingResultSink implements ResultSink {

	private final MatchCodec.MatchWriter writer;

	private boolean headerPrinted = false;

	/** Uses {@link TabularMatchCodec} for writing. */
	public IdPrintingResultSink(Writer writer) {
		this.writer = new TabularMatchCodec.WriterImpl(writer);
	}

	public IdPrintingResultSink(IOResource resource, MatchCodec codec) throws IOException {
		writer = codec.newWriter(resource);
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#prepare()
	 */
	@Override
	public void prepare() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#prepare(int)
	 */
	@Override
	public void prepare(int size) {
		prepare();
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#add(de.ims.icarus2.query.api.engine.result.Match)
	 */
	@Override
	public void add(Match match) {
		if(!headerPrinted) {
			//TODO print column headers first?
		}

		try {
			writer.write(match);
		} catch (IOException e) {
			throw new QueryException(GlobalErrorCode.IO_ERROR, "Failed to write match", e);
		}
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#add(de.ims.icarus2.query.api.engine.result.ResultEntry, de.ims.icarus2.query.api.engine.result.PayloadReader)
	 */
	@Override
	public void add(ResultEntry entry, PayloadReader payloadReader) {
		// We don't output payload information!
		add(entry.getMatch());
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#discard()
	 */
	@Override
	public void discard() throws InterruptedException {
		try {
			finish();
		} catch (InterruptedException e) {
			// no-op
		}
	}

	/**
	 * @see de.ims.icarus2.query.api.engine.result.ResultSink#finish()
	 */
	@Override
	public void finish() throws InterruptedException {
		try {
			writer.close();
		} catch (Exception e) {
			throw new QueryException(GlobalErrorCode.IO_ERROR, "Failed to close writer", e);
		}
	}

}
