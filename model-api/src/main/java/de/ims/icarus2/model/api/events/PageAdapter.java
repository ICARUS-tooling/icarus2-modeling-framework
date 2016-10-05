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
 *
 */
package de.ims.icarus2.model.api.events;

import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.view.CorpusView.PageControl;

/**
 * @author Markus Gärtner
 *
 */
public class PageAdapter implements PageListener {

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageClosing(de.ims.icarus2.model.api.view.CorpusView.PageControl, int)
	 */
	@Override
	public void pageClosing(PageControl source, int page) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageClosed(de.ims.icarus2.model.api.view.CorpusView.PageControl, int)
	 */
	@Override
	public void pageClosed(PageControl source, int page) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageLoading(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, int)
	 */
	@Override
	public void pageLoading(PageControl source, int page, int size) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageLoaded(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, int)
	 */
	@Override
	public void pageLoaded(PageControl source, int page, int size) {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.api.events.PageListener#pageFailed(de.ims.icarus2.model.api.view.CorpusView.PageControl, int, de.ims.icarus2.model.api.ModelException)
	 */
	@Override
	public void pageFailed(PageControl source, int page, ModelException ex) {
		// no-op
	}

}
