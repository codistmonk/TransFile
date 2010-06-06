package net.sourceforge.transfile.operations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.transfile.tools.Tools;

/**
 * TODO doc
 *
 * @author codistmonk (creation 2010-06-05)
 *
 */
public abstract class AbstractOperation implements Operation {
	
	private final Collection<Listener> listeners;
	
	private final Connection connection;
	
	private final String fileName;
	
	private State state;
	
	private double progress;
	
	private File localFile;
	
	/**
	 * 
	 * @param connection
	 * <br>Should not be null
	 * <br>Shared parameter
	 * @param fileName
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public AbstractOperation(final Connection connection, final String fileName) {
		this.listeners = new ArrayList<Listener>();
		this.connection = connection;
		this.fileName = fileName;
		this.state = State.QUEUED;
	}
	
	@Override
	public final synchronized Connection getConnection() {
		return this.connection;
	}
	
	/**
	 * 
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public final synchronized Listener[] getListeners() {
		return this.listeners.toArray(new Listener[this.listeners.size()]);
	}
	
	@Override
	public final synchronized void addConnectionListener(final Listener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public final synchronized void removeConnectionListener(final Listener listener) {
		this.listeners.remove(listener);
	}
	
	@Override
	public final synchronized String getFileName() {
		return this.fileName;
	}
	
	@Override
	public final synchronized File getLocalFile() {
		return this.localFile;
	}
	
	@Override
	public final synchronized double getProgress() {
		return this.progress;
	}
	
	@Override
	public final synchronized State getState() {
		return this.state;
	}
	
	@Override
	public final synchronized void setLocalFile(final File localFile) {
		this.localFile = localFile;
	}
	
	/**
	 * 
	 * @param progress
	 * <br>Range: {@code [0.0 .. 1.0]}
	 */
	public final synchronized void setProgress(final double progress) {
		try {
			logEntry(progress);
			
			this.progress = progress;
			
			for (final Listener listener : this.getListeners()) {
				listener.progressChanged();
			}
		} finally {
			logReturn(null);
		}
	}
	
	/**
	 * 
	 * @param state
	 * <br>Should not be null
	 * <br>Shared parameter
	 */
	public final synchronized void setState(final State state) {
		try {
			logEntry(state);
			
			this.state = state;
			
			for (final Listener listener : this.getListeners()) {
				listener.stateChanged();
			}
		} finally {
			logReturn(null);
		}
	}
	
	/**
	 * 
	 * @param states
	 * <br>Should not be null
	 * @throws IllegalStateException if the current state is not in {@code states}
	 */
	public final void checkState(final State... states) {
		if (!set(states).contains(this.getState())) {
			throw new IllegalStateException("This section should have been executed in state " + Arrays.toString(states) + " but was executed in state " + this.getState());
		}
	}
	
	/**
	 * 
	 * @param <T> the common type of the elements
	 * @param elements
	 * <br>Should not be null
	 * @return
	 * <br>A non-null value
	 * <br>A new value
	 */
	public static final <T> HashSet<T> set(final T... elements) {
		return new HashSet<T>(Arrays.asList(elements));
	}
	
	/**
	 * TODO doc
	 * 
	 * @param parameters
	 * <br>Should not be null
	 */
	public static final void logEntry(final Object... parameters) {
		final Class<?> callerClass = Tools.getCallerClass();
		final String callerMethodName = Tools.getCallerMethodName();
		final String methodQualifiedName = callerClass.getName() + "." + callerMethodName;
		final Logger logger = Logger.getLogger(methodQualifiedName);
		
		logger.log(Level.FINEST, "Entering " + methodQualifiedName + " in thread " + Thread.currentThread() + " with parameters " + Arrays.toString(parameters));
	}
	
	/**
	 * TODO doc
	 * 
	 * @param returnedValue
	 * <br>Can be null
	 */
	public static final void logReturn(final Object returnedValue) {
		final Class<?> callerClass = Tools.getCallerClass();
		final String callerMethodName = Tools.getCallerMethodName();
		final String methodQualifiedName = callerClass.getName() + "." + callerMethodName;
		final Logger logger = Logger.getLogger(methodQualifiedName);
		
		logger.log(Level.FINEST, "Exiting " + methodQualifiedName + " in thread " + Thread.currentThread() + " with return value " + returnedValue);
	}
	
}
