/*
 * Copyright 2010 Ning, Inc.
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

package com.ning.metrics.collector.endpoint.resources;

import com.ning.metrics.collector.endpoint.EventEndPointStats;
import com.ning.metrics.collector.endpoint.EventStats;
import com.ning.metrics.collector.endpoint.MockEventHandler;
import com.ning.metrics.collector.endpoint.MockHttpHeaders;
import com.ning.metrics.collector.endpoint.ParsedRequest;
import com.ning.metrics.collector.endpoint.extractors.MockEventExtractor;
import com.ning.metrics.serialization.event.Event;
import com.ning.metrics.serialization.event.StubEvent;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class TestEventRequestHandler
{
    private EventEndPointStats stats = null;
    private MockEventHandler eventHandler = null;
    private MockEventExtractor eventExtractor = null;
    private EventRequestHandler eventRequestHandler = null;
    private EventStats eventStats = null;

    @BeforeMethod(alwaysRun = true)
    void setup()
    {
        eventHandler = new MockEventHandler();
        stats = new EventEndPointStats(5);
        eventExtractor = new MockEventExtractor();
        eventRequestHandler = new EventRequestHandler(eventHandler, eventExtractor, stats);
        eventStats = new EventStats();
    }

    @Test(groups = "fast")
    public void testSuccess() throws Exception
    {
        Event event = createEvent("fuu");
        eventExtractor.setEvent(event);
        Response res = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 1);
        Assert.assertEquals(eventHandler.getProcessedEventList().get(0).getName(), "fuu");

        Assert.assertEquals(res.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        Assert.assertEquals(stats.getTotalEvents(), 1);
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 1);
        Assert.assertEquals(stats.getFailedToParseEvents(), 0);
    }

    @Test(groups = "fast")
    public void testEventExtractorReturnsNull() throws Exception
    {
        eventExtractor.setEvent(null);
        Response res = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        // Test that eventHandler sees the null value
        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 1);
        Assert.assertEquals(eventHandler.getProcessedEventList().get(0), null);

        Assert.assertEquals(res.getStatus(), 202);

        Assert.assertEquals(stats.getTotalEvents(), 1);
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 1);
        Assert.assertEquals(stats.getFailedToParseEvents(), 0);
    }

    @Test(groups = "fast")
    public void testEventExtractorThrowsEventParse() throws Exception
    {
        eventExtractor.setThrowsEventParseException(true);

        Response response = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        Assert.assertEquals(eventHandler.isHandleFailureCalled(), true);
        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 0);

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

        Assert.assertEquals(stats.getTotalEvents(), 0); // if it can't parse the events, it can't count the total events
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 0);
        Assert.assertEquals(stats.getFailedToParseEvents(), 1);
    }

    @Test(groups = "fast")
    public void testEventExtractorThrowsRuntimeException() throws Exception
    {
        eventExtractor.setThrowsRuntimeException(true);

        Response response = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        Assert.assertEquals(eventHandler.isHandleFailureCalled(), true);
        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 0);

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        Assert.assertEquals(stats.getTotalEvents(), 0);
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 0);
        Assert.assertEquals(stats.getFailedToParseEvents(), 1);
    }

    @Test(groups = "fast")
    public void testEventHandlerThrowsBeforeEventProcessed() throws Exception
    {
        eventHandler.setThrowExceptionBeforeEvent(true);

        Response response = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 0);

        // Server only returns statusCode != 202 when there is an issue extracting the events.
        // Not when it fails to process an individual event.
        Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        Assert.assertEquals(stats.getTotalEvents(), 1);
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 0);
        Assert.assertEquals(stats.getFailedToParseEvents(), 1);
    }

    @Test(groups = "fast")
    public void testEventHandlerThrowsAfterEventProcessed() throws Exception
    {
        eventHandler.setThrowExceptionAfterEvent(true);

        Response response = eventRequestHandler.handleEventRequest(createMockRequestAnnotation(), eventStats);

        Assert.assertEquals(eventHandler.getProcessedEventList().size(), 1);

        // Server only returns statusCode != 202 when there is an issue extracting the events.
        // Not when it fails to process an individual event.
        Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());

        Assert.assertEquals(stats.getTotalEvents(), 1);
        Assert.assertEquals(stats.getSuccessfulParseEvents(), 0);
        Assert.assertEquals(stats.getFailedToParseEvents(), 1);
    }

    private Event createEvent(final String description)
    {
        return new StubEvent()
        {
            @Override
            public String getName()
            {
                return description;
            }
        };
    }


    /**
     * The event handler doesn't care about the annotation (only the event extractor does).
     * Generate a dummy one.
     *
     * @return an empty annotation
     */
    private ParsedRequest createMockRequestAnnotation()
    {
        return new ParsedRequest("DummyEvent", new MockHttpHeaders(), null, null, null, null);
    }
}
