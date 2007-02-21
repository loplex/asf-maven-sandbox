package org.apache.maven.surefire.junit4;

import org.apache.maven.surefire.report.StackTraceWriter;
import org.junit.runner.notification.Failure;

/**
 * Writes out a specific {@link org.junit.runner.notification.Failure} for
 * surefire as a stacktrace.
 * 
 * @author Karl M. Davis
 */
public class JUnit4StackTraceWriter implements StackTraceWriter
{
	// Member Variables
	private Failure junitFailure;

	/**
	 * Constructor.
	 * 
	 * @param junitFailure
	 *            the {@link Failure} that this will be operating on
	 */
	public JUnit4StackTraceWriter(Failure junitFailure)
	{
		this.junitFailure = junitFailure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.surefire.report.StackTraceWriter#writeTraceToString()
	 */
	public String writeTraceToString()
	{
		return junitFailure.getTrace();
	}

	/**
	 * At the moment, returns the same as {@link #writeTraceToString()}.
	 * 
	 * @see org.apache.maven.surefire.report.StackTraceWriter#writeTrimmedTraceToString()
	 */
	public String writeTrimmedTraceToString()
	{
		return junitFailure.getTrace();
	}

	/**
	 * Returns the exception associated with this failure.
	 * 
	 * @see org.apache.maven.surefire.report.StackTraceWriter#getThrowable()
	 */
	public Throwable getThrowable()
	{
		return junitFailure.getException();
	}

}
