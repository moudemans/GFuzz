package edu.berkeley.cs.jqf.fuzz.gdbFuzz;

import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.util.Coverage;
import edu.berkeley.cs.jqf.instrument.tracing.events.TraceEvent;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class GGuidance implements Guidance {

    public int numTrials = 0;
    public int maxTrials;

    public String initialFile, currentFile;

    public String inputLocationFile = "edu/berkeley/cs/jqf/fuzz/gdbFuzz/fuzz-input/inputLocation.txt";

    private File currentInputFile;
    private File initialInputFile;
    public File outputDirectory;

    // Coverage
    protected Coverage coverage = new Coverage();

    public GGuidance(String method, File seedFile, long maxTrials, File outputDir, PrintStream out) {
        if (maxTrials <= 0) {
            throw new IllegalArgumentException("maxTrials must be greater than 0");
        }
        this.maxTrials = 1000;

//        this.initialFile = initialFile;
//        this.currentFile = initialFile;

        currentInputFile= seedFile;
        initialInputFile = seedFile;
        this.outputDirectory = outputDir;
    }

    @Override
    public InputStream getInput() throws IllegalStateException, GuidanceException {
        numTrials++;
        if (numTrials % 100 == 0) {
            System.out.println("\t  At trial: " + numTrials + " + passing file: " + currentFile);
        }
        return new ByteArrayInputStream(("fuzz/src/main/java/" + inputLocationFile).getBytes());
    }

    @Override
    public boolean hasInput() {
        if(numTrials > maxTrials) {
            return false;
        }
        return true;
    }

    @Override
    public void observeGeneratedArgs(Object[] args) {
        for (Object o :
                args) {
            if(o instanceof String) {
                System.out.println("arg: " + (String) o);
            }
        }
    }

    @Override
    public void handleResult(Result result, Throwable error) throws GuidanceException {
        System.out.println("Calling Handle Result");
    }

    @Override
    public Consumer<TraceEvent> generateCallBack(Thread thread) {
        System.out.println(String.format("Thread %s produced event", thread.getName()));

        return this::handleEvent;
    }

    protected void handleEvent(TraceEvent e) {
        // Collect totalCoverage
        coverage.handleEvent(e);
    }


}
