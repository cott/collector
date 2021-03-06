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

package com.ning.metrics.collector.endpoint.extractors;

import com.ning.metrics.collector.endpoint.ExtractedAnnotation;
import com.ning.metrics.serialization.event.Event;
import com.ning.metrics.serialization.event.SmileBucketEvent;
import com.ning.metrics.serialization.event.ThriftEnvelopeEvent;
import com.ning.metrics.serialization.smile.JsonStreamToSmileBucketEvent;
import com.ning.metrics.serialization.thrift.ThriftEnvelope;
import com.ning.metrics.serialization.thrift.ThriftField;
import com.ning.metrics.serialization.thrift.ThriftFieldListParser;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Stream-based API (POST).
 * Serialization is done in the eventtracker library. The shared code for serialization/deserialization
 * is in the com.ning:metrics.serialization-* libraries.
 */
class BodyEventExtractor implements EventExtractor
{
    private static final Logger log = Logger.getLogger(BodyEventExtractor.class);

    @Override
    public Collection<? extends Event> extractEvent(final ExtractedAnnotation annotation) throws EventParsingException
    {
        final String eventType = annotation.getEventName();

        if (eventType != null) {
            if (annotation == null) {
                log.warn("Null annotation");
                throw new EventParsingException("Null annotation");
            }

            log.debug(String.format("receiving event of type %s, content-type %s", eventType, annotation.getContentType()));

            // contentType defaults to ning/thrift, for backwards compatibility
            if (annotation.getContentType() == null || annotation.getContentType().equals("ning/thrift")) {
                final ArrayList<ThriftField> thriftFieldList;

                try {
                    thriftFieldList = new ThriftFieldListParser().parse(annotation.getContentLength(), annotation.getInputStream());
                }
                catch (IOException e) {
                    throw new EventParsingException(String.format("IOException while trying to parse event from post body"), e);
                }
                catch (IllegalArgumentException e) {
                    throw new EventParsingException(String.format("Parse exception while trying to parse event from post body"), e);
                }

                return Collections.singletonList(new ThriftEnvelopeEvent(
                    annotation.getDateTime(),
                    new ThriftEnvelope(eventType, thriftFieldList),
                    annotation.getBucketGranularity()
                ));
            }
            else if (annotation.getContentType().equals(MediaType.APPLICATION_JSON) || annotation.getContentType().equals("application/json+smile")) {
                final Collection<SmileBucketEvent> smileEvents;

                try {
                    // JsonStreamToSmileBucketEvent extracts Json or Smile
                    smileEvents = JsonStreamToSmileBucketEvent.extractEvent(eventType, annotation.getInputStream());
                }
                catch (IOException e) {
                    throw new EventParsingException(String.format("IOException while trying to parse event from post body"), e);
                }

                return smileEvents;
            }
            else {
                log.warn(String.format("Content-Type [%s] not supported", annotation.getContentType()));
            }
        }

        return null;
    }
}
