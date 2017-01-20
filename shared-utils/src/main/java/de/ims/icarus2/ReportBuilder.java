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

	public static <R extends ReportItem> ReportBuilder<R> newBuilder() {
		return new ReportBuilder<>();
	}

	public static <R extends ReportItem> ReportBuilder<R> newBuilder(Identity source) {
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

	/**
	 * @see de.ims.icarus2.util.AbstractBuilder#create()
	 */
	@Override
	protected Report<R> create() {
		if(source==null && items.isEmpty()) {
			return Report.emptyReport();
		} else {
			return new ReportImpl<>(this);
		}
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
