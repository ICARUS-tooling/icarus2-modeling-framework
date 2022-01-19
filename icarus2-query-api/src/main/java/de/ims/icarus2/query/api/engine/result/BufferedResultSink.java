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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.List;

import de.ims.icarus2.query.api.engine.EngineSettings;
import de.ims.icarus2.query.api.engine.EngineSettings.IntField;
import de.ims.icarus2.util.collections.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
public class BufferedResultSink implements ResultSink {

	private List<Match> matches;
	private final EngineSettings settings;
	private final Object lock = new Object();
	private boolean open = false;

	public BufferedResultSink(EngineSettings settings) {
		this.settings = requireNonNull(settings);
	}

	private void checkOpen() {
		checkState("Sink not opened yet or already closed", open);
	}

	@Override
	public void prepare() {
		synchronized (lock) {
			matches = new ObjectArrayList<>(settings.getInt(IntField.INITIAL_MAIN_BUFFER_SIZE));
			open = true;
		}
	}

	@Override
	public void prepare(int size) {
		synchronized (lock) {
			matches = new ObjectArrayList<>(size);
			open = true;
		}
	}

	@Override
	public void add(Match match) {
		synchronized (lock) {
			checkOpen();
			matches.add(match);
		}
	}

	@Override
	public void add(ResultEntry entry, PayloadReader payloadReader) {
		add(entry.getMatch());
	}

	@Override
	public void discard() throws InterruptedException {
		synchronized (lock) {
			matches.clear();
			open = false;
		}
	}

	@Override
	public void finish() throws InterruptedException {
		synchronized (lock) {
			open = false;
		}
	}

	public List<Match> getMatches() {
		return CollectionUtils.unmodifiableListProxy(matches);
	}
}
