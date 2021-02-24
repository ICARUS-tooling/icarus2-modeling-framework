/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.driver.mods;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.concurrent.atomic.AtomicBoolean;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.apiguard.Guarded;
import de.ims.icarus2.apiguard.Guarded.MethodType;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.Driver;
import de.ims.icarus2.model.api.driver.mods.DriverModule;
import de.ims.icarus2.model.api.driver.mods.ModuleMonitor;
import de.ims.icarus2.model.api.driver.mods.ModuleState;
import de.ims.icarus2.util.AbstractBuilder;
import de.ims.icarus2.util.AbstractPart;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractDriverModule extends AbstractPart<Driver> implements DriverModule {

	protected final DefaultModuleState state;
	protected final StaticIdentity identity;

	private final AtomicBoolean ready = new AtomicBoolean(false);
	private final AtomicBoolean busy = new AtomicBoolean(false);

	/*
	 * Only used during active tasks to mark a client originated abort
	 */
	private volatile boolean cancelled = false;

	protected AbstractDriverModule() {
		String id = getClass().getName();

		state = new DefaultModuleState(id);
		identity = new StaticIdentity(id);
	}

	protected AbstractDriverModule(String id) {
		requireNonNull(id);

		state = new DefaultModuleState(id);
		identity = new StaticIdentity(id);
	}

	protected static void checkInterrupted() throws InterruptedException {
		if(Thread.interrupted())
			throw new InterruptedException();
	}

	@Override
	public Driver getDriver() {
		checkAdded();

		return getOwner();
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public boolean isReady() {
		return ready.get();
	}

	@Override
	public boolean isBusy() {
		return busy.get();
	}

	@Override
	public ModuleState getState() {
		return state;
	}

	/**
	 * Returns the 'cancelled' flag, only meaningful during an active task
	 * @return
	 */
	protected boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void prepare(ModuleMonitor monitor) throws InterruptedException {
		if(isReady())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Module is already prepared");
		if(!busy.compareAndSet(false, true))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Module is already busy");

		try {
			cancelled = false;
			boolean result = doPrepare(monitor);

			if(!cancelled) {
				ready.set(result);
			}
		} finally {
			busy.set(false);
			cancelled = false;
		}
	}

	/**
	 * Performs internal preparation steps to make the module ready for further use.
	 * For most modules this will be the time and place to do the binding onto the
	 * live instances of layers and such.
	 *
	 * @param monitor
	 * @return
	 * @throws InterruptedException
	 */
	protected abstract boolean doPrepare(ModuleMonitor monitor) throws InterruptedException;

	@Override
	public void reset(ModuleMonitor monitor) throws InterruptedException {

		/*
		 *  NOTE: reset should allow a call even when a previous 'prepare()' failed,
		 *  so that a module can recover from a premature cancellation.
		 */

//		if(!isReady())
//			throw new ModelException(ModelError.ILLEGAL_STATE, "Module is not ready - cannot reset");
		if(!busy.compareAndSet(false, true))
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Module is already busy");

		try {
			cancelled = false;
			boolean result = doReset(monitor);

			if(!cancelled) {
				ready.set(result);
			}
		} finally {
			busy.set(false);
			cancelled = false;
		}
	}

	protected abstract boolean doReset(ModuleMonitor monitor) throws InterruptedException;

	/**
	 * Default implementation sets the internal 'cancelled' flag to {@code true},
	 * thereby signaling the running operation to stop.
	 * <p>
	 * Note that it is important for any subclass to ensure that the implementations of
	 * both the {@link #doPrepare(ModuleMonitor)} and {@link #doReset(ModuleMonitor)}
	 * methods actually honor the cancellation and check for the {@link #isCancelled() cancelled}
	 * flag regularly!
	 *
	 * @see de.ims.icarus2.model.api.driver.mods.DriverModule#cancel()
	 */
	@Override
	public void cancel() {
		if(!busy.get())
			throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Module is not busy - cannot cancel");

		cancelled = true;
	}

	protected abstract void doCancel();

	public static abstract class AbstractModuleBuilder<B extends AbstractModuleBuilder<B, D>, D extends Driver> extends AbstractBuilder<B, DriverModule> {

		private D driver;

		@Guarded(methodType=MethodType.BUILDER)
		public B driver(D driver) {
			requireNonNull(driver);
			checkState(this.driver==null);

			this.driver = driver;

			return thisAsCast();
		}

		@Guarded(methodType=MethodType.GETTER)
		public D getDriver() {
			return driver;
		}

		@Override
		protected void validate() {
			checkState("Driver not set", driver!=null);
		}
	}
}
