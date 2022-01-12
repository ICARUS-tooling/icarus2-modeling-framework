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

		os.writeUTF(contentType.getId().orElseThrow(IllegalStateException::new));
	}

	@Override
	public synchronized void readExternal(ObjectInput is) throws IOException,
			ClassNotFoundException {
		super.readExternal(is);

		String contentTypeId = is.readUTF();
		contentType = ContentTypeRegistry.getInstance().getType(contentTypeId);
	}
}
