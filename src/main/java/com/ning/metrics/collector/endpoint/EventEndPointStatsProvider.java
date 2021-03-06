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

package com.ning.metrics.collector.endpoint;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ning.metrics.collector.binder.config.CollectorConfig;

public class EventEndPointStatsProvider implements Provider<EventEndPointStats>
{
    private final CollectorConfig config;

    @Inject
    public EventEndPointStatsProvider(final CollectorConfig config)
    {
        this.config = config;
    }

    @Override
    public EventEndPointStats get()
    {
        return new EventEndPointStats(config.getRateWindowSizeMinutes());
    }
}
