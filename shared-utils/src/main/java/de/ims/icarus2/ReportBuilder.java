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
import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.Conditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.Report.Severity;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.id.Identity;

/**
 * @author Markus Gärtner
 *
 */
public class ReportBuilder<R extends ReportItem> extends AbstractBuilder<ReportBuilder<R>, Report<R>> {

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
		checkNotNull(source);
		checkState("Source already set", this.source==null);

		this.source = source;

		return thisAsCast();
	}

	public ReportBuilder<R> addItem(R item) {
		checkNotNull(item);

		items.add(item);

		switch (item.getSeverity()) {
		case ERROR:
			errorCount++;
			break;

		case WARNING:
			warningCount++;

		default:
			break;
		}

		return thisAsCast();
	}

	@SuppressWarnings("unchecked")
	public ReportBuilder<R> addItem(Severity severity, Identity source, ErrorCode code, String message, Object...data) {
		return addItem((R) new ReportItemImpl(severity, source, code, message, data));
	}

	public ReportBuilder<R> addItem(Severity severity, ErrorCode code, String message) {
		return addItem(severity, null, code, message, (Object[])null);
	}

	public ReportBuilder<R> addError(ErrorCode code, String message) {
		return addItem(Severity.ERROR, null, code, message, (Object[])null);
	}

	public ReportBuilder<R> addError(ErrorCode code, String message, Object...data) {
		return addItem(Severity.ERROR, null, code, message, data);
	}

	public ReportBuilder<R> addWarning(ErrorCode code, String message) {
		return addItem(Severity.WARNING, null, code, message, (Object[])null);
	}

	public ReportBuilder<R> addWarning(ErrorCode code, String message, Object...data) {
		return addItem(Severity.WARNING, null, code, message, data);
	}

	public ReportBuilder<R> addInfo(ErrorCode code, String message) {
		return addItem(Severity.INFO, null, code, message, (Object[])null);
	}

	public ReportBuilder<R> addInfo(ErrorCode code, String message, Object...data) {
		return addItem(Severity.INFO, null, code, message, data);
	}

	public ReportBuilder<R> addInfo(String message) {
		return addItem(Severity.INFO, null, GlobalErrorCode.INFO, message, (Object[])null);
	}

	public ReportBuilder<R> addInfo(String message, Object...data) {
		return addItem(Severity.INFO, null, GlobalErrorCode.INFO, message, data);
	}

	public ReportBuilder<R> addItems(Collection<? extends R> items) {
		checkNotNull(items);
		checkArgument("Empty collection of report items", !items.isEmpty());

		this.items.addAll(items);

		for(R item : items) {
			switch (item.getSeverity()) {
			case ERROR:
				errorCount++;
				break;

			case WARNING:
				warningCount++;

			default:
				break;
			}
		}

		return thisAsCast();
	}

	public ReportBuilder<R> addReport(Report<? extends R> report) {
		checkNotNull(report);

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

	}

	public static class ReportItemImpl implements ReportItem {

		private final Severity severity;
		private final Identity source;
		private final ErrorCode code;
		private final String message;
		private final Object[] data;

		public ReportItemImpl(Severity severity, Identity source,
				ErrorCode code, String message, Object...data) {

			checkNotNull(severity);
			checkNotNull(code);
			checkNotNull(message);

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
			return String.format(message, data);
		}

		/**
		 * @see de.ims.icarus2.Report.ReportItem#getMessage(java.util.Locale)
		 */
		@Override
		public String getMessage(Locale locale) {
			return String.format(locale, message, data);
		}

	}
}
