/*
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
package de.ims.icarus2.model.api.events;

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;

/**
 *
 * @author Markus Gärtner
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
	void pageFailed(PageControl source, int page, IcarusApiException exception);
}
