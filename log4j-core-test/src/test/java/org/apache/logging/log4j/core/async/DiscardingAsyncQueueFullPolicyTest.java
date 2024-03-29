/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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
package org.apache.logging.log4j.core.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.test.categories.AsyncLoggers;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the DiscardingAsyncQueueFullPolicy class..
 */
@Category(AsyncLoggers.class)
public class DiscardingAsyncQueueFullPolicyTest {

    private static long currentThreadId() {
        return Thread.currentThread().getId();
    }

    private static long otherThreadId() {
        return -1;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorDisallowsNullThresholdLevel() {
        new DiscardingAsyncQueueFullPolicy(null);
    }

    @Test
    public void testThresholdLevelIsConstructorValue() {
        assertSame(Level.ALL, new DiscardingAsyncQueueFullPolicy(Level.ALL).getThresholdLevel());
        assertSame(Level.OFF, new DiscardingAsyncQueueFullPolicy(Level.OFF).getThresholdLevel());
        assertSame(Level.INFO, new DiscardingAsyncQueueFullPolicy(Level.INFO).getThresholdLevel());
    }

    @Test
    public void testGetRouteDiscardsIfThresholdCapacityReachedAndLevelEqualOrLessSpecificThanThreshold()
            throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.WARN);

        for (final Level level : new Level[] {Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL}) {
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(currentThreadId(), level));
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(otherThreadId(), level));
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(currentThreadId(), level));
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(otherThreadId(), level));
        }
    }

    @Test
    public void testGetRouteDiscardsIfQueueFullAndLevelEqualOrLessSpecificThanThreshold() throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.WARN);

        for (final Level level : new Level[] {Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL}) {
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(currentThreadId(), level));
            assertEquals(level.name(), EventRoute.DISCARD, router.getRoute(otherThreadId(), level));
        }
    }

    @Test
    public void testGetRouteEnqueuesIfThresholdCapacityReachedButLevelMoreSpecificThanThreshold() throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.WARN);

        for (final Level level : new Level[] {Level.ERROR, Level.FATAL, Level.OFF}) {
            assertEquals(level.name(), EventRoute.SYNCHRONOUS, router.getRoute(currentThreadId(), level));
            assertEquals(level.name(), EventRoute.ENQUEUE, router.getRoute(otherThreadId(), level));
            assertEquals(level.name(), EventRoute.SYNCHRONOUS, router.getRoute(currentThreadId(), level));
            assertEquals(level.name(), EventRoute.ENQUEUE, router.getRoute(otherThreadId(), level));
        }
    }

    @Test
    public void testGetRouteEnqueueIfOtherThreadQueueFullAndLevelMoreSpecificThanThreshold() throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.WARN);

        for (final Level level : new Level[] {Level.ERROR, Level.FATAL, Level.OFF}) {
            assertEquals(level.name(), EventRoute.ENQUEUE, router.getRoute(otherThreadId(), level));
        }
    }

    @Test
    public void testGetRouteSynchronousIfCurrentThreadQueueFullAndLevelMoreSpecificThanThreshold() throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.WARN);

        for (final Level level : new Level[] {Level.ERROR, Level.FATAL, Level.OFF}) {
            assertEquals(level.name(), EventRoute.SYNCHRONOUS, router.getRoute(currentThreadId(), level));
        }
    }

    @Test
    public void testGetDiscardCount() throws Exception {
        final DiscardingAsyncQueueFullPolicy router = new DiscardingAsyncQueueFullPolicy(Level.INFO);
        assertEquals("initially", 0, DiscardingAsyncQueueFullPolicy.getDiscardCount(router));

        assertEquals(EventRoute.DISCARD, router.getRoute(-1L, Level.INFO));
        assertEquals("increase", 1, DiscardingAsyncQueueFullPolicy.getDiscardCount(router));

        assertEquals(EventRoute.DISCARD, router.getRoute(-1L, Level.INFO));
        assertEquals("increase", 2, DiscardingAsyncQueueFullPolicy.getDiscardCount(router));

        assertEquals(EventRoute.DISCARD, router.getRoute(-1L, Level.INFO));
        assertEquals("increase", 3, DiscardingAsyncQueueFullPolicy.getDiscardCount(router));
    }
}
