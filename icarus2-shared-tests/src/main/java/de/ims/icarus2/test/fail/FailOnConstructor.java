/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.test.fail;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.util.Collections;

import javax.lang.model.element.Modifier;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Markus Gärtner
 *
 */
public class FailOnConstructor {

	/**
	 * Creates a class that will always fail its initialization via the
	 * {@link Class#newInstance()} method by throwing an instance of the
	 * specified {@code exceptionClass}.
	 *
	 * @param packageName a fully qualified proper java package name or the empty string
	 * @param className a proper java class name without the package prefix
	 * @param baseClass the desired super class of the generated class object or {@code null}
	 * if the class should directly inherit from {@link Object}.
	 * @param exceptionClass the type of exception to throw on attempts of instantiating the class
	 * @return
	 */
	public static <T extends Object> Class<T> createClassForException(
			String packageName, String className, Class<?> baseClass,
			Class<? extends Exception> exceptionClass) {

		requireNonNull(packageName);
		requireNonNull(className);
		requireNonNull(exceptionClass);

		JavaFileObject sourceFile = createSource(packageName, className, baseClass, exceptionClass, null);

		return createClass(sourceFile, packageName, className);
	}

	/**
	 * Creates a class that will always fail its initialization via the
	 * {@link Constructor#newInstance(Object...)} method of the specified
	 * constructor by throwing an instance of the given {@code exceptionClass}.
	 *
	 * @param packageName a fully qualified proper java package name or the empty string
	 * @param className a proper java class name without the package prefix
	 * @param baseClass the desired super class of the generated class object or {@code null}
	 * if the class should directly inherit from {@link Object}.
	 * @param exceptionClass the type of exception to throw on attempts of instantiating the class
	 * @param constructorSig the parameter signature of the sole constructor of the created class
	 * @return
	 */
	public static <T extends Object> Class<T> createClassForException(
			String packageName, String className, Class<?> baseClass,
			Class<? extends Exception> exceptionClass,
			Class<?>...constructorSig) {

		requireNonNull(packageName);
		requireNonNull(className);
		requireNonNull(exceptionClass);
		requireNonNull(constructorSig);
		if(constructorSig.length<1)
			throw new IllegalArgumentException("Missing classes for constructor signature");

		JavaFileObject sourceFile = createSource(packageName, className,
				baseClass, exceptionClass, constructorSig);

		return createClass(sourceFile, packageName, className);
	}

	@SuppressWarnings({ "unchecked", "boxing" })
	private static <T extends Object> Class<T> createClass(JavaFileObject sourceFile,
			String packageName, String className) {
		ByteArrayJavaFileObject classFile = new ByteArrayJavaFileObject(packageName, className);
		JavaFileManager fileManager = createFileManager(sourceFile, classFile);

		assertTrue(ToolProvider.getSystemJavaCompiler().getTask(
				null, fileManager, null, null, null, Collections.singleton(sourceFile)).call());

		ClassLoader classLoader = createClassLoader(classFile);

		try {
			return (Class<T>) classLoader.loadClass(createFullName(packageName, className, false));
		} catch (ClassNotFoundException e) {
			throw new InternalError("Unable to load new class :"+e.getMessage(), e);
		}
	}

	private static MethodSpec createNoArgsConstructor(Class<? extends Exception> exceptionClass) {
		return MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addStatement("throw new $T()", exceptionClass)
				.addException(exceptionClass)
				.build();
	}

	private static MethodSpec createConstructor(Class<? extends Exception> exceptionClass,
			Class<?>[] constructorSig) {
		MethodSpec.Builder builder = MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addStatement("throw new $T()", exceptionClass)
				.addException(exceptionClass);

		for(int i=0; i<constructorSig.length; i++) {
			builder.addParameter(constructorSig[i], "arg"+i);
		}

		return builder.build();
	}

	private static JavaFileObject createSource(String packageName,
			String className, Class<?> baseClass, Class<? extends Exception> exceptionClass,
			Class<?>[] constructorSig) {
		MethodSpec constructorSpec = constructorSig==null ?
				createNoArgsConstructor(exceptionClass) : createConstructor(exceptionClass, constructorSig);

		TypeSpec typeSpec = TypeSpec
				.classBuilder(ClassName.get(packageName, className))
				.addModifiers(Modifier.PUBLIC)
				.superclass(baseClass==null ? TypeName.OBJECT : TypeName.get(baseClass))
				.addMethod(constructorSpec)
				.build();

		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

		return javaFile.toJavaFileObject();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static JavaFileManager createFileManager(JavaFileObject sourceFile, JavaFileObject classFile) {
		return new ForwardingJavaFileManager(
				ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null,
						StandardCharsets.UTF_8)) {

			@Override
			public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
					FileObject sibling) throws IOException {
				return classFile;
			}

			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
					throws IOException {
				return sourceFile;
			}
		};
	}

	private static String createFullName(String packageName, String className, boolean asPath) {
		String name = packageName.isEmpty() ? className : packageName + '.' + className;
		if(asPath) {
			name = name.replace('.', '/');
		}
		return name;
	}

	private static URI createUri(String packageName, String className, Kind kind) {
		return  URI.create(createFullName(packageName, className, true) + kind.extension);
	}

	private static ClassLoader createClassLoader(final ByteArrayJavaFileObject classFile) {

		PrivilegedAction<ClassLoader> createLoader = () -> {
			return new SecureClassLoader() {
				@Override
				protected Class<?> findClass(String name) throws ClassNotFoundException {
					if(!classFile.getName().replace('/', '.').startsWith(name))
						throw new ClassNotFoundException("Unknown class: "+name);

					byte[] b = classFile.getBytes();

					return super.defineClass(name, b, 0, b.length);
				}
			};
		};

		return AccessController.doPrivileged(createLoader);
	}

	private static class ByteArrayJavaFileObject extends SimpleJavaFileObject {

	    private final ByteArrayOutputStream bos =
	        new ByteArrayOutputStream();

	    public ByteArrayJavaFileObject(String packageName, String className) {
	        super(createUri(packageName, className, Kind.CLASS), Kind.CLASS);
	    }

	    /**
	     * @see javax.tools.SimpleJavaFileObject#openOutputStream()
	     */
	    @Override
	    public OutputStream openOutputStream() throws IOException {
	    	return bos;
	    }

	    /**
	     * @see javax.tools.SimpleJavaFileObject#openWriter()
	     */
	    @Override
	    public Writer openWriter() throws IOException {
	        return new OutputStreamWriter(openOutputStream(), StandardCharsets.UTF_8);
	    }

	    public byte[] getBytes() {
	    	return bos.toByteArray();
	    }
	}
}
