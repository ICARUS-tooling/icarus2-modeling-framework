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
package de.ims.icarus2.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.ims.icarus2.util.nio.SubChannel;

/**
 * @author Markus Gärtner
 *
 */
public class RecycleBufferedReader {

	public static void main(String[] args) throws IOException, URISyntaxException {
		Path file;
		URL url = RecycleBufferedReader.class.getResource("RecycleBufferedReader.java");
		if(url==null) {
			file = Paths.get("src/main/java/de/ims/icarus2/exp/RecycleBufferedReader.java");
		} else {
			file = Paths.get(url.toURI());
		}

		try(SeekableByteChannel channel = Files.newByteChannel(file)) {
			try(SubChannel subChannel = new SubChannel()) {
				subChannel.setSource(channel);
//				subChannel.setOffsets(0, channel.size());

				int size = (int) channel.size();
				int slots = 4;

				int slotsize = size/slots;

				try(BufferedReader reader = new BufferedReader(Channels.newReader(subChannel, StandardCharsets.UTF_8.newDecoder(), 65536))) {
					for(int slot=0;slot<slots;slot++) {
						int end = size-(slot*slotsize);
						int begin = Math.max(0, end-slotsize);

						subChannel.setOffsets(begin, end);

						System.out.println("===============================");
						System.out.println(readReader(reader));
						System.out.println("===============================");
					}
				}
			}
		}
	}

	private static String readReader(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder(1000);

		String line;
		while((line=reader.readLine())!=null) {
			sb.append(line).append('\n');
		}

		return sb.toString();
	}
}
