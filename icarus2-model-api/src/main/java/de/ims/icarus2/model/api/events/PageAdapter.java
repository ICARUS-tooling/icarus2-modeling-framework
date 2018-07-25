/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl;

/**
 * @author Markus Gärtner
 *
 */
public class PageAdapter implements PageListener {

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageClosing(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int)
	 */
	@Override
	public void pageClosing(PageControl source, int page) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageClosed(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int)
	 */
	@Override
	public void pageClosed(PageControl source, int page) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageLoading(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, int)
	 */
	@Override
	public void pageLoading(PageControl source, int page, int size) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageLoaded(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, int)
	 */
	@Override
	public void pageLoaded(PageControl source, int page, int size) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageFailed(de.ims.icarus2.model.api.view.paged.PagedCorpusView.PageControl, int, de.ims.icarus2.model.api.ModelException)
	 */
	@Override
	public void pageFailed(PageControl source, int page, ModelException ex) {
		// no-op
	}

}
