/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.collector.events.hadoop.writer;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestFileError
{
    private static final String PATH = "/events/foo.thrift";
    private static final IOException EXCEPTION = new IOException();
    private FileError error;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception
    {
        error = new FileError(PATH, EXCEPTION);
    }

    @Test(groups = "fast")
    public void testGetFilename() throws Exception
    {
        Assert.assertEquals(error.getFilename(), PATH);
    }

    @Test(groups = "fast")
    public void testGetException() throws Exception
    {
        Assert.assertEquals(error.getException(), EXCEPTION);
    }
}
