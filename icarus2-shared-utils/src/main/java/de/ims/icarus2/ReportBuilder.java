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
package de.ims.icarus2;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.Report.ReportItemCollector;
import de.ims.icarus2.Report.Severity;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 *
 */
public class ReportBuilder<R extends ReportItem> extends AbstractBuilder<ReportBuilder<R>, Report<R>>
		implements ReportItemCollector {

	private final List<R> items = new LinkedList<>();
	private int errorCount;
	private int warningCount;
	private Identity source;

	public static <R extends ReportItem> ReportBuilder<R> builder() {
		return new ReportBuilder<>();
	}

	public static <R extends ReportItem> ReportBuilder<R> builder(Identity source) {
		ReportBuilder<R> builder = new ReportBuilder<>();
		return builder.source(source);
	}

	private ReportBuilder() {
		// no-op
	}

	public ReportBuilder<R> source(Identity source) {
		requireNonNull(source);
		checkState("Source already set", this.source==null);

		this.source = source;

		return thisAsCast();
	}

	public ReportBuilder<R> addItem(R item) {
		requireNonNull(item);

		items.add(item);

		refreshCounts(item);

		return thisAsCast();
	}

	private void refreshCounts(R item) {
		switch (item.getSeverity()) {
		case ERROR:
			errorCount++;
			break;

		case WARNING:
			warningCount++;
			break;

		default:
			break;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addItem(Severity severity, Identity source, ErrorCode code, String message, Object...data) {
		addItem((R) new ReportItemImpl(LocalDateTime .now(), severity, source, code, message, data));
	}

	public ReportBuilder<R> addItems(Collection<? extends R> items) {
		requireNonNull(items);
		checkArgument("Empty collection of report items", !items.isEmpty());

		this.items.addAll(items);

		items.forEach(this::refreshCounts);

		return thisAsCast();
	}

	public ReportBuilder<R> addReport(Report<? extends R> report) {
		requireNonNull(report);

		this.items.addAll(report.getItems());

		errorCount += report.countErrors();
		warningCount += report.countWarnings();

		return thisAsCast();
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public Identity getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus2.util.AbstractBuilder#create()
	 */
	@Override
	protected Report<R> create() {
		if(source==null && items.isEmpty()) {
			return Report.emptyReport();
		}

		return new ReportImpl<>(this);
	}

	static class ReportImpl<R extends ReportItem> implements Report<R> {

		private final List<R> items;
		private final int errorCount;
		private final int warningCount;
		private final Identity source;

		ReportImpl(ReportBuilder<? extends R> builder) {
			items = new ArrayList<>(builder.items); //defensive copying
			errorCount = builder.errorCount;
			warningCount = builder.warningCount;
			source = builder.source;
		}

		/**
		 * @see de.ims.icarus2.Report#getSource()
		 */
		@Override
		public Identity getSource() {
			return source;
		}

		/**
		 * @see de.ims.icarus2.Report#countErrors()
		 */
		@Override
		public int countErrors() {
			return errorCount;
		}

		/**
		 * @see de.ims.icarus2.Report#countWarnings()
		 */
		@Override
		public int countWarnings() {
			return warningCount;
		}

		/**
		 * @see de.ims.icarus2.Report#getItems()
		 */
		@Override
		public Collection<R> getItems() {
			return items;
		}

		/**
		 * @see de.ims.icarus2.Report#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return items.isEmpty();
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return toString(null);
		}
	}

	public static class ReportItemImpl implements ReportItem {

		private final Severity severity;
		private final Identity source;
		private final ErrorCode code;
		private final String message;
		private final Object[] data;

		private final LocalDateTime timestamp;

		public ReportItemImpl(LocalDateTime timestamp, Severity severity, Identity source,
				ErrorCode code, String message, Object...data) {

			requireNonNull(severity);
			requireNonNull(code);
			requireNonNull(message);

			if(timestamp==null) {
				timestamp = LocalDateTime.now();
			}

			this.timestamp = timestamp;
			this.severity = severity;
			this.source = source;
			this.code = code;
			this.message = message;
			this.data = data;
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getSeverity()
		 */
		@Override
		public Severity getSeverity() {
			return severity;
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getSource()
		 */
		@Override
		public Identity getSource() {
			return source;
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getCode()
		 */
		@Override
		public ErrorCode getCode() {
			return code;
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getMessage()
		 */
		@Override
		public String getMessage() {
			return StringUtil.format(message, data);
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getTimestamp()
		 */
		@Override
		public LocalDateTime getTimestamp() {
			return timestamp;
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getMessage(java.util.Locale)
		 */
		@Override
		public String getMessage(Locale locale) {
			return StringUtil.format(locale, message, data);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			appendTo(sb);
			return sb.toString();
		}
	}
}
