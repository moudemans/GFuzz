/*
 * Copyright (c) 2017-2018 The Regents of the University of California
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.cs.jqf.fuzz.mo;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A front-end that only generates random inputs.
 *
 * <p>This class provides no guidance to quickcheck. It seeds random values from
 * {@link Random}, making it effectively an unguided random test input
 * generator.
 */
public class NoGuidance implements Guidance {

    private boolean keepGoing = true;
    private long numTrials = 0;

    private long numDiscards = 0;
    private final long maxTrials;
    private final float maxDiscardRatio = 0.9f;
    private final PrintStream out;
    private Random random = new Random();
    private Coverage coverage;
    private static boolean KEEP_GOING_ON_ERROR = true;



    /** Time since this guidance instance was created. */
    protected final Date startTime = new Date();
    /** Time at last stats refresh. */
    protected Date lastRefreshTime = startTime;
    /** Minimum amount of time (in millis) between two stats refreshes. */
    protected static final long STATS_REFRESH_TIME_PERIOD = 10000;
    /** Total execs at last stats refresh. */
    protected long lastNumTrials = 0;





    /** Coverage statistics for a single run. */
    protected Coverage runCoverage = new Coverage();

    /** Cumulative coverage statistics. */
    protected Coverage totalCoverage = new Coverage();

    /** Cumulative coverage for valid inputs. */
    protected Coverage validCoverage = new Coverage();

    /** Map which tracks the amount of times each known branch is covered */
    protected Map<Set<Integer>, Integer> branchesHitCount = new HashMap<>();


    /**
     * Creates a NoGuidance instance that will run a maximum number
     * of trials.
     *
     * @param maxTrials the maximum number of runs to execute
     * @param out an optional stream for logging error traces
     */
    public NoGuidance(long maxTrials, PrintStream out) {
        if (maxTrials <= 0) {
            throw new IllegalArgumentException("maxTrials must be greater than 0");
        }
        this.maxTrials = maxTrials;
        this.out = out;
    }

    /**
     * Returns a stream of random numbers
     *
     * @return An infinitely long input stream that generates random numbers
     */
    @Override
    public InputStream getInput() {
//        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
//        int val =  random.nextInt(256);
//        System.out.println("Returning input: " +  val);
//        return Guidance.createInputStream(() -> val);

        return new ByteArrayInputStream(("fuzz-dir/seed.txt").getBytes());
    }

    /**
     * Returns <code>true</code> as long as <code>maxTrials</code> has not been reached.
     * @return <code>true</code> as long as <code>maxTrials</code> has not been reached
     */
    @Override
    public boolean hasInput() {
        return keepGoing;
    }

    /**
     * Handles the result of a fuzz run.
     *
     * @param result   the result of the fuzzing trial
     * @param error    the error thrown during the trial, or <code>null</code>
     */
    @Override
    public void handleResult(Result result, Throwable error) {

        process_result_value(result, error);

        // Stopping criteria
        if (numTrials >= maxTrials) {
            this.keepGoing = false;
        }

        if (numTrials > 10 && ((float) numDiscards)/((float) (numTrials)) > maxDiscardRatio) {
            throw new GuidanceException("Assumption is too strong; too many inputs discarded");
        }

        if (result == Result.SUCCESS || result == Result.INVALID) {

            // Coverage before
            int nonZeroBefore = totalCoverage.getNonZeroCount();
            int validNonZeroBefore = validCoverage.getNonZeroCount();

            // Update total coverage
            totalCoverage.updateBits(runCoverage);
            if (result == Result.SUCCESS) {
                validCoverage.updateBits(runCoverage);
            }



        }
        if (true) {
            this.displayStats();
        }



        // Clear coverage stats for this run
        runCoverage.clear();
    }
    private void displayStats() {
        PrintStream console = System.out;

        Date now = new Date();
        long intervalMilliseconds = now.getTime() - lastRefreshTime.getTime();
        if (intervalMilliseconds < STATS_REFRESH_TIME_PERIOD) {
            return;
        }
        long intervalTrials = numTrials - lastNumTrials;
        double intervalExecsPerSecDouble = intervalTrials * 1000.0 / intervalMilliseconds;
        lastRefreshTime = now;
        lastNumTrials = numTrials;
        long elapsedMilliseconds = now.getTime() - startTime.getTime();



        int nonZeroCount = totalCoverage.getNonZeroCount();
        double nonZeroFraction = nonZeroCount * 100.0 / totalCoverage.size();
        int nonZeroValidCount = runCoverage.getNonZeroCount();
        double nonZeroValidFraction = nonZeroValidCount * 100.0 / runCoverage.size();


        console.print("\033[2J");
        console.print("\033[H");
//        console.printf("\tResults directory:    %s\n", this.outputDirectory.getAbsolutePath());
        console.printf("\tElapsed time:         %s \n", millisToDuration(elapsedMilliseconds));
        console.printf("\tNumber of executions: %,d\n", numTrials);
        console.printf("\tTotal coverage:       %,d branches (%.2f%% of map)\n", nonZeroCount, nonZeroFraction);
        console.printf("\trun coverage:       %,d branches (%.2f%% of map)\n", nonZeroValidCount, nonZeroValidFraction);
        console.println();

    }

    private String millisToDuration(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis % TimeUnit.MINUTES.toMillis(1));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis % TimeUnit.HOURS.toMillis(1));
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        String result = "";
        if (hours > 0) {
            result = hours + "h ";
        }
        if (hours > 0 || minutes > 0) {
            result += minutes + "m ";
        }
        result += seconds + "s";
        return result;
    }

    private void process_result_value(Result result, Throwable error) {
        numTrials++;

        // Display error stack trace in case of failure
        if (result == Result.FAILURE) {
            if (out != null) {
                error.printStackTrace(out);
            }
            this.keepGoing = KEEP_GOING_ON_ERROR;
        }

        // Keep track of discards
        if (result == Result.INVALID) {
            numDiscards++;
        }
    }

    /**
     * Returns a callback that does almost nothing.
     *
     * <p>Since this is unguided random guidance, the trace events are
     * not used in generating inputs.</p>
     *
     * <p>The handler here merely updates coverage statistics.</p>
     *
     * @param thread the thread whose events to handle
     * @return a callback that does nothing.
     */
    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        System.out.println("BBBBBBBBBBBBBBBBBBBBB");
        System.out.println(String.format("Thread %s produced event", thread.getName()));
//        return getCoverage()::handleEvent;

        return this::handleEvent;
    }


    protected void handleEvent(TraceEvent e) {
        getCoverage().handleEvent(e);

    }

    /**
     * Returns a reference to the coverage statistics.
     * @return a reference to the coverage statistics
     */
    public Coverage getCoverage() {
        if (runCoverage == null) {
            runCoverage = new Coverage();
        }
        return runCoverage;
    }
}
