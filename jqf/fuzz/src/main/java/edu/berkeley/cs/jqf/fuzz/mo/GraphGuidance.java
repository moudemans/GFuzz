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

import tudcomponents.MyGraph;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import org.apache.commons.io.FileUtils;
import tudgraphs.GraphMutations;
import tudgraphs.GraphMutator;

import java.io.*;
import java.time.Duration;
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
    private long failedMutation = 0;
    private long invalidStates = 0;

    private final String testClassName;
    private final String testMethodName;

    private long numDiscards = 0;
    private final long maxTrials;
    protected final long maxDurationMillis;
    private final float maxDiscardRatio = 0.9f;

    int MAX_MUTATION_DEPTH = 500;
    boolean USE_MAX_DEPTH = false;
    boolean USE_GENERATION_FOLDER = false;
    int graph_generator = 1; // 0: GMARK, 1: PGMARK

    private final PrintStream out;
    private Random random = new Random();
    private Coverage coverage;
    private static boolean KEEP_GOING_ON_ERROR = true;

    private static final String WORKING_DIR = "fuzz-dir/";
    private static final String SAVED_INPUTS_DIR = "saved-inputs/";
    private static final String RUNNING_DIR = "run-files/";
    private static final String SEED_DIR = "seeds/";
    private static final String NEW_INPUTS_DIR = "new-inputs/";
    private static boolean CLEAR_ALL_PREVIOUS_RESULTS_ON_START = true;


    private static String currentInputFile = "";
    private static String default_mutated_file_location = WORKING_DIR + RUNNING_DIR + "mutated.json";
    private static String nextInputFileLocation = default_mutated_file_location;


    private static ArrayList<String> seed_files = new ArrayList<>();
    Queue<String> priorityFiles = new LinkedList<>();
    private static ArrayList<String> important_files = new ArrayList<>(); // List of files which increase coverage / produce error. This is used to mutate
    private static HashMap<String, Integer> important_files_usage_count = new HashMap<>(); // List of files which increase coverage / produce error. This is used to mutate


    protected Set<List<StackTraceElement>> uniqueFailures = new HashSet<>();
    protected Set<String> uniqueFailuresString = new HashSet<>();
    protected HashMap<Long, String> uniqueFailuresAtTrial = new HashMap<>();


    protected HashMap<String, Set<String>> files_mutated = new HashMap<>();

    private static final boolean PRINT_MUTATION_COUNT = true;
    private static final boolean PRINT_FILE_COUNT = true;
    private static final boolean PRINT_MUTATION_RESPONSIBILITY = true;
    private static final boolean PRINT_UNIQUE_ERRORS = true;
    protected HashMap<GraphMutations.MutationMethod, Integer> mutation_counts = new HashMap<>();
    protected HashMap<String, GraphMutations.MutationMethod> coverage_by_mutation = new HashMap<>();

    protected HashMap<String, HashMap<GraphMutations.MutationMethod, Integer>> error_caused_by_mutation = new HashMap<>();


    GraphMutations.MutationMethod last_mutation_applied = GraphMutations.MutationMethod.NoMutation;

    protected int mutation_framework = 1; // -1 no muitation, 0 random bit mutations, 1 graph mutations, 2 limited graph breaking mutations


    /**
     * Time since this guidance instance was created.
     */
    protected final Date startTime = new Date();
    /**
     * Time at last stats refresh.
     */
    protected Date lastRefreshTime = startTime;
    /**
     * Minimum amount of time (in millis) between two stats refreshes.
     */
    protected static final long STATS_REFRESH_TIME_PERIOD = 10000;
    /**
     * Total execs at last stats refresh.
     */
    protected long lastNumTrials = 0;

    /**
     * The directory where fuzzing results are written.
     */
    protected File outputDirectory;
    protected File seedDirectory;
    protected File runningDirectory;
    protected File savedInputsDirectory;
    protected File newInputsDirectory;

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
     * Creates a NoGuidance instance that will run a maximum number
     * of trials.
     *
     * @param maxTrials      the maximum number of runs to execute
     * @param out            an optional stream for logging error traces
     * @param testClassName
     * @param testMethodName
     */
    public GraphGuidance(long maxTrials, PrintStream out, String testClassName, String testMethodName, Duration duration) {
//        if (maxTrials <= 0) {
//            throw new IllegalArgumentException("maxTrials must be greater than 0");
//        }
        this.maxTrials = maxTrials;
        this.maxDurationMillis = duration != null ? duration.toMillis() : Long.MAX_VALUE;

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
            if (!file.isFile()) {
                continue;
            }
            if (!file.getName().endsWith(".json")) {
                continue;
            }

            System.out.printf("loading seed file [%s]: %s --> %s \n", counter, file.getName(), file.getPath());
            seed_files.add(file.getPath());
            priorityFiles.add(file.getPath());
            counter++;

        }
        if (counter == 1) {
            System.err.printf("No seeds have been loaded from the dir [%s] \n", seedDirectory.getPath());
            System.exit(-1);
        }

    }

    private void prepareOutputDirectory() throws IOException {

        outputDirectory = new File(WORKING_DIR);

        System.out.println("Preparing output directories ...");

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
        if (!savedInputsDirectory.exists() && !this.savedInputsDirectory.mkdirs()) {
            System.out.println("!! Could not create directory: " + savedInputsDirectory);
        }

        this.newInputsDirectory = new File(outputDirectory, NEW_INPUTS_DIR);
        if (!newInputsDirectory.exists() && !this.newInputsDirectory.mkdirs()) {
            System.out.println("!! Could not create directory: " + newInputsDirectory);
        }

        if (CLEAR_ALL_PREVIOUS_RESULTS_ON_START && outputDirectory.isDirectory()) {
            try {
                System.out.println("\t Clearing previous output results");
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

        // If the queue of input files is empty, use the relevant files for a new mutation
        if (!priorityFiles.isEmpty()) {
            currentInputFile = priorityFiles.poll();
            System.out.println("Using priority file: " + currentInputFile);
            nextInputFileLocation = currentInputFile;
            last_mutation_applied = GraphMutations.MutationMethod.NoMutation;
        } else if (!important_files.isEmpty()) {
            //select random file from files which discovered new coverage
            int random_seed = random.nextInt(important_files.size());
            currentInputFile = important_files.get(random_seed);

            if (!important_files_usage_count.containsKey(currentInputFile)) {
                important_files_usage_count.put(currentInputFile, 0);
            }

            important_files_usage_count.put(currentInputFile, important_files_usage_count.get(currentInputFile)+1);

            if (USE_MAX_DEPTH && important_files_usage_count.get(currentInputFile) > MAX_MUTATION_DEPTH) {
                important_files.remove(currentInputFile);
            }

            // Mutate said file
            mutate_current_file();
        } else if (USE_GENERATION_FOLDER) {
            File[] sample_inputs = newInputsDirectory.listFiles();
            if (sample_inputs == null || sample_inputs.length == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Waiting for new inputs was interupted");
                }
            }
            File[] new_inputs = newInputsDirectory.listFiles();
            if (new_inputs == null || new_inputs.length == 0) {
                System.err.printf("No new inputs from the input folder found, is the generator running? Otherwise disable new inputs variable\n");
                keepGoing = false;
            } else {
                int random_input_index = random.nextInt(new_inputs.length);

                MyGraph g = null;
                try {
                    if (graph_generator == 0) {
                        g = MyGraph.readGraphFromGMARK(new_inputs[random_input_index].getPath());
                    } else {
                        g = MyGraph.readGraphFromPGMARK(new_inputs[random_input_index].getPath());
                    }
                } catch (Exception e) {
                    System.err.println("Could not load new file from new input folder: " + new_inputs[random_input_index].getPath());
                }

                if (g == null) {
                    System.err.println("Could not load new file from new input folder: " + new_inputs[random_input_index].getPath());
                }
                String output_file_name = new_inputs[random_input_index].getName().split("\\.")[0] + ".json";
                String output_folder = WORKING_DIR + RUNNING_DIR;
                System.out.println("Writing new input to: " + output_folder + output_file_name);
                MyGraph.writeGraphToJSON(output_folder + output_file_name, g);

                if (new_inputs[random_input_index].exists()){
                    boolean succes = new_inputs[random_input_index].delete();
                    if (!succes) {
                        System.err.println("Could not delete old input file: " + new_inputs[random_input_index].getPath());
                    }
                }

                currentInputFile = output_folder + output_file_name;
//                System.out.println("File used: " + currentInputFile);
            }

        }
        else {
            int random_seed = random.nextInt(seed_files.size());
            currentInputFile = seed_files.get(random_seed);
            // Mutate said file
            mutate_current_file();
        }

        if (numTrials < 2) {
            initialise_classes();
        }

        ArrayList<String> out_debug = new ArrayList<>();
        out_debug.add("file_used: " + currentInputFile);
        out_debug.add("last_mutation_applied: " + last_mutation_applied);
        out_debug.add("next_file: " + nextInputFileLocation);
        write_array_to_file(out_debug );

        // DEFAULT TO: WORKING_DIR + RUNNING_DIR + "mutated.ser"
        InputStream targetStream = new ByteArrayInputStream(nextInputFileLocation.getBytes());
        return targetStream;
    }

    // Method to touch each branch to prevent incorrect code coverage
    private void initialise_classes() {
        MyGraph currentGraph;
        try {
//            currentGraph = MyGraph.readGraphFromFile(currentInputFile);
            currentGraph = MyGraph.readGraphFromJSON(currentInputFile);
        } catch (Exception e) {
            return;
        }
        if (mutation_framework == 0) {
            GraphMutator.ByteMutationLimit(currentGraph, 0);
            GraphMutator.ByteMutationLimit(currentGraph, 1);
            GraphMutator.ByteMutationLimit(currentGraph, 2);
            GraphMutator.ByteMutationLimit(currentGraph, 3);
            GraphMutator.ByteMutationLimit(currentGraph, 4);
            GraphMutator.ByteMutationLimit(currentGraph, 5);
        } else if (mutation_framework == 1) {
            for (GraphMutations.MutationMethod mm : GraphMutations.getActiveMutationMethodList()) {
                GraphMutator.mutateGraph(currentGraph, mm);
            }
        }
    }

    private String mutate_current_file() {

        MyGraph currentGraph;
        try {
//            currentGraph = MyGraph.readGraphFromFile(currentInputFile);
            currentGraph = MyGraph.readGraphFromJSON(currentInputFile);
        } catch (Exception e) {
            System.err.printf("An error occured while loading graph from [%s] \n", currentInputFile);
            important_files.remove(currentInputFile);
            return currentInputFile;
        }
        GraphMutations.MutationMethod mutation_applied;


        // If the file has been mutated before, certain mutations should not be reaplied
        if (!files_mutated.containsKey(currentInputFile)) {
            files_mutated.put(currentInputFile, new HashSet<>());
        }

        nextInputFileLocation = default_mutated_file_location;
        if (mutation_framework == -1) {
            mutation_applied = GraphMutations.MutationMethod.NoMutation;
        } else if (mutation_framework == 0) {

//            try {
////                GraphMutator.ByteMutationToFile(currentGraph, nextInputFileLocation);
//                currentGraph = GraphMutator.ByteMutationLimit(currentGraph);
//                mutation_applied = GraphMutations.MutationMethod.ByteMutation;
//            } catch (IOException e) {
//                mutation_applied = GraphMutations.MutationMethod.NoMutation;
//                invalidStates++;
//            }
            boolean succes = GraphMutator.ByteMutationLimit(currentGraph, -1);
            if (succes) {
                mutation_applied = GraphMutations.MutationMethod.ByteMutation;
            } else {
                mutation_applied = GraphMutations.MutationMethod.NoMutation;
                invalidStates++;
            }
            if (!mutation_counts.containsKey(mutation_applied)) {
                mutation_counts.put(mutation_applied, 0);
            }
            mutation_counts.put(mutation_applied, mutation_counts.get(mutation_applied) + 1);
            last_mutation_applied = mutation_applied;
//            return nextInputFileLocation;

        } else if (mutation_framework == 1) { // no restrictions on mutations
            mutation_applied = GraphMutator.mutateGraph(currentGraph, null);
        } else if (mutation_framework == 2) {
            String mutation_message = GraphMutator.mutateGraphLimit(currentGraph, files_mutated.get(currentInputFile));
            if (mutation_message.contains("$")) {
                mutation_applied = GraphMutations.MutationMethod.valueOf(mutation_message.split("$")[0]);
                files_mutated.get(currentInputFile).add(mutation_message);
            } else {
                mutation_applied = GraphMutations.MutationMethod.NoMutation;
            }
        } else { //
            mutation_applied = GraphMutations.MutationMethod.NoMutation;
            System.err.println("Invalid mutation framework selected: " + mutation_framework);
        }

        if (mutation_applied == GraphMutations.MutationMethod.NoMutation) {
            failedMutation++;
        }

//        MyGraph.writeGraphToFile(nextInputFileLocation, currentGraph);
        MyGraph.writeGraphToJSON(nextInputFileLocation, currentGraph);

        if (!mutation_counts.containsKey(mutation_applied)) {
            mutation_counts.put(mutation_applied, 0);
        }
        mutation_counts.put(mutation_applied, mutation_counts.get(mutation_applied) + 1);
        last_mutation_applied = mutation_applied;
//        System.out.println("mutation applied: " + mutation_applied);

        return nextInputFileLocation;
    }

    /**
     * Returns <code>true</code> as long as <code>maxTrials</code> has not been reached.
     *
     * @return <code>true</code> as long as <code>maxTrials</code> has not been reached
     */
    @Override
    public boolean hasInput() {
        return keepGoing;
    }

    /**
     * Handles the result of a fuzz run.
     *
     * @param result the result of the fuzzing trial
     * @param error  the error thrown during the trial, or <code>null</code>
     */
    @Override
    public void handleResult(Result result, Throwable error) {

        process_result_value(result, error);

        process_stopping_criteria();


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

    private void process_stopping_criteria() {
        // Stopping criteria
        if (maxTrials > 0 && numTrials >= maxTrials) {
            System.out.println("Max trials has been reached");
            this.keepGoing = false;
        }

        long elapsedMillis = new Date().getTime() - startTime.getTime();
        if (elapsedMillis >= this.maxDurationMillis) {
            this.keepGoing = false;
        }

        if (numTrials > 10 && ((float) numDiscards) / ((float) (numTrials)) > maxDiscardRatio) {
            throw new GuidanceException("Assumption is too strong; too many inputs discarded");
        }

        if (!keepGoing) {
            write_log_to_file();
        }
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
            traceElementsString += testProgramTraceElements.get(i).toString();
        }

        if (!error_caused_by_mutation.containsKey(traceElementsString)) {
            error_caused_by_mutation.put(traceElementsString, new HashMap<>());
        }
        if (!error_caused_by_mutation.get(traceElementsString).containsKey(last_mutation_applied)) {
            error_caused_by_mutation.get(traceElementsString).put(last_mutation_applied, 0);
        }
        error_caused_by_mutation.get(traceElementsString).put(last_mutation_applied, error_caused_by_mutation.get(traceElementsString).get(last_mutation_applied) + 1);

        //   Attempt to add this to the set of unique failures
        if (!uniqueFailuresString.contains(traceElementsString)) {
            uniqueFailures.add(testProgramTraceElements);
            uniqueFailuresString.add(traceElementsString);
            uniqueFailuresAtTrial.put(numTrials, traceElementsString);
            System.out.println("New unique error found!");
            System.out.println(traceElementsString);
            System.out.println("****");
            if (uniqueFailuresString.size() > 10000) {
                System.err.println("Found more than 10.000x unique errors, check program and outputs");
                keepGoing = false;
            }
            saveCurrentInput("error");
        }
    }

    private void saveCurrentInput(String type) {
        String dest_folder = savedInputsDirectory.getPath();
        String new_file_name = type + "_" + numTrials + ".json";

        try {
            System.out.printf("Saving last used input [%s], applied mutation [%s] to [%s] \n", nextInputFileLocation, last_mutation_applied, new_file_name);
            FileUtils.copyFile(new File(nextInputFileLocation), new File(dest_folder, new_file_name));
//            Files.copy(new File(nextInputFileLocation), new File(dest_folder, new_file_name));
//            copy_file(nextInputFileLocation, dest_folder + "/" + new_file_name);
        } catch (IOException e) {
            System.err.println("Could not copy file which produced new coverage to the saved inputs dir");
            System.exit(-1);
        }

        coverage_by_mutation.put(new_file_name, last_mutation_applied);
        if (!type.equals("error")) {
            important_files.add(dest_folder + "/" + new_file_name);
            priorityFiles.add(dest_folder + "/" + new_file_name);
        }
    }

    public void copy_file(String src, String dst) throws IOException {

        File src_file = new File(src);
        File dst_file = new File(dst);
        try (
                InputStream in = new BufferedInputStream(
                        new FileInputStream(src_file));
                OutputStream out = new BufferedOutputStream(
                        new FileOutputStream(dst_file))) {

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, lengthRead);
                out.flush();
            }
        }
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
        console.printf("\tFailed mutations:       %,d\n", failedMutation);
        console.printf("\tInvalid states:       %,d\n", invalidStates);
        console.printf("\tNum discards:       %,d\n", numDiscards);
        if (PRINT_MUTATION_COUNT) {
            console.printf("\tmutation counts:       \n");
            for (GraphMutations.MutationMethod mm :
                    mutation_counts.keySet()) {
                console.printf("\t\t %s: %,d\n", mm.toString(), mutation_counts.get(mm));

            }
        }
        if (PRINT_FILE_COUNT) {
            console.printf("\tfiles used counts:       \n");
            List<String> files = new ArrayList<>(important_files_usage_count.keySet());
            String[] files_array = files.toArray(new String[important_files_usage_count.size()]);
            Arrays.sort(files_array, new Comparator<String>() {
                public int compare(String str1, String str2) {
                    if (!str1.contains("_") || !str2.contains("_")) {
                        return 1;
                    }
                    String substr1 = str1.split("_")[1].split("\\.")[0];
                    String substr2 = str2.split("_")[1].split("\\.")[0];


                    return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
                }
            });

            for (String f :
                    files_array) {
                console.printf("\t\t %s: %,d\n", f, important_files_usage_count.get(f));

            }
        }
        if (PRINT_MUTATION_RESPONSIBILITY) {
            console.printf("\tSaved inputs:       \n");
            String[] sorted_keys = coverage_by_mutation.keySet().toArray(new String[0]);
//            sorted_keys.sort(String.CASE_INSENSITIVE_ORDER);
            Arrays.sort(sorted_keys, new Comparator<String>() {
                public int compare(String str1, String str2) {
                    if (str1.contains("fuzz") || str2.contains("fuzz")) {
                        return 1;
                    }

                    if (!str1.contains("_") || !str2.contains("_")) {
                        return -1;
                    }
                    String substr1 = str1.split("_")[1].split("\\.")[0];
                    String substr2 = str2.split("_")[1].split("\\.")[0];

                    return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
                }
            });

            if (sorted_keys.length <= 5) {
                for (String f :
                        sorted_keys) {
                    console.printf("\t\t %s, created by mutation: %s\n", f, coverage_by_mutation.get(f));

                }
            } else {
                console.printf("\t\t ...\n");
                for (int i = sorted_keys.length - 5; i < sorted_keys.length; i++) {
                    String f = sorted_keys[i];
                    console.printf("\t\t %s, created by mutation: %s\n", f, coverage_by_mutation.get(f));
                }
            }
        }

        console.println();

    }

    public void write_array_to_file(ArrayList<String> text) {
        try {
            FileWriter myWriter = new FileWriter(runningDirectory.getPath() + "/last_mutation.txt");
            myWriter.write("FUZZ LOG: " + testClassName + " - " + testMethodName + "\n");
            for (String s : text) {
                myWriter.write(s + "\n");
            }
            myWriter.close();
//            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void write_log_to_file() {
        System.out.println("Collecting statistics");

        Date now = new Date();

        lastRefreshTime = now;
        lastNumTrials = numTrials;
        long elapsedMilliseconds = now.getTime() - startTime.getTime();


        int nonZeroCount = totalCoverage.getNonZeroCount();
        double nonZeroFraction = nonZeroCount * 100.0 / totalCoverage.size();

        ArrayList<String> output = new ArrayList<>();
        output.add(String.format("\tMutation framework used:         %s \n", mutation_framework));
        output.add(String.format("\tElapsed time:         %s \n", millisToDuration(elapsedMilliseconds)));
        output.add(String.format("\tNumber of executions: %,d\n", numTrials));
        output.add(String.format("\tTotal coverage:       %,d branches (%.2f%% of map)\n", nonZeroCount, nonZeroFraction));
        output.add(String.format("\tFailed mutations:       %,d\n", failedMutation));
        output.add(String.format("\tInvalid states:       %,d\n", invalidStates));
        output.add(String.format("\tNum discards:       %,d\n", numDiscards));
        if (PRINT_MUTATION_COUNT) {
            output.add(String.format("\tmutation counts:       \n"));
            for (GraphMutations.MutationMethod mm :
                    mutation_counts.keySet()) {
                output.add(String.format("\t\t %s: %,d\n", mm.toString(), mutation_counts.get(mm)));

            }
        }
        if (PRINT_MUTATION_RESPONSIBILITY) {
            output.add(String.format("\tSaved inputs:       \n"));
            for (String f :
                    coverage_by_mutation.keySet()) {
                output.add(String.format("\t\t %s, created by mutation: %s\n", f, coverage_by_mutation.get(f)));

            }
        }

        output.add("\n\n Unique failures found: ");
        for (String s :
                uniqueFailuresString) {
            output.add("\n\t" + s);
        }


        if (PRINT_UNIQUE_ERRORS) {
            output.add("\n\tUniqueErrors:       \n");
            ArrayList<Long> sorted_keys = new ArrayList<>(uniqueFailuresAtTrial.keySet());
            Collections.sort(sorted_keys);

            for (Long f :
                    sorted_keys) {
                output.add(String.format("\t\t Unique error at [%s], error message: %s\n", f, uniqueFailuresAtTrial.get(f)));
            }
        }

        if (PRINT_UNIQUE_ERRORS) {
            output.add("\n*** UniqueErrors caused by: ***       \n");
            ArrayList<Long> sorted_keys = new ArrayList<>(uniqueFailuresAtTrial.keySet());
            Collections.sort(sorted_keys);

            for (Long f :
                    sorted_keys) {
                output.add(String.format("\tUnique error: [%s]\n", uniqueFailuresAtTrial.get(f)));
                if (!error_caused_by_mutation.containsKey(uniqueFailuresAtTrial.get(f))) {
                    output.add("\t\tCould not find mutations that caused this error\n");
                    continue;
                }
                ArrayList<GraphMutations.MutationMethod> caused_by_mutations = new ArrayList<>(error_caused_by_mutation.get(uniqueFailuresAtTrial.get(f)).keySet());
                for (GraphMutations.MutationMethod mm : caused_by_mutations) {
                    output.add(String.format("\t\t [%s]: %s\n", mm.toString(), error_caused_by_mutation.get(uniqueFailuresAtTrial.get(f)).get(mm)));
                }
            }
        }

        if (PRINT_FILE_COUNT) {
            output.add(String.format(("\tfiles used counts:       \n")));
            List<String> files = new ArrayList<>(important_files_usage_count.keySet());
            String[] files_array = files.toArray(new String[important_files_usage_count.size()]);
            Arrays.sort(files_array, new Comparator<String>() {
                public int compare(String str1, String str2) {
                    if (!str1.contains("_") || !str2.contains("_")) {
                        return 1;
                    }
                    String substr1 = str1.split("_")[1].split("\\.")[0];
                    String substr2 = str2.split("_")[1].split("\\.")[0];


                    return Integer.valueOf(substr1).compareTo(Integer.valueOf(substr2));
                }
            });

            for (String f :
                    files_array) {
                output.add(String.format("\t\t %s: %,d\n", f, important_files_usage_count.get(f)));

            }
        }


        try {
            FileWriter myWriter = new FileWriter(savedInputsDirectory.getPath() + "/fuzz-log.txt");
            myWriter.write("FUZZ LOG: " + testClassName + " - " + testMethodName + "\n");
            for (String s : output) {
                myWriter.write(s);
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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
//                error.printStackTrace(out);
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
     *
     * @return a reference to the coverage statistics
     */
    public Coverage getCoverage() {
        if (runCoverage == null) {
            runCoverage = new Coverage();
        }
        return runCoverage;
    }
}
