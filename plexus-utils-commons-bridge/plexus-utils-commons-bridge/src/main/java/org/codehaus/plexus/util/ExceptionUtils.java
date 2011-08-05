package org.codehaus.plexus.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ExceptionUtils contains helper methods for treating Throwable objects.
 * Please note that lots of the given methods are nowadays not needed anymore
 * with Java &gt; 1.4. With Java-1.4 the Exception class itself got all
 * necessary methods to treat chained Exceptions. The original ExceptionUtils
 * got created when this was not yet the case!
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class ExceptionUtils
{
    /**
     * The maximum level of nestings evaluated when searching for a root cause.
     * We do this to prevent stack overflows!
     *
     * @see #getRootCause(Throwable)
     * @see #getRootCauseStackTrace(Throwable)
     */
    private static final int MAX_ROOT_CAUSE_DEPTH = 20;

    private static CopyOnWriteArrayList<String> specialCauseMethodNames = new CopyOnWriteArrayList<String>();
    static
    {
        specialCauseMethodNames.add( "getException" );
        specialCauseMethodNames.add( "getSourceException" );
        specialCauseMethodNames.add( "getRootCause" );
        specialCauseMethodNames.add( "getCausedByException" );
        specialCauseMethodNames.add( "getNested" );
    }

    /**
     * This method is only here for backward compat reasons.
     * It's original means was to add additional method names
     * which got checked to determine if the Throwable in question
     * is a chained exception.
     * It's not needed anymore in case of java &gt; 1.4 since
     * Throwable itself supports chains nowadays.
     *
     * @param methodName
     */
    @Deprecated
    public static void addCauseMethodName( String methodName )
    {
        specialCauseMethodNames.add(methodName);
    }

    /**
     * The {@link Throwable#getCause()} of the given Throwable.
     *
     * @param throwable
     *
     * @return the cause of the given Throwable, <code>null</code> if no cause exists.
     */
    public static Throwable getCause( Throwable throwable )
    {
        Throwable retVal = throwable.getCause();

        if ( retVal == null)
        {

            retVal = getCause( throwable
                             , specialCauseMethodNames.toArray( new String[ specialCauseMethodNames.size() ] ) );
        }

        return retVal;
    }

    /**
     * Get the cause of the given throwable if any by using the given methodNames
     *
     * @param throwable
     * @param methodNames
     * @return
     */
    public static Throwable getCause( Throwable throwable, String[] methodNames )
    {
        Throwable retVal = null;

        // first try a few standard Exception types we already know
        if ( retVal == null && throwable instanceof SQLException )
        {
            retVal = ((SQLException) throwable).getNextException();
        }

        if ( retVal == null && throwable instanceof InvocationTargetException)
        {
            retVal = ((InvocationTargetException) throwable).getTargetException();
        }

        if ( retVal == null )
        {
            for ( String methodName : methodNames )
            {
                if ( methodName == null )
                {
                    continue;
                }

                retVal = getCauseByMethodName( throwable, methodName );
                if ( retVal != null )
                {
                    return retVal;
                }
            }
        }

        return retVal;
    }

    /**
     * Internal method to get the cause of a given Throwable by a specified methods name.
     *
     * @param throwable
     * @param methodName
     * @return the cause or <code>null</code> if either the method doesn't exist or there is no cause.
     */
    private static Throwable getCauseByMethodName( Throwable throwable, String methodName )
    {
        Method method;
        try
        {
            method = throwable.getClass().getMethod( methodName, null );
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }

        if ( method.getReturnType() == null || !method.getReturnType().isAssignableFrom( Throwable.class ) )
        {
            return null;
        }

        try
        {
            return (Throwable) method.invoke( throwable );
        }
        catch (IllegalAccessException e)
        {
            return null;
        }
        catch (InvocationTargetException e)
        {
            return null;
        }
    }


    /**
     * Go down the {@link Throwable#getCause()} chain to find the
     * source of the problem. For nested Throwables, this will return
     * the recursively deepest Throwable in the chain.
     *
     * @param throwable
     *
     * @return the original cause of the Throwable
     */
    public static Throwable getRootCause( Throwable throwable )
    {
        if ( throwable == null )
        {
            throw new NullPointerException( "Throwable in ExceptionUtils#getRootCause must not be null!" );
        }

        Throwable rootCause = throwable;
        int depth = 0;

        while ( rootCause != null )
        {
            if ( depth >= MAX_ROOT_CAUSE_DEPTH )
            {
                // maximum depth level reached!
                return rootCause;
            }

            Throwable nextRootCause = getCause( rootCause );

            if ( nextRootCause == null )
            {
                if ( depth == 0 )
                {
                    return null;
                }
                else
                {
                    return rootCause;
                }
            }

            rootCause = nextRootCause;
            depth++;
        }

        return rootCause;
    }

    public static int getThrowableCount( Throwable throwable )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return -1;
    }

    public static Throwable[] getThrowables( Throwable throwable )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return null;
    }

    public static int indexOfThrowable( Throwable throwable, Class type )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return -1;
    }

    public static int indexOfThrowable( Throwable throwable, Class type, int fromIndex )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return -1;
    }

    public static void printRootCauseStackTrace( Throwable t, PrintStream stream )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
    }

    public static void printRootCauseStackTrace( Throwable t )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
    }

    public static void printRootCauseStackTrace( Throwable t, PrintWriter writer )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
    }

    public static String[] getRootCauseStackTrace( Throwable t )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return null;
    }

    public static String getStackTrace( Throwable t )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return null;
    }

    public static String getFullStackTrace( Throwable t )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return null;
    }

    public static boolean isNestedThrowable( Throwable throwable )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return true;
    }

    public static String[] getStackFrames( Throwable t )
    {
        System.out.println("TODO IMPLEMENT");
        //X TODO implement
        return null;
    }

}
