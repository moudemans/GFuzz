package edu.berkeley.cs.jqf.fuzz.gdbFuzz;

import edu.berkeley.cs.jqf.fuzz.ei.ZestGuidance;
import edu.berkeley.cs.jqf.fuzz.gdbFuzz.examples.P2LabelAlgorithm;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import org.junit.runner.Result;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class GFuzzDriver {

    public static int guidanceMethod = 0; // 0 == Zest, 1 == Graph, 2 == Random

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java " + GFuzzDriver.class + " TEST_CLASS TEST_METHOD [OUTPUT_DIR [SEED_DIR | SEED_FILES...]]");
            System.exit(1);
        }

        P2LabelAlgorithm p = new P2LabelAlgorithm();

        System.out.println("Parsing program arguments... ");
        String testClassName = args[0];
        String testMethodName = args[1];
        String outputDirectoryName = args.length > 2 ? args[2] : "fuzz-results";
        File outputDirectory = new File(outputDirectoryName);
        File[] seedFiles = null;
        if (args.length > 3) {
            seedFiles = new File[args.length - 3];
            for (int i = 3; i < args.length; i++) {
                seedFiles[i - 3] = new File(args[i]);
            }
        }
        System.out.println("\t Class name: " + testClassName);
        System.out.println("\t Method name: " + testMethodName);
        System.out.println("\t output dir: " + outputDirectory.getAbsolutePath());
        System.out.println("\t seed file(s): " );
        Arrays.stream(seedFiles).map(file -> file.getName()).forEach(s -> System.out.println("\t\t- " + s));

        loadGuidance(testClassName, testMethodName, outputDirectory, seedFiles);


    }

    private static void loadGuidance(String testClassName, String testMethodName, File outputDirectory, File[] seedFiles) {
        try {
            // Load the guidance
            String title = testClassName + "#" + testMethodName;
            Guidance guidance = null;

            System.out.println("");


            switch (guidanceMethod) {
                case 0:
                    guidance = loadZestGuidance(title, outputDirectory, seedFiles);
                    break;
                case 1:
                    guidance = loadGraphGuidance(title, outputDirectory, seedFiles);
                    break;
                default:
                    guidance = loadNoGuidance(seedFiles, 10000);
            }

            System.out.println("Guidance loaded: " + guidance.getClass().toString());

            // Run the Junit test
            Result res = GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);
            if (Boolean.getBoolean("jqf.logCoverage")) {
                if (guidance instanceof ZestGuidance) {
                    System.out.println(String.format("Covered %d edges.",
                            ((ZestGuidance) guidance).getTotalCoverage().getNonZeroCount()));
                }
            }
            if (Boolean.getBoolean("jqf.ei.EXIT_ON_CRASH") && !res.wasSuccessful()) {
                System.exit(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static NoGuidance loadNoGuidance(File[] seedFiles, int i) {
        return new NoGuidance(seedFiles[0].getPath(), i, null);
    }

    private static Guidance loadGraphGuidance(String title, File outputDirectory, File[] seedFiles) throws IOException {
        if (seedFiles == null) {
            return new ZestGuidance(title, null, outputDirectory);
        } else if (seedFiles.length == 1 && seedFiles[0].isDirectory()) {
            return new ZestGuidance(title, null, outputDirectory, seedFiles[0]);
        } else {
            return new ZestGuidance(title, null, outputDirectory, seedFiles);
        }
    }

    private static ZestGuidance loadZestGuidance(String title, File outputDirectory, File[] seedFiles) throws IOException {
        if (seedFiles == null) {
            return new ZestGuidance(title, null, outputDirectory);
        } else if (seedFiles.length == 1 && seedFiles[0].isDirectory()) {
            return new ZestGuidance(title, null, outputDirectory, seedFiles[0]);
        } else {
            return new ZestGuidance(title, null, outputDirectory, seedFiles);
        }
    }
}
