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

import Components.MyGraph;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import org.apache.commons.io.FileUtils;
import util.GraphUtil;

import java.io.*;
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
public class GraphGuidance implements Guidance {

    private boolean keepGoing = true;
    private long numTrials = 0;

    private final String testClassName;
    private final String testMethodName;

    private long numDiscards = 0;
    private final long maxTrials;
    private final float maxDiscardRatio = 0.9f;
    private final PrintStream out;
    private Random random = new Random();
    private Coverage coverage;
    private static boolean KEEP_GOING_ON_ERROR = true;

    private static final String WORKING_DIR = "fuzz-dir/";
    private static final String SAVED_INPUTS_DIR = "saved-inputs/";
    private static final String RUNNING_DIR = "run-files/";
    private static final String SEED_DIR = "seeds/";
    private static boolean CLEAR_ALL_PREVIOUS_RESULTS_ON_START = true;


    private static String currentInputFile = "";
    private static String nextInputFileLocation = WORKING_DIR + RUNNING_DIR + "mutated.ser";


    private static ArrayList<String> seed_files = new ArrayList<>();
    Queue<String> inputFiles = new PriorityQueue<>();
    private static ArrayList<String> important_files = new ArrayList<>(); // List of files which increase coverage / produce error. This is used to mutate


    protected Set<List<StackTraceElement>> uniqueFailures = new HashSet<>();
    protected Set<String> uniqueFailuresString = new HashSet<>();






    /** Time since this guidance instance was created. */
    protected final Date startTime = new Date();
    /** Time at last stats refresh. */
    protected Date lastRefreshTime = startTime;
    /** Minimum amount of time (in millis) between two stats refreshes. */
    protected static final long STATS_REFRESH_TIME_PERIOD = 10000;
    /** Total execs at last stats refresh. */
    protected long lastNumTrials = 0;

    /** The directory where fuzzing results are written. */
    protected File outputDirectory;
    protected File seedDirectory;
    protected File runningDirectory;
    protected File savedInputsDirectory;

    /** Coverage statistics for a single run. */
    protected Coverage runCoverage = new Coverage();

    /** Cumulative coverage statistics. */
    protected Coverage totalCoverage = new Coverage();

    /** Cumulative coverage for valid inputs. */
    protected Coverage validCoverage = new Coverage();



    /**
     * Creates a NoGuidance instance that will run a maximum number
     * of trials.
     *
     * @param maxTrials      the maximum number of runs to execute
     * @param out            an optional stream for logging error traces
     * @param testClassName
     * @param testMethodName
     */
    public GraphGuidance(long maxTrials, PrintStream out, String testClassName, String testMethodName) {
        if (maxTrials <= 0) {
            throw new IllegalArgumentException("maxTrials must be greater than 0");
        }
        this.maxTrials = maxTrials;
        this.out = out;
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;


        try {
            System.out.println("Preparing output dir from working directory: " + System.getProperty("user.dir"));
            prepareOutputDirectory();
        } catch (IOException i) {
            System.err.println("Could not start the guidance due to the output directories not being able to be generated");
            System.err.println(i.getMessage());
            System.exit(-1);
        }

        load_seeds();

    }

    private void load_seeds() {
        File[] listOfFiles = seedDirectory.listFiles();

        int counter = 1;
        System.out.println("Files in seed dir: ");
        for (File file : listOfFiles) {
            System.out.println("\t " + file.getPath() + " + " + file.getName());
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(".ser")) {
                continue;
            }

            System.out.printf("loading seed file [%s]: %s --> %s ", counter, file.getName(), file.getPath());
            seed_files.add(file.getPath());
            inputFiles.add(file.getPath());
            counter++;

        }
        if (counter == 1) {
            System.err.printf("No seeds have been loaded from the dir [%s] \n", seedDirectory.getPath() );
            System.exit(-1);
        }

    }

    private void prepareOutputDirectory() throws IOException {

        outputDirectory = new File(WORKING_DIR);

        // Create the output directory if it does not exist
        if (!outputDirectory.exists()) {
                throw new IOException("output directory does not exists" +
                        outputDirectory.getAbsolutePath());

        }




        // Make sure we can write to output directory
        if (!outputDirectory.isDirectory() || !outputDirectory.canWrite()) {
            throw new IOException("Output directory is not a writable directory: " +
                    outputDirectory.getAbsolutePath());
        }


        this.seedDirectory = new File(outputDirectory, SEED_DIR);
        if (!seedDirectory.exists()) {
            throw new IOException("Output directory is not a writable directory: " +
                    seedDirectory);
        }


        this.runningDirectory = new File(outputDirectory, RUNNING_DIR);
        if (!runningDirectory.exists() && !this.runningDirectory.mkdirs()) {
            System.out.println("!! Could not create directory: " + runningDirectory);
        }

        this.savedInputsDirectory = new File(outputDirectory, SAVED_INPUTS_DIR);
        if ( !savedInputsDirectory.exists() &&!this.savedInputsDirectory.mkdirs()) {
            System.out.println("!! Could not create directory: " + savedInputsDirectory);
        }
        if (CLEAR_ALL_PREVIOUS_RESULTS_ON_START && outputDirectory.isDirectory()) {
            try {
                File inputs = new File(outputDirectory, RUNNING_DIR);
                FileUtils.cleanDirectory(inputs);
                File saved = new File(outputDirectory, SAVED_INPUTS_DIR);
                FileUtils.cleanDirectory(saved);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Returns a file location of the next input
     *
     * @return An infinitely long input stream that generates random numbers
     */
    @Override
    public InputStream getInput() {

        if (!inputFiles.isEmpty()) {
            currentInputFile = inputFiles.poll();
        } else {
//            System.err.println("No more input files, mutating seeds");
            int random_seed = random.nextInt(important_files.size());
            currentInputFile = important_files.get(random_seed);
        }


        MyGraph currentGraph = MyGraph.readGraphFromFile(currentInputFile);
        MyGraph mutation = GraphUtil.byteMutation(currentGraph, 1, random);

        MyGraph.writeGraphToFile(nextInputFileLocation, mutation);

        InputStream targetStream = new ByteArrayInputStream(nextInputFileLocation.getBytes());
//        saveInput();
        return targetStream;

//        return new ByteArrayInputStream(("fuzz-dir/seed.txt").getBytes());
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
        System.out.println("RESULTS = " + result);
        if (result == Result.SUCCESS || result == Result.INVALID) {

            // Coverage before
            int nonZeroBefore = totalCoverage.getNonZeroCount();
            int validNonZeroBefore = validCoverage.getNonZeroCount();
            
            

            // Update total coverage
            totalCoverage.updateBits(runCoverage);
            if (result == Result.SUCCESS) {
                validCoverage.updateBits(runCoverage);
            }
            
            if (nonZeroBefore < totalCoverage.getNonZeroCount()) {
                System.out.println("New coverage found!");
                saveCurrentInput("Coverage");
                
                //TODO: If coverage has increased. This file now needs to be mutated again  
            }
        }
        if (result == Result.FAILURE || result == Result.TIMEOUT) {
            handleFailure(result, error);
        }

        if (true) {
            this.displayStats();
        }



        // Clear coverage stats for this run
        runCoverage.clear();
    }

    private void handleFailure(Result result, Throwable error) {
        String msg = error.getMessage();

        //get the root cause
        Throwable rootCause = error;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }


        // Only check the Stack trace elements until the program driver that is being tested
        ArrayList<StackTraceElement> testProgramTraceElements = new ArrayList<>();
        boolean testClassFound = false;
        for (int i = 0; i < rootCause.getStackTrace().length; i++) {
            // If the test class has been found in the stacktrace, but this element is no longer said test class then the stacktrace will only contain the test framework, not the test program.
            if (testClassFound && !rootCause.getStackTrace()[i].getClassName().equals(testClassName)) {
                break;
            }

            testProgramTraceElements.add(rootCause.getStackTrace()[i]);

            // Check the correct element of the stacktrace if it originated from the test class.
            if (rootCause.getStackTrace()[i].getClassName().equals(testClassName)) {
                testClassFound = true;
            }
        }
        String traceElementsString = "";
        for (int i = 0; i < testProgramTraceElements.size(); i++) {
            traceElementsString +=testProgramTraceElements.get(i).toString();
            System.out.println(testProgramTraceElements.get(i).toString());
        }

        //   Attempt to add this to the set of unique failures
//        if(!uniqueFailures.contains(testProgramTraceElements)) {
        if(!uniqueFailuresString.contains(traceElementsString)) {
            uniqueFailures.add(testProgramTraceElements);
            uniqueFailuresString.add(traceElementsString);

            if (uniqueFailuresString.size() > 10000) {
                System.err.println("Found more than 10.000x unique errors, check program and outputs");
                keepGoing = false;
            }
            saveCurrentInput("error");
        }
    }

    private void saveCurrentInput(String type) {
        String dest_folder= savedInputsDirectory.getPath();
        String new_file_name = type + "_" + numTrials + ".ser";

        try {
            FileUtils.copyFile(new File(currentInputFile), new File(dest_folder, new_file_name));
        } catch (IOException e) {
            System.err.println("Could not copy file which produced new coverage to the saved inputs dir");
            System.exit(-1);
        }

        important_files.add(dest_folder+ "/" +new_file_name);
        inputFiles.add(dest_folder+ "/"+new_file_name);
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
