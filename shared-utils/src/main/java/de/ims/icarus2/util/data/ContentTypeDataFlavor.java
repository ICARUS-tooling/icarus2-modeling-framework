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
package de.ims.icarus2.util.data;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Markus Gärtner
 *
 */
public class ContentTypeDataFlavor extends DataFlavor {

	private ContentType contentType;

	/**
	 * For support of the {@code Externalizable} interface.
	 */
	public ContentTypeDataFlavor() {
		super();
	}

	public ContentTypeDataFlavor(ContentType contentType) {
		super(contentType.getContentClass(), contentType.getName()+"-DataFlavor"); //$NON-NLS-1$
		this.contentType = contentType;
	}

	public ContentType getContentType() {
		return contentType;
	}

	@Override
	public synchronized void writeExternal(ObjectOutput os) throws IOException {
		super.writeExternal(os);

		os.writeUTF(contentType.getId());
	}

	@Override
	public synchronized void readExternal(ObjectInput is) throws IOException,
			ClassNotFoundException {
		super.readExternal(is);

		String contentTypeId = is.readUTF();
		contentType = ContentTypeRegistry.getInstance().getType(contentTypeId);
	}
}
