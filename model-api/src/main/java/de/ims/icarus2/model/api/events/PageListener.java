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

 * $Revision: 437 $
 * $Date: 2015-11-10 13:03:14 +0100 (Di, 10 Nov 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/api/events/PageListener.java $
 *
 * $LastChangedDate: 2015-11-10 13:03:14 +0100 (Di, 10 Nov 2015) $
 * $LastChangedRevision: 437 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.api.events;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.CorpusView.PageControl;

/**
 *
 * @author Markus Gärtner
 * @version $Id: PageListener.java 437 2015-11-10 12:03:14Z mcgaerty $
 *
 */
public interface PageListener {

	/**
	 * Fired when a {@link PageControl} initiates the closing process
	 * of a page. At this point the page in question is still active and
	 * all its information (e.g. {@link PageControl#getIndices() indices})
	 * available.
	 *
	 * @param source
	 * @param page
	 */
	void pageClosing(PageControl source, int page);

	/**
	 * Fired when a {@link PageControl} has successfully closed a previously
	 * active page. Note that unlike the {@link #pageClosing(PageControl, int) pre-process}
	 * version of this notification, the page in question will have already been
	 * closed and most of its associated resources been released when this event
	 * is fired!
	 *
	 * @param source
	 * @param page
	 */
	void pageClosed(PageControl source, int page);

	/**
	 * Fired when a {@link PageControl} initiates the loading process of a page.
	 * At this point the only reliable information about the new page will be
	 * the number of accessible entries as stated i nthe {@code size} argument.
	 *
	 * @param source
	 * @param page
	 * @param size
	 */
	void pageLoading(PageControl source, int page, int size);

	//TODO comments
	/**
	 * Fired when a {@link PageControl} has successfully finished loading a page.
	 * The information in the {@code size} parameter is slightly redundant since
	 * from that point on the specified {@code page} and all its (meta)data is
	 * fully available.
	 *
	 * @param source
	 * @param page
	 * @param size
	 */
	void pageLoaded(PageControl source, int page, int size);

	/**
	 * Fired when either one of {@link PageControl#loadPage(int)} or {@link PageControl#closePage()}
	 * encountered an error during its execution. The {@code page} parameter specifies the affected
	 * page and the raw or wrapped {@code exception} is also provided.
	 *
	 * @param source
	 * @param page
	 * @param exception
	 */
	void pageFailed(PageControl source, int page, ModelException exception);
}
