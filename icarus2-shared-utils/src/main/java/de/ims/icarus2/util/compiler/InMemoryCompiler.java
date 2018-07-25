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
package de.ims.icarus2.util.compiler;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;

/**
 * @author Markus Gärtner
 *
 */
public class InMemoryCompiler {

	private volatile static InMemoryCompiler instance;

	public static InMemoryCompiler getSharedInstance() {
		InMemoryCompiler result = instance;

		if (result == null) {
			synchronized (InMemoryCompiler.class) {
				result = instance;

				if (result == null) {
					instance = newInstance();
					result = instance;
				}
			}
		}

		return result;
	}

	public static InMemoryCompiler newInstance(InMemoryJavaFileManager fileManager) {
		return new InMemoryCompiler(fileManager);
	}

	public static InMemoryCompiler newInstance() {
		return new InMemoryCompiler(null);
	}

	private static JavaCompiler getJavaCompiler() {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler==null)
			throw new IcarusException(GlobalErrorCode.VM_JDK_REQUIRED, "No java compiler available");
		return compiler;
	}

	private final Lock lock = new ReentrantLock();

	private final List<JavaFileObject> inputFiles = new ArrayList<>();

	private final InMemoryJavaFileManager fileManager;

	InMemoryCompiler(InMemoryJavaFileManager fileManager) {

		if(fileManager==null) {
			fileManager = new InMemoryJavaFileManager(getJavaCompiler().getStandardFileManager(null, null, null));
		}

		this.fileManager = fileManager;
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public InMemoryJavaFileManager getFileManager() {
		return fileManager;
	}

	public void addInputFile(JavaFileObject file) {
		requireNonNull(file);
		inputFiles.add(file);
	}

	public void addInputFile(String className, CharSequence content) {
		addInputFile(new CharSequenceJavaFileObject(className, content));
	}

	/**
	 * Compiles all the currently available input files.
	 * After compilation, no matter the result, the internal list of files will be cleared.
	 *
	 * @param diagnosticListener
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "boxing" })
	public boolean compile(DiagnosticListener diagnosticListener) {
		if(inputFiles.isEmpty())
			throw new IcarusException(GlobalErrorCode.ILLEGAL_STATE, "Nothing to compile - no input files specified");

		try {
			@SuppressWarnings("unchecked")
			CompilationTask task = getJavaCompiler().getTask(null, fileManager, diagnosticListener, null, null, inputFiles);

			return task.call();
		} finally {
			inputFiles.clear();
		}
	}
}
