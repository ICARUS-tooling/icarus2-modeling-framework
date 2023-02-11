/**
 *
 */
package de.ims.icarus2.util.lang;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import de.ims.icarus2.util.collections.LazyCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * @author Markus Gärtner
 *
 */
@NotThreadSafe
public class ResourceResolver {

	private final List<Reference<ClassLoader>> loaders = new ObjectArrayList<>();

	//TODO allow registration of individual resources by name+URL

	public void addLoader(ClassLoader loader) {
		requireNonNull(loader);
		for(Iterator<Reference<ClassLoader>> it = loaders.iterator(); it.hasNext();) {
			Reference<ClassLoader> ref = it.next();
			ClassLoader l = ref.get();
			if(l==null) {
				it.remove();
			} else if(l==loader) {
				return;
			}
		}
		loaders.add(new WeakReference<>(loader));
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public @Nullable URL getResource(String name) {
		requireNonNull(name);
		for(Iterator<Reference<ClassLoader>> it = loaders.iterator(); it.hasNext();) {
			Reference<ClassLoader> ref = it.next();
			ClassLoader loader = ref.get();
			if(loader==null) {
				it.remove();
				continue;
			}
			URL url = loader.getResource(name);
			if(url!=null) {
				return url;
			}
		}
		return null;
	}

	public List<URL> getResources(String name) throws IOException {
		requireNonNull(name);
		LazyCollection<URL> result = LazyCollection.lazyList();
		for(Iterator<Reference<ClassLoader>> it = loaders.iterator(); it.hasNext();) {
			Reference<ClassLoader> ref = it.next();
			ClassLoader loader = ref.get();
			if(loader==null) {
				it.remove();
				continue;
			}
			Enumeration<URL> res = loader.getResources(name);
			for(;res.hasMoreElements();) {
				result.add(res.nextElement());
			}
		}
		return result.getAsList();
	}

	public @Nullable InputStream getResourceAsStream(String name) {
		requireNonNull(name);
		for(Iterator<Reference<ClassLoader>> it = loaders.iterator(); it.hasNext();) {
			Reference<ClassLoader> ref = it.next();
			ClassLoader loader = ref.get();
			if(loader==null) {
				it.remove();
				continue;
			}
			InputStream in = loader.getResourceAsStream(name);
			if(in!=null) {
				return in;
			}
		}
		return null;
	}
}
