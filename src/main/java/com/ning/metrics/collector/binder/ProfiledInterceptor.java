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

package com.ning.metrics.collector.binder;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;
import org.perf4j.aop.Profiled;

public class ProfiledInterceptor implements MethodInterceptor
{
    public Object invoke(final MethodInvocation invocation) throws Throwable
    {
        final Profiled profiled = invocation.getMethod().getAnnotation(Profiled.class);
        final StopWatch stopWatch = new LoggingStopWatch(profiled.tag(), profiled.message());

        try {
            return invocation.proceed();
        }
        finally {
            stopWatch.stop();
        }
    }
}
