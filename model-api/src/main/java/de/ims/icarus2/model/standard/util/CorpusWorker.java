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

 * $Revision: 392 $
 * $Date: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus2Core/core/de.ims.icarus2.model/source/de/ims/icarus2/model/util/CorpusWorker.java $
 *
 * $LastChangedDate: 2015-04-29 12:56:11 +0200 (Mi, 29 Apr 2015) $
 * $LastChangedRevision: 392 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus2.model.standard.util;

import javax.swing.SwingWorker;

import de.ims.icarus2.model.api.corpus.Corpus;

/**
 * @author Markus Gärtner
 * @version $Id: CorpusWorker.java 392 2015-04-29 10:56:11Z mcgaerty $
 *
 */
public class CorpusWorker extends SwingWorker<Corpus, Integer> {

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Corpus doInBackground() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
