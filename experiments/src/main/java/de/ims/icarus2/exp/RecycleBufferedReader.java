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
package de.ims.icarus2.exp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
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
		Path file = Paths.get("D:","Workspaces","git","icarus2","experiments","src","main","java","de","ims","icarus2","exp","RecycleBufferedReader.java");

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
