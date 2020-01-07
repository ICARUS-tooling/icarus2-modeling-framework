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
package de.ims.icarus2.util.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.StringUtil;


/**
 * @author Markus Gärtner
 *
 */
public final class IOUtil {

	public static final int KB = 1024;
	public static final int MB = KB * 1024;
	public static final int GB = MB * 1024;


	private static final Logger log = LoggerFactory.getLogger(IOUtil.class);

	private IOUtil() {
		// no-op
	}

	public static final int DEFAULT_BUFFER_SIZE  = 1024 << 6;

	public static final Filter<Path> fileFilter =
			entry -> Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS);

	public static final Filter<Path> jarFilter =
			entry -> fileFilter.accept(entry) && entry.endsWith(".jar");

	public static final Filter<Path> directoryFilter =
			entry -> Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);

	public static void ensureFolder(Path path) {
		if(!Files.isDirectory(path) && Files.notExists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				throw new Error("Unable to create directory: "+path);
			}
		}
	}

	public static void deleteDirectory(final Path root) throws IOException {
		Files.walkFileTree(root, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static void cleanDirectory(final Path root) throws IOException {
		Files.walkFileTree(root, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				if(!Files.isSameFile(root, dir)) {
					Files.delete(root);
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static boolean isZipSource(String name) {
		return name.endsWith(".zip");
	}

	public static boolean isGZipSource(String name) {
		return name.endsWith(".gzip") || name.endsWith(".gz"); //$NON-NLS-2$
	}

	public static boolean isGZipSource(Path path) {
		if(path!=null && path.getNameCount()>0) {
			path = path.getFileName();
		}
		if(path==null) {
			return false;
		}
		return isGZipSource(path.toString());
	}

	public static String readStream(InputStream input) throws IOException {
		return readStream(input, StandardCharsets.UTF_8);
	}

	public static String readStream(InputStream input, Charset encoding) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = input.read(buffer)) > 0) {
			baos.write(buffer, 0, len);
		}
		input.close();
		return new String(baos.toByteArray(), encoding);
	}

	public static String readStreamUnchecked(InputStream input) {
		return readStreamUnchecked(input, StandardCharsets.UTF_8);
	}

	public static String readStreamUnchecked(InputStream input, Charset encoding) {
		try {
			return readStream(input, encoding);
		} catch (IOException e) {
			// ignore
		}

		return null;
	}

    public static void copyStream(final InputStream in, final OutputStream out) throws IOException {
    	copyStream(in, out, 0);
    }

    public static void copyStream(final InputStream in, final OutputStream out,
            int bufferSize) throws IOException {
    	if(bufferSize==0) {
    		bufferSize = DEFAULT_BUFFER_SIZE;
    	}
        byte[] buf = new byte[bufferSize];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Wraps a given {@link Reader} within a {@link BufferedReader} or returns
     * the {@code reader} in case it already is an instance of {@link BufferedReader}.
     * @param reader
     * @return
     */
    public static BufferedReader buffer(Reader reader) {
    	if(reader instanceof BufferedReader) {
    		return (BufferedReader) reader;
    	}

    	return new BufferedReader(reader);
    }

    /**
     * @see RandomAccessFile#writeInt(int)
     */
    public static void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >>> 24) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>>  8) & 0xFF);
        out.write((v >>>  0) & 0xFF);
    }

    /**
     * @see RandomAccessFile#writeLong(long)
     */
    public final void writeLong(OutputStream out, long v) throws IOException {
    	out.write((int)(v >>> 56) & 0xFF);
    	out.write((int)(v >>> 48) & 0xFF);
    	out.write((int)(v >>> 40) & 0xFF);
    	out.write((int)(v >>> 32) & 0xFF);
    	out.write((int)(v >>> 24) & 0xFF);
    	out.write((int)(v >>> 16) & 0xFF);
    	out.write((int)(v >>>  8) & 0xFF);
    	out.write((int)(v >>>  0) & 0xFF);
    }

    /**
     * @see RandomAccessFile#readInt()
     */
    public static int readInt(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * @see RandomAccessFile#readLong()
     */
    public static long readLong(InputStream in) throws IOException {
        return ((long)(readInt(in)) << 32) + (readInt(in) & 0xFFFFFFFFL);
    }

    /**
     * {@link AutoCloseable#close() close} the given {@code closeable}, ignoring any
     * {@link IOException} being thrown.
     * @param closeable
     */
    public static void closeSilently(Closeable closeable) {
    	try {
			closeable.close();
		} catch (IOException e) {
			log.error("Failed to close closeable resource", e);
		}
    }

	public static boolean isLocalFile(URL url) {
		String scheme = url.getProtocol();
		return "file".equalsIgnoreCase(scheme) && !hasHost(url);
	}

	public static String stripJarContext(String context) {
		int begin = context.startsWith("jar:") ? 4 : 0;
		int end = context.indexOf("jar!/", begin);
		if(end!=-1) {
			end += 3;
		} else {
			end = context.length();
		}

		return context.substring(begin, end);
	}

	public static boolean hasHost(URL url) {
		String host = url.getHost();
		return host != null && !"".equals(host);
	}

	public static boolean isLocal(URL url) {
		if (isLocalFile(url)) {
			return true;
		}
		String protocol = url.getProtocol();
		if ("jar".equalsIgnoreCase(protocol)) {
			String path = url.getPath();
			int emIdx = path.lastIndexOf('!');
			String subUrlString = emIdx == -1 ? path : path.substring(0, emIdx);
			try {
				URL subUrl = new URL(subUrlString);
				return isLocal(subUrl);
			} catch (java.net.MalformedURLException mfu) {
				return false;
			}
		}

		return false;
	}

	public static BufferedReader getReader(InputStream is, Charset cs) {
		return new BufferedReader(new InputStreamReader(is, cs));
	}

	public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	public static final String CHARSET_OPTION = "charset";
	public static final String CHARSET_NAME_OPTION = "charsetName";
	public static final String ENCODING_OPTION = "encoding";

	public static Charset getCharset(Options options, Charset defaultCharset) {
		Object charset = null;
		if(options!=null) {
			charset = options.firstSet(
					CHARSET_OPTION,
					CHARSET_NAME_OPTION,
					ENCODING_OPTION);
		}

		if(charset == null) {
			charset = defaultCharset==null ? UTF8_CHARSET : defaultCharset;
		} else if(charset instanceof String) {
			charset = Charset.forName((String)charset);
		}

		if(!(charset instanceof Charset))
			throw new NullPointerException("Invalid charset: "+charset.getClass());

		return (Charset) charset;
	}

	public static Charset getCharset(Options options) {
		return getCharset(options, null);
	}

    /**
     * Checks if resource exist and can be opened.
     * @param url absolute URL which points to a resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isResourceExists(final URL url) {
    	try {
	        Path path = Paths.get(url.toURI());
	        if (path != null) {
	            return Files.isReadable(path);
	        }
	        if ("jar".equalsIgnoreCase(url.getProtocol())) {
	            return isJarResourceExists(url);
	        }
	        return isUrlResourceExists(url);
    	} catch(URISyntaxException|FileSystemNotFoundException e) {
    		return false;
    	}
    }

    /**
     * Checks if resource URL exist and can be opened.
     * @param url absolute URL which points to a resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isUrlResourceExists(final URL url) {
        try {
            try(InputStream is = url.openStream()) {
            	is.available();
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Checks if resource jar exist and can be opened.
     * @param url absolute URL which points to a jar resource to be checked
     * @return <code>true</code> if given URL points to an existing resource
     */
    public static boolean isJarResourceExists(final URL url) {
        try {
            String urlStr = url.toExternalForm();
            int p = urlStr.indexOf("!/");
            if (p == -1) {// this is invalid JAR file URL
                return false;
            }
            Path path = Paths.get(url.toURI());
            if (path == null) {// this is non-local JAR file URL
                return isUrlResourceExists(url);
            }
            if (!Files.isReadable(path)) {
                return false;
            }
            if (p == urlStr.length() - 2) {// URL points to the root entry of JAR file
                return true;
            }
            try(JarFile jarFile = new JarFile(path.toFile())) {
                return jarFile.getEntry(urlStr.substring(p + 2)) != null;
            }
        } catch (IOException | URISyntaxException ioe) {
            return false;
        }
    }

    /**
     * Utility method to convert a {@link Path} object to a local URL.
     * @param p a file object
     * @return absolute URL that points to the given file
     * @throws MalformedURLException if file can't be represented as URL for
     *         some reason
     */
    public static URL fileToUrl(Path p) throws MalformedURLException {
        try {
            return p.toAbsolutePath().toUri().toURL();
        } catch (MalformedURLException mue) {
            throw mue;
        } catch (IOError ioe) {
            throw new MalformedURLException("unable to create absolute path: "
            		+ p + " " + ioe);
        }
    }

    /**
     * Creates a new {@code URL} that points to the same resource as the {@code source}
     * argument, but has all its file parts properly encoded. Note that this method will yield
     * undesired results if a part of the given {@code URL}'s file section has already been
     * encoded!
     */
    public static URL encodeURL(URL source) throws MalformedURLException {
    	String file = source.getPath();

    	int fileBegin = file.lastIndexOf(':')+1;
    	int jarSep = file.indexOf("!/", fileBegin);
    	if(jarSep==-1) {
    		jarSep = file.length();
    	}

    	String part = file.substring(fileBegin, jarSep);

    	StringBuilder sb = new StringBuilder();
    	if(fileBegin>0) {
    		sb.append(file, 0, fileBegin);
    	}
    	sb.append(encodeUrlFilePart(part));
    	if(jarSep<file.length()) {
    		sb.append(file, jarSep, file.length()-1);
    	}

    	return new URL(source.getProtocol(), source.getHost(), source.getPort(), sb.toString());
    }

    private static String encodeUrlFilePart(String file) {
    	String[] items = file.split("/");
    	for(int i = 0; i<items.length; i++) {
    		if(items[i].isEmpty()) {
    			continue;
    		}

    		try {
				items[i] = URLEncoder.encode(items[i], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// cannot happen
			}
    	}

    	return StringUtil.join(items, "/");
    }

    public static Path toRelativePath(Path root, Path p) {
    	if(p==null) {
    		return p;
    	}

    	try {
    		p = p.toAbsolutePath();
    	} catch(IOError e) {
    		log.warn("Error converting to absolute path: {}", p, e);
    		return p;
    	}

    	if(p.startsWith(root)) {
    		p = p.subpath(root.getNameCount(), p.getNameCount());
    	}
		return p;
    }

    // Utility methods for reading or writing entire files

    public static void writeFileUTF8(Path file, String content) throws IOException {
    	try(Writer writer = Files.newBufferedWriter(file, UTF8_CHARSET)) {
    		writer.write(content);
    	}
    }

    public static void writeFile(Path file, byte[] content) throws IOException {
    	Files.write(file, content);
    }

    public static byte[] readFile(Path file) throws IOException {
    	return Files.readAllBytes(file);
    }

    public static String readFileUTF8(Path file) throws IOException {
    	return new String(Files.readAllBytes(file), UTF8_CHARSET);
    }

    public static Path makeFile(Path folder, String name) throws IOException {
    	Path file = folder.resolve(name);
    	if(Files.notExists(file, LinkOption.NOFOLLOW_LINKS)) {
    		Files.createFile(file);
    	}
    	return file;
    }
}
