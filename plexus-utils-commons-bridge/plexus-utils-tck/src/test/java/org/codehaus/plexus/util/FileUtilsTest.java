package org.codehaus.plexus.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.AssertionFailedError;
import org.apache.maven.tck.FixPlexusBugs;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * This is used to test FileUtils for correctness.
 *
 * @author Peter Donald
 * @author Matthew Hawthorne
 * @author Stephen Colebourne
 * @author Jim Harrington
 * @version $Id: FileUtilsTestCase.java 1081025 2011-03-13 00:45:10Z niallp $
 * @see FileUtils
 */
public class FileUtilsTest
{
    @Rule
    public FixPlexusBugs fixPlexusBugs = new FixPlexusBugs();

    // Test data

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestName name = new TestName();

    /**
     * Size of test directory.
     */
    private static final int TEST_DIRECTORY_SIZE = 0;

    /**
     * Delay in milliseconds to make sure test for "last modified date" are accurate
     */
    //private static final int LAST_MODIFIED_DELAY = 600;

    private File testFile1;

    private File testFile2;

    private long testFile1Size;

    private long testFile2Size;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp()
        throws Exception
    {
        testFile1 = folder.newFile( "file1-test.txt" );
        testFile2 = folder.newFile( "file1a-test.txt" );

        testFile1Size = (int) testFile1.length();
        testFile2Size = (int) testFile2.length();

        folder.getRoot().mkdirs();
        createFile( testFile1, testFile1Size );
        createFile( testFile2, testFile2Size );
        FileUtils.deleteDirectory( folder.getRoot() );
        folder.getRoot().mkdirs();
        createFile( testFile1, testFile1Size );
        createFile( testFile2, testFile2Size );
    }

    protected void createFile( File file, long size )
        throws IOException
    {
        if ( !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }
        BufferedOutputStream output = new BufferedOutputStream( new java.io.FileOutputStream( file ) );
        try
        {
            generateTestData( output, size );
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    protected byte[] generateTestData( long size )
    {
        try
        {
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            generateTestData( baout, size );
            return baout.toByteArray();
        }
        catch ( IOException ioe )
        {
            throw new RuntimeException( "This should never happen: " + ioe.getMessage() );
        }
    }

    protected void generateTestData( OutputStream out, long size )
        throws IOException
    {
        for ( int i = 0; i < size; i++ )
        {
            //output.write((byte)'X');

            // nice varied byte pattern compatible with Readers and Writers
            out.write( (byte) ( ( i % 127 ) + 1 ) );
        }
    }

    protected void createLineBasedFile( File file, String[] data )
        throws IOException
    {
        if ( file.getParentFile() != null && !file.getParentFile().exists() )
        {
            throw new IOException( "Cannot create file " + file + " as the parent directory does not exist" );
        }
        PrintWriter output = new PrintWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );
        try
        {
            for ( int i = 0; i < data.length; i++ )
            {
                output.println( data[i] );
            }
        }
        finally
        {
            IOUtil.close( output );
        }
    }

    protected File newFile( String filename )
        throws IOException
    {
        File destination = folder.newFile( filename );
        /*
        assertTrue( filename + "Test output data file shouldn't previously exist",
                    !destination.exists() );
        */
        if ( destination.exists() )
        {
            FileUtils.forceDelete( destination );
        }
        return destination;
    }

    protected void checkFile( File file, File referenceFile )
        throws Exception
    {
        assertThat( "Check existence of output file", file.exists(), is( true ) );
        assertEqualContent( referenceFile, file );
    }

    /**
     * Assert that the content of two files is the same.
     */
    private void assertEqualContent( File f0, File f1 )
        throws IOException
    {
        /* This doesn't work because the filesize isn't updated until the file
         * is closed.
        assertTrue( "The files " + f0 + " and " + f1 +
                    " have differing file sizes (" + f0.length() +
                    " vs " + f1.length() + ")", ( f0.length() == f1.length() ) );
        */
        InputStream is0 = new java.io.FileInputStream( f0 );
        try
        {
            InputStream is1 = new java.io.FileInputStream( f1 );
            try
            {
                byte[] buf0 = new byte[1024];
                byte[] buf1 = new byte[1024];
                int n0 = 0;
                int n1 = 0;

                while ( -1 != n0 )
                {
                    n0 = is0.read( buf0 );
                    n1 = is1.read( buf1 );
                    assertThat(
                        "The files " + f0 + " and " + f1 + " have differing number of bytes available (" + n0 + " vs "
                            + n1 + ")", n0, is( n1 ) );

                    assertThat( "The files " + f0 + " and " + f1 + " have different content", buf0, is( buf1 ) );
                }
            }
            finally
            {
                is1.close();
            }
        }
        finally
        {
            is0.close();
        }
    }

    /**
     * Assert that the content of a file is equal to that in a byte[].
     */
    protected void assertEqualContent( byte[] b0, File file )
        throws IOException
    {
        InputStream is = new java.io.FileInputStream( file );
        int count = 0, numRead = 0;
        byte[] b1 = new byte[b0.length];
        try
        {
            while ( count < b0.length && numRead >= 0 )
            {
                numRead = is.read( b1, count, b0.length );
                count += numRead;
            }
            assertThat( "Different number of bytes: ", count, is( b0.length ) );
            for ( int i = 0; i < count; i++ )
            {
                assertThat( "byte " + i + " differs", b1[i], is( b0[i] ) );
            }
        }
        finally
        {
            is.close();
        }
    }

    /**
     * Assert that the content of a file is equal to that in a char[].
     */
    protected void assertEqualContent( char[] c0, File file )
        throws IOException
    {
        Reader ir = new java.io.FileReader( file );
        int count = 0, numRead = 0;
        char[] c1 = new char[c0.length];
        try
        {
            while ( count < c0.length && numRead >= 0 )
            {
                numRead = ir.read( c1, count, c0.length );
                count += numRead;
            }
            assertThat( "Different number of chars: ", count, is( c0.length ) );
            for ( int i = 0; i < count; i++ )
            {
                assertThat( "char " + i + " differs", c1[i], is( c0[i] ) );
            }
        }
        finally
        {
            ir.close();
        }
    }

    protected void checkWrite( OutputStream output )
        throws Exception
    {
        try
        {
            new java.io.PrintStream( output ).write( 0 );
        }
        catch ( Throwable t )
        {
            throw new AssertionFailedError(
                "The copy() method closed the stream " + "when it shouldn't have. " + t.getMessage() );
        }
    }

    protected void checkWrite( Writer output )
        throws Exception
    {
        try
        {
            new java.io.PrintWriter( output ).write( 'a' );
        }
        catch ( Throwable t )
        {
            throw new AssertionFailedError(
                "The copy() method closed the stream " + "when it shouldn't have. " + t.getMessage() );
        }
    }

    protected void deleteFile( File file )
        throws Exception
    {
        if ( file.exists() )
        {
            assertThat( "Couldn't delete file: " + file, file.delete(), is( true ) );
        }
    }

    //-----------------------------------------------------------------------
    // byteCountToDisplaySize
    @Test
    public void byteCountToDisplaySize()
    {
        assertThat( "0 bytes", is( FileUtils.byteCountToDisplaySize( 0 ) ) );
        assertThat( "1 KB", is( FileUtils.byteCountToDisplaySize( 1024 ) ) );
        assertThat( "1 MB", is( FileUtils.byteCountToDisplaySize( 1024 * 1024 ) ) );
        assertThat( "1 GB", is( FileUtils.byteCountToDisplaySize( 1024 * 1024 * 1024 ) ) );
    }

    //-----------------------------------------------------------------------
    @Test
    public void toFile1()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file.txt" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file.txt" ) );
    }

    @Test
    public void toFile2()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file%20n%61me%2520.tx%74" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file name%20.txt" ) );
    }

    @Test
    public void toFile3()
        throws Exception
    {
        assertThat( FileUtils.toFile( (URL) null ), CoreMatchers.<File>nullValue() );
        assertThat( FileUtils.toFile( new URL( "http://jakarta.apache.org" ) ), CoreMatchers.<File>nullValue() );
    }

    @Test(expected = NumberFormatException.class)
    public void toFile4()
        throws Exception
    {
        URL url = new URL( "file", null, "a/b/c/file%%20%me.txt%" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), containsString( "file% %me.txt%" ) );
    }

    /**
     * IO-252
     */
    @Test
    public void toFile5()
        throws Exception
    {
        URL url = new URL( "file", null, "both%20are%20100%20%25%20true" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), is( "both are 100 % true" ) );
    }

    @Test
    public void toFileUtf8()
        throws Exception
    {
        URL url = new URL( "file", null, "/home/%C3%A4%C3%B6%C3%BC%C3%9F" );
        File file = FileUtils.toFile( url );
        assertThat( file.toString(), not( containsString( "\u00E4\u00F6\u00FC\u00DF" ) ) );
    }

    // toURLs

    @Test
    public void toURLs1()
        throws Exception
    {
        File[] files = new File[]{ new File( folder.getRoot(), "file1.txt" ), new File( folder.getRoot(), "file2.txt" ),
            new File( folder.getRoot(), "test file.txt" ), };
        URL[] urls = FileUtils.toURLs( files );

        assertThat( urls.length, is( files.length ) );
        assertThat( urls[0].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[0].toExternalForm().indexOf( "file1.txt" ) >= 0, is( true ) );
        assertThat( urls[1].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[1].toExternalForm(), containsString( "file2.txt" ) );

        // Test escaped char
        assertThat( urls[2].toExternalForm().startsWith( "file:" ), is( true ) );
        assertThat( urls[2].toExternalForm(), not( containsString( "test%20file.txt" ) ) );
    }

//    @Test public void toURLs2() throws Exception {
//        File[] files = new File[] {
//            new File(getTestDirectory(), "file1.txt"),
//            null,
//        };
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(files.length, urls.length);
//        assertEquals(true, urls[0].toExternalForm().startsWith("file:"));
//        assertEquals(true, urls[0].toExternalForm().indexOf("file1.txt") > 0);
//        assertEquals(null, urls[1]);
//    }
//
//    @Test public void toURLs3() throws Exception {
//        File[] files = null;
//        URL[] urls = FileUtils.toURLs(files);
//
//        assertEquals(0, urls.length);
//    }

    // contentEquals

    @Test
    public void contentEquals()
        throws Exception
    {
        // Non-existent files
        File file = new File( folder.getRoot(), name.getMethodName() );
        File file2 = new File( folder.getRoot(), name.getMethodName() + "2" );
        // both don't  exist
        assertThat( FileUtils.contentEquals( file, file ), is( true ) );
        assertThat( FileUtils.contentEquals( file, file2 ), is( true ) );
        assertThat( FileUtils.contentEquals( file2, file2 ), is( true ) );
        assertThat( FileUtils.contentEquals( file2, file ), is( true ) );

        // Directories
        FileUtils.contentEquals( folder.getRoot(), folder.getRoot() );

        // Different files
        File objFile1 = new File( folder.getRoot(), name.getMethodName() + ".object" );
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/lang/Object.class" ), objFile1 );

        File objFile1b = new File( folder.getRoot(), name.getMethodName() + ".object2" );
        objFile1.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/lang/Object.class" ), objFile1b );

        File objFile2 = new File( folder.getRoot(), name.getMethodName() + ".collection" );
        objFile2.deleteOnExit();
        FileUtils.copyURLToFile( getClass().getResource( "/java/util/Collection.class" ), objFile2 );

        assertThat( FileUtils.contentEquals( objFile1, objFile2 ), is( false ) );
        assertThat( FileUtils.contentEquals( objFile1b, objFile2 ), is( false ) );
        assertThat( FileUtils.contentEquals( objFile1, objFile1b ), is( true ) );

        assertThat( FileUtils.contentEquals( objFile1, objFile1 ), is( true ) );
        assertThat( FileUtils.contentEquals( objFile1b, objFile1b ), is( true ) );
        assertThat( FileUtils.contentEquals( objFile2, objFile2 ), is( true ) );

        // Equal files
        file.createNewFile();
        file2.createNewFile();
        assertThat( FileUtils.contentEquals( file, file ), is( true ) );
        assertThat( FileUtils.contentEquals( file, file2 ), is( true ) );
    }

    // copyURLToFile

    @Test
    public void copyURLToFile()
        throws Exception
    {
        // Creates file
        File file = new File( folder.getRoot(), name.getMethodName() );
        file.deleteOnExit();

        // Loads resource
        String resourceName = "/java/lang/Object.class";
        FileUtils.copyURLToFile( getClass().getResource( resourceName ), file );

        // Tests that resuorce was copied correctly
        FileInputStream fis = new FileInputStream( file );
        try
        {
            assertThat( "Content is not equal.",
                        IOUtil.contentEquals( getClass().getResourceAsStream( resourceName ), fis ), is( true ) );
        }
        finally
        {
            fis.close();
        }
        //TODO Maybe test copy to itself like for copyFile()
    }

    // forceMkdir

    @Test
    public void forceMkdir()
        throws Exception
    {
        // Tests with existing directory
        FileUtils.forceMkdir( folder.getRoot() );

        // Creates test file
        File testFile = new File( folder.getRoot(), name.getMethodName() );
        testFile.deleteOnExit();
        testFile.createNewFile();
        assertThat( "Test file does not exist.", testFile.exists(), is( true ) );

        // Tests with existing file
        try
        {
            FileUtils.forceMkdir( testFile );
            fail( "Exception expected." );
        }
        catch ( IOException ex )
        {
        }

        testFile.delete();

        // Tests with non-existent directory
        FileUtils.forceMkdir( testFile );
        assertThat( "Directory was not created.", testFile.exists(), is( true ) );
    }

    // sizeOfDirectory

    @Test
    public void sizeOfDirectory()
        throws Exception
    {
        File file = new File( folder.getRoot(), name.getMethodName() );

        // Non-existent file
        try
        {
            FileUtils.sizeOfDirectory( file );
            fail( "Exception expected." );
        }
        catch ( IllegalArgumentException ex )
        {
        }

        // Creates file
        file.createNewFile();
        file.deleteOnExit();

        // Existing file
        try
        {
            FileUtils.sizeOfDirectory( file );
            fail( "Exception expected." );
        }
        catch ( IllegalArgumentException ex )
        {
        }

        // Existing directory
        file.delete();
        file.mkdir();

        assertThat( "Unexpected directory size", FileUtils.sizeOfDirectory( file ), is( (long) TEST_DIRECTORY_SIZE ) );
    }

    // copyFile

    @Test
    public void copyFile1()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "copy1.txt" );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFile( testFile1, destination );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile1Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    public void IGNOREtestCopyFileLarge()
        throws Exception
    {

        File largeFile = new File( folder.getRoot(), "large.txt" );
        File destination = new File( folder.getRoot(), "copylarge.txt" );

        System.out.println( "START:   " + new java.util.Date() );
        createFile( largeFile, FileUtils.ONE_GB );
        System.out.println( "CREATED: " + new java.util.Date() );
        FileUtils.copyFile( largeFile, destination );
        System.out.println( "COPIED:  " + new java.util.Date() );

        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( largeFile.length() ) );
    }

    @Test
    public void copyFile2()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "copy2.txt" );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFile( testFile1, destination );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile2Size ) );
        /* disabled: Thread.sleep doesn't work reliably for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyToSelf()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "copy3.txt" );
        //Prepare a test file
        FileUtils.copyFile( testFile1, destination );

        FileUtils.copyFile( destination, destination );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryToDirectory_NonExistingDest()
        throws Exception
    {
        createFile( testFile1, 1234 );
        createFile( testFile2, 4321 );
        File srcDir = folder.getRoot();
        File subDir = new File( srcDir, "sub" );
        subDir.mkdir();
        File subFile = new File( subDir, "A.txt" );
        FileUtils.fileWrite( subFile, "UTF8", "HELLO WORLD" );
        File destDir = new File( System.getProperty( "java.io.tmpdir" ), "tmp-FileUtilsTestCase" );
        FileUtils.deleteDirectory( destDir );
        File actualDestDir = new File( destDir, srcDir.getName() );

        FileUtils.copyDirectory( srcDir, destDir );

        assertThat( "Check exists", destDir.exists(), is( true ) );
        assertThat( "Check exists", actualDestDir.exists(), is( true ) );
        assertThat( "Check size", FileUtils.sizeOfDirectory( actualDestDir ),
                    is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( actualDestDir, "sub/A.txt" ).exists(), is( true ) );
        FileUtils.deleteDirectory( destDir );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryToNonExistingDest()
        throws Exception
    {
        createFile( testFile1, 1234 );
        createFile( testFile2, 4321 );
        File srcDir = folder.getRoot();
        File subDir = new File( srcDir, "sub" );
        subDir.mkdir();
        File subFile = new File( subDir, "A.txt" );
        FileUtils.fileWrite( subFile, "UTF8", "HELLO WORLD" );
        File destDir = new File( System.getProperty( "java.io.tmpdir" ), "tmp-FileUtilsTestCase" );
        FileUtils.deleteDirectory( destDir );

        FileUtils.copyDirectory( srcDir, destDir );

        assertThat( "Check exists", destDir.exists(), is( true ) );
        assertThat( "Check size", FileUtils.sizeOfDirectory( destDir ), is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( destDir, "sub/A.txt" ).exists(), is( true ) );
        FileUtils.deleteDirectory( destDir );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryToExistingDest()
        throws Exception
    {
        createFile( testFile1, 1234 );
        createFile( testFile2, 4321 );
        File srcDir = folder.getRoot();
        File subDir = new File( srcDir, "sub" );
        subDir.mkdir();
        File subFile = new File( subDir, "A.txt" );
        FileUtils.fileWrite( subFile, "UTF8", "HELLO WORLD" );
        File destDir = new File( System.getProperty( "java.io.tmpdir" ), "tmp-FileUtilsTestCase" );
        FileUtils.deleteDirectory( destDir );
        destDir.mkdirs();

        FileUtils.copyDirectory( srcDir, destDir );

        assertThat( FileUtils.sizeOfDirectory( destDir ), is( FileUtils.sizeOfDirectory( srcDir ) ) );
        assertThat( new File( destDir, "sub/A.txt" ).exists(), is( true ) );
    }

    /**
     * Test for IO-141
     */
    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryToChild()
        throws Exception
    {
        File grandParentDir = new File( folder.getRoot(), "grandparent" );
        File parentDir = new File( grandParentDir, "parent" );
        File childDir = new File( parentDir, "child" );
        createFilesForTestCopyDirectory( grandParentDir, parentDir, childDir );

        long expectedCount =
            FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size()
                + FileUtils.getFileAndDirectoryNames( parentDir, null, null, true, true, true, true ).size();
        long expectedSize = FileUtils.sizeOfDirectory( grandParentDir ) + FileUtils.sizeOfDirectory( parentDir );
        FileUtils.copyDirectory( parentDir, childDir );
        assertThat(
            1L * FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size(),
            is( expectedCount ) );
        assertThat( FileUtils.sizeOfDirectory( grandParentDir ), is( expectedSize ) );
    }

    /**
     * Test for IO-141
     */
    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryToGrandChild()
        throws Exception
    {
        File grandParentDir = new File( folder.getRoot(), "grandparent" );
        File parentDir = new File( grandParentDir, "parent" );
        File childDir = new File( parentDir, "child" );
        createFilesForTestCopyDirectory( grandParentDir, parentDir, childDir );

        long expectedCount =
            ( FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size() * 2 );
        long expectedSize = ( FileUtils.sizeOfDirectory( grandParentDir ) * 2 );
        FileUtils.copyDirectory( grandParentDir, childDir );
        assertThat(
            1L * FileUtils.getFileAndDirectoryNames( grandParentDir, null, null, true, true, true, true ).size(),
            is( expectedCount ) );
        assertThat( FileUtils.sizeOfDirectory( grandParentDir ), is( expectedSize ) );
    }

    /**
     * Test for IO-217 FileUtils.copyDirectoryToDirectory makes infinite loops
     */
    @Test
    public void copyDirectoryToItself()
        throws Exception
    {
        File dir = new File( folder.getRoot(), "itself" );
        dir.mkdirs();
        FileUtils.copyDirectory( dir, dir );
        assertThat( FileUtils.getFileAndDirectoryNames( dir, null, null, true, true, true, true ).size(), is( 1 ) );
    }

    private void createFilesForTestCopyDirectory( File grandParentDir, File parentDir, File childDir )
        throws Exception
    {
        File childDir2 = new File( parentDir, "child2" );
        File grandChildDir = new File( childDir, "grandChild" );
        File grandChild2Dir = new File( childDir2, "grandChild2" );
        File file1 = new File( grandParentDir, "file1.txt" );
        File file2 = new File( parentDir, "file2.txt" );
        File file3 = new File( childDir, "file3.txt" );
        File file4 = new File( childDir2, "file4.txt" );
        File file5 = new File( grandChildDir, "file5.txt" );
        File file6 = new File( grandChild2Dir, "file6.txt" );
        FileUtils.deleteDirectory( grandParentDir );
        grandChildDir.mkdirs();
        grandChild2Dir.mkdirs();
        FileUtils.fileWrite( file1, "UTF8", "File 1 in grandparent" );
        FileUtils.fileWrite( file2, "UTF8", "File 2 in parent" );
        FileUtils.fileWrite( file3, "UTF8", "File 3 in child" );
        FileUtils.fileWrite( file4, "UTF8", "File 4 in child2" );
        FileUtils.fileWrite( file5, "UTF8", "File 5 in grandChild" );
        FileUtils.fileWrite( file6, "UTF8", "File 6 in grandChild2" );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyDirectoryErrors()
        throws Exception
    {
        try
        {
            FileUtils.copyDirectory( null, null );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( new File( "a" ), null );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( null, new File( "a" ) );
            fail();
        }
        catch ( NullPointerException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( new File( "doesnt-exist" ), new File( "a" ) );
            fail();
        }
        catch ( IOException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( testFile1, new File( "a" ) );
            fail();
        }
        catch ( IOException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( folder.getRoot(), testFile1 );
            fail();
        }
        catch ( IOException ex )
        {
        }
        try
        {
            FileUtils.copyDirectory( folder.getRoot(), folder.getRoot() );
            fail();
        }
        catch ( IOException ex )
        {
        }
    }

    // forceDelete

    @Test
    public void forceDeleteAFile1()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "copy1.txt" );
        destination.createNewFile();
        assertThat( "Copy1.txt doesn't exist to delete", destination.exists(), is( true ) );
        FileUtils.forceDelete( destination );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
    }

    @Test
    public void forceDeleteAFile2()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "copy2.txt" );
        destination.createNewFile();
        assertThat( "Copy2.txt doesn't exist to delete", destination.exists(), is( true ) );
        FileUtils.forceDelete( destination );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void forceDeleteAFile3()
        throws Exception
    {
        File destination = new File( folder.getRoot(), "no_such_file" );
        assertThat( "Check No Exist", !destination.exists(), is( true ) );
        try
        {
            FileUtils.forceDelete( destination );
            fail( "Should generate FileNotFoundException" );
        }
        catch ( FileNotFoundException ignored )
        {
        }
    }

    // copyFileToDirectory

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void copyFile1ToDir()
        throws Exception
    {
        File directory = new File( folder.getRoot(), "subdir" );
        if ( !directory.exists() )
        {
            directory.mkdirs();
        }
        File destination = new File( directory, testFile1.getName() );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFileToDirectory( testFile1, directory );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile1Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/

        try
        {
            FileUtils.copyFileToDirectory( destination, directory );
            fail( "Should not be able to copy a file into the same directory as itself" );
        }
        catch ( IOException ioe )
        {
            //we want that, cannot copy to the same directory as the original file
        }
    }

    @Test
    public void copyFile2ToDir()
        throws Exception
    {
        File directory = new File( folder.getRoot(), "subdir" );
        if ( !directory.exists() )
        {
            directory.mkdirs();
        }
        File destination = new File( directory, testFile1.getName() );

        //Thread.sleep(LAST_MODIFIED_DELAY);
        //This is to slow things down so we can catch if
        //the lastModified date is not ok

        FileUtils.copyFileToDirectory( testFile1, directory );
        assertThat( "Check Exist", destination.exists(), is( true ) );
        assertThat( "Check Full copy", destination.length(), is( testFile2Size ) );
        /* disabled: Thread.sleep doesn't work reliantly for this case
        assertTrue("Check last modified date preserved",
            testFile1.lastModified() == destination.lastModified());*/
    }

    // forceDelete

    @Test
    public void forceDeleteDir()
        throws Exception
    {
        File testDirectory = folder.newFolder( name.getMethodName() );
        FileUtils.forceDelete( testDirectory.getParentFile() );
        assertThat( "Check No Exist", !testDirectory.getParentFile().exists(), is( true ) );
    }

    /**
     * Test the FileUtils implementation.
     */
    @Test
    public void fileUtils()
        throws Exception
    {
        // Loads file from classpath
        File file1 = new File( folder.getRoot(), "test.txt" );
        String filename = file1.getAbsolutePath();

        //Create test file on-the-fly (used to be in CVS)
        OutputStream out = new java.io.FileOutputStream( file1 );
        try
        {
            out.write( "This is a test".getBytes( "UTF-8" ) );
        }
        finally
        {
            out.close();
        }

        File file2 = new File( folder.getRoot(), "test2.txt" );

        FileUtils.fileWrite( file2, "UTF-8", filename );
        assertThat( file2.exists(), is( true ) );
        assertThat( file2.length() > 0, is( true ) );

        String file2contents = FileUtils.fileRead( file2, "UTF-8" );
        assertThat( "Second file's contents correct", filename.equals( file2contents ), is( true ) );

        assertThat( file2.delete(), is( true ) );

        String contents = FileUtils.fileRead( new File( filename ), "UTF-8" );
        assertThat( "FileUtils.fileRead()", contents.equals( "This is a test" ), is( true ) );

    }

    @Test
    public void fileReadWithDefaultEncoding()
        throws Exception
    {
        File file = new File( folder.getRoot(), "read.obj" );
        FileOutputStream out = new FileOutputStream( file );
        byte[] text = "Hello /u1234".getBytes();
        out.write( text );
        out.close();

        String data = FileUtils.fileRead( file );
        assertThat( data, is( "Hello /u1234" ) );
    }

    @Test
    public void fileReadWithEncoding()
        throws Exception
    {
        File file = new File( folder.getRoot(), "read.obj" );
        FileOutputStream out = new FileOutputStream( file );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        out.write( text );
        out.close();

        String data = FileUtils.fileRead( file, "UTF8" );
        assertThat( data, is( "Hello /u1234" ) );
    }

    @Test
    @Ignore("Commons test case that is failing for plexus")
    public void readLines()
        throws Exception
    {
        File file = newFile( "lines.txt" );
        try
        {
            String[] data = new String[]{ "hello", "/u1234", "", "this is", "some text" };
            createLineBasedFile( file, data );

            List<String> lines = FileUtils.loadFile( file );
            assertThat( lines, is( Arrays.asList( data ) ) );
        }
        finally
        {
            deleteFile( file );
        }
    }

    @Test
    public void writeStringToFile1()
        throws Exception
    {
        File file = new File( folder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, "UTF8", "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }

    @Test
    public void writeStringToFile2()
        throws Exception
    {
        File file = new File( folder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, null, "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent( text, file );
    }

    @Test
    public void writeCharSequence1()
        throws Exception
    {
        File file = new File( folder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, "UTF8", "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes( "UTF8" );
        assertEqualContent( text, file );
    }

    @Test
    public void writeCharSequence2()
        throws Exception
    {
        File file = new File( folder.getRoot(), "write.txt" );
        FileUtils.fileWrite( file, null, "Hello /u1234" );
        byte[] text = "Hello /u1234".getBytes();
        assertEqualContent( text, file );
    }

    @Test
    public void writeStringToFileWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = newFile( "lines.txt" );
        FileUtils.fileWrite( file, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void writeStringToFile_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = newFile( "lines.txt" );
        FileUtils.fileWrite( file, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void writeWithEncoding_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = newFile( "lines.txt" );
        FileUtils.fileWrite( file, "UTF-8", "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "UTF-8", "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test
    public void write_WithAppendOptionTrue_ShouldNotDeletePreviousFileLines()
        throws Exception
    {
        File file = newFile( "lines.txt" );
        FileUtils.fileWrite( file, "This line was there before you..." );

        FileUtils.fileAppend( file.getAbsolutePath(), "this is brand new data" );

        String expected = "This line was there before you..." + "this is brand new data";
        String actual = FileUtils.fileRead( file );
        assertThat( actual, is( expected ) );
    }

    @Test(expected = NullPointerException.class)
    public void deleteQuietlyForNull()
        throws IOException
    {
        FileUtils.deleteDirectory( (File) null );
    }

    @Test
    public void deleteQuietlyDir()
        throws IOException
    {
        File testDirectory = new File( folder.getRoot(), "testDeleteQuietlyDir" );
        File testFile = new File( testDirectory, "testDeleteQuietlyFile" );
        testDirectory.mkdirs();
        createFile( testFile, 0 );

        assertThat( testDirectory.exists(), is( true ) );
        assertThat( testFile.exists(), is( true ) );
        FileUtils.deleteDirectory( testDirectory );
        assertThat( "Check No Exist", testDirectory.exists(), is( false ) );
        assertThat( "Check No Exist", testFile.exists(), is( false ) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteQuietlyFile()
        throws IOException
    {
        File testFile = new File( folder.getRoot(), "testDeleteQuietlyFile" );
        createFile( testFile, 0 );

        assertThat( testFile.exists(), is( true ) );
        FileUtils.deleteDirectory( testFile );
        assertThat( "Check No Exist", testFile.exists(), is( false ) );
    }

    @Test
    public void deleteQuietlyNonExistent()
        throws IOException
    {
        File testFile = new File(folder.getRoot(), "testDeleteQuietlyNonExistent" );
        assertThat( testFile.exists(), is( false ) );

        FileUtils.deleteDirectory( testFile );
    }

}
