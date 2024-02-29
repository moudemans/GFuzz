
package edu.berkeley.cs.jqf.fuzz.gdbFuzz;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.GraphUtil;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.Components.MyGraph;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;

/**
 * A front-end that only generates random inputs.
 *
 * <p>This class provides no guidance to quickcheck. It seeds random values from
 * {@link Random}, making it effectively an unguided random test input
 * generator.
 */
public class NoGuidance implements Guidance {



    /**
     * Coverage statistics for a single run.
     */
    protected Coverage runCoverage = new Coverage();

    /**
     * Cumulative coverage statistics.
     */
    protected Coverage totalCoverage = new Coverage();

    /**
     * Cumulative coverage for valid inputs.
     */
    protected Coverage validCoverage = new Coverage();

    /**
     * The number of valid inputs.
     */
    protected long numValid = 0;
    /**
     * The maximum number of keys covered by any single input found so far.
     */
    protected int maxCoverage = 0;

    /**
     * The set of unique failures found so far.
     */
    protected Set<List<StackTraceElement>> uniqueFailures = new HashSet<>();
    protected HashMap<ArrayList<StackTraceElement>, Long> uniqueFailuresWithTrial = new HashMap<ArrayList<StackTraceElement>, Long>();


    /**
     * The list of total failures found so far.
     */
    protected int totalFailures = 0;

    /**
     * Validity fuzzing -- if true then save valid inputs that increase valid coverage
     */
    protected boolean validityFuzzing;

    private boolean keepGoing = true;
    private long numTrials = 0;
    private long numDiscards = 0;
    private final long maxTrials;
    private final float maxDiscardRatio = 0.9f;
    private final PrintStream out;
    private Random random = new Random();
    private Coverage coverage;
    private static boolean KEEP_GOING_ON_ERROR = true;
    String initialFile = "ABCDEFGHIGK";
    ArrayList<String> testFiles = new ArrayList<String>();
    //System.out.println(initialString);
    //InputStream input;
    //input.read();
    byte[] bytes = initialFile.getBytes();
    String currentFile;
    int v=0;
    //try
    //{
    //System.out.println("UTF8");
    //targetStream = new ByteArrayInputStream(initialString.getBytes());
    /**
     * Creates a NoGuidance instance that will run a maximum number
     * of trials.
     *
     * @param maxTrials the maximum number of runs to execute
     * @param out an optional stream for logging error traces
     */
    public NoGuidance(String initialFile, long maxTrials, PrintStream out) {
        if (maxTrials <= 0) {
            throw new IllegalArgumentException("maxTrials must be greater than 0");
        }
        this.maxTrials = maxTrials;
        this.out = out;
        this.initialFile = initialFile;
        this.currentFile = initialFile;
        testFiles.add(initialFile);
    }
    /**
     * Returns a stream of random numbers
     *
     * @return An infinitely long input stream that generates random numbers
     */
    @Override
    public InputStream getInput() {
        if (numTrials % 1000 == 0) {
            System.out.println("\t  At trial: " + numTrials + " + passing file: " + currentFile);
        }

        // Clear coverage stats for this run
        runCoverage = new Coverage();

        // TODO : Add mutations
        MyGraph currentGraph = MyGraph.readGraphFromFile(currentFile);
        MyGraph mutation = GraphUtil.byteMutation(currentGraph, 1, random);

        String output_file_name ="" ;
        if(currentFile.equals(initialFile)) {
            String[] path = initialFile.split("/");
            for(String s: path) {
                output_file_name += s + "/";
            }
            output_file_name += "fuzzedInput.txt";

        }
        MyGraph.writeGraphToFile(output_file_name, mutation);

        InputStream targetStream = new ByteArrayInputStream(output_file_name.getBytes());
//        saveInput();
        return targetStream;
    }

    /**
     * Returns <tt>true</tt> as long as <tt>maxTrials</tt> has not been reached.
     * @return <tt>true</tt> as long as <tt>maxTrials</tt> has not been reached
     */
    @Override
    public boolean hasInput() {
        return keepGoing;
    }

    /**
     * Handles the result of a fuzz run.
     *
     * @param result   the result of the fuzzing trial
     * @param error    the error thrown during the trial, or <tt>null</tt>
     */
    @Override
    public void handleResult(Result result, Throwable error) {
        numTrials++;
        if (numTrials % 1000 == 0) {
            System.out.println("Handling result ");
        }

        // Display error stack trace in case of failure
        if (result == Result.FAILURE) {
            if (out != null) {
                error.printStackTrace(out);
            }
            this.keepGoing = KEEP_GOING_ON_ERROR;
        }

        boolean valid = result == Result.SUCCESS;

        if (valid) {
            // Increment valid counter
            this.numValid++;
        }

        // Keep track of discards
        if (result == Result.INVALID) {
            numDiscards++;
        }

        // Keep track of discards
        if (result == Result.INVALID) {
            numDiscards++;
        }

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

            // Compute a list of keys for which this input can assume responsiblity.
            // Newly covered branches are always included.
            // Existing branches *may* be included, depending on the heuristics used.
            // A valid input will steal responsibility from invalid inputs
//            Set<Object> responsibilities = computeResponsibilities(valid);
            //System.out.println("Responsibilities of this input: "+responsibilities);

            // Update total coverage
            totalCoverage.updateBits(runCoverage);
            if (valid) {
                validCoverage.updateBits(runCoverage);
            }

            // Coverage after
            int nonZeroAfter = totalCoverage.getNonZeroCount();
            if (nonZeroAfter > maxCoverage) {
                maxCoverage = nonZeroAfter;
            }



        } else if (result == Result.FAILURE || result == Result.TIMEOUT) {

            String msg = error.getMessage();

            //get the root cause
            Throwable rootCause = error;
            while (rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            this.totalFailures++;


            // Only check the Stack trace elements until the program driver that is being tested
            ArrayList<StackTraceElement> testProgramTraceElements = new ArrayList<>();

            for (int i = 0; i < rootCause.getStackTrace().length; i++) {
                testProgramTraceElements.add(rootCause.getStackTrace()[i]);

            }

            if(!uniqueFailuresWithTrial.containsKey(testProgramTraceElements)) {
                uniqueFailures.add(testProgramTraceElements);
                uniqueFailuresWithTrial.put(testProgramTraceElements, numTrials);
            }
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
        System.out.println(String.format("Thread %s produced event", thread.getName()));

        return this::handleEvent;
    }

    protected void handleEvent(TraceEvent e) {
        // Collect totalCoverage
        runCoverage.handleEvent(e);
    }

    /**
     * Returns a reference to the coverage statistics.
     * @return a reference to the coverage statistics
     */
    public Coverage getCoverage() {
        if (coverage == null) {
            coverage = new Coverage();
        }
        return coverage;
    }

}
