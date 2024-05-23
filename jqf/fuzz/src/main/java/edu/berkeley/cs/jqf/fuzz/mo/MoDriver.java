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

import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import tudgraphs.GraphMutations;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rohan Padhye
 */
public class MoDriver {

    private static long maxTrials = Long.MAX_VALUE;
    private static String testClassName;
    private static String testMethodName;
    private static int mutation_framework = 1;
    private static boolean new_input_enables = false;
    private static Duration maxDuration = Duration.of(5, ChronoUnit.MINUTES);

    public static void main(String[] args) {

        parse_arguments(args);
        System.out.println();

        try {
            // Load the guidance

//            NoGuidance guidance = new NoGuidance(maxTrials, System.err);
            GraphGuidance guidance = new GraphGuidance(maxTrials, System.out, testClassName, testMethodName, maxDuration, mutation_framework, new_input_enables);

            // Run the Junit test
            GuidedFuzzing.run(testClassName, testMethodName, guidance, System.out);

            if (Boolean.getBoolean("jqf.logCoverage")) {
                System.out.println(String.format("Covered %d edges.",
                        guidance.getCoverage().getNonZeroCount()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }

    }

    private static void parse_arguments(String[] args) {
        System.out.println("Starting fuzz driver with parameters:");
        if (args.length < 2) {
            System.err.println("Usage: java " + MoDriver.class + " TEST_CLASS TEST_METHOD [MAX_TRIALS]");
            System.exit(1);
        }

        testClassName = args[0];
        System.out.printf("\t [%d]: test class: %s\n", 0, testClassName);
        testMethodName = args[1];
        System.out.printf("\t [1]: test method: %s\n", testMethodName);

        int framework_index = 2;
        if (args.length > framework_index) {
            set_mutation_framework(args[framework_index], framework_index);
        }

        int new_inputs_index = 3;
        if (args.length > new_inputs_index) {
            set_new_inputs_enabled(args[new_inputs_index], new_inputs_index);
        }

        int duration_index = 4;
        if (args.length > duration_index) {
            set_duration(args[duration_index], duration_index);
        }

        int mutation_status_index = 5;
        if (args.length > mutation_status_index) {
            set_mutations_status(args[mutation_status_index], mutation_status_index);
        }
    }

    private static void set_new_inputs_enabled(String arg, int index) {
        if (arg.equals("0")) {
            new_input_enables = false;
        } else if (arg.equals("1")) {
            new_input_enables = true;
        } else {
            System.out.printf("\t [%d]: Could not set new inputs enabled: %s, please use 0 (disabled) or 1 (enabled) \n", index, arg);
            System.exit(1);
        }
        System.out.printf("\t [%d]: new inputs enabled: %s\n", index, new_input_enables);
    }

    private static void set_duration(String arg, int index) {
        if (arg.equals("0")) {
            System.out.printf("\t [%d]: default max duration: %s, default max trials: %s\n", index, maxDuration, maxTrials);
            return;
        }
        if (arg.startsWith("t")) {
            maxTrials = Long.parseLong(arg.split("t")[1]);
            System.out.printf("\t [%d]: default max duration: %s, max trials: %s\n", index, maxDuration, maxTrials);
        } else if (arg.startsWith("d")) {
            maxDuration = Duration.of(Integer.parseInt(arg.split("d")[1]), ChronoUnit.MINUTES);
            System.out.printf("\t [%d]: max duration: %s, default max trials: %s\n", index, maxDuration, maxTrials);

        } else {
            System.out.printf("\t [%d]: Could not recognise the duration: %s. Start the duretion with t for maxTrials and d for maxDuration \n", index, arg);
            System.exit(1);
        }
    }

    private static void set_mutation_framework(String arg, int index) {
        if (arg.equals("None") || arg.equals("-1")) {
            mutation_framework = -1;
        } else if (arg.equals("Random") || arg.equals("0")) {
            mutation_framework = 0;
        } else if (arg.equals("PGFuzz") || arg.equals("1")) {
            mutation_framework = 1;
        } else {
            System.out.printf("\t Unrecognized mutation_framework: %s\n", arg);
            System.exit(1);
        }
        System.out.printf("\t [%d]: Mutation_framework: %s (%s)\n", index, mutation_framework, arg);
    }

    private static void set_mutations_status(String arg, int index) {
        if (arg.equals("-1")) {
            System.out.printf("\t [%d] mutation method: all\n", index);
            return;
        }

        int mutationSelected = Integer.parseInt(arg);
        ArrayList<GraphMutations.MutationMethod> mutationMethods = new ArrayList<>(List.of(GraphMutations.MutationMethod.values()));

        GraphMutations.MutationMethod[] mm1 = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.AddEdge, GraphMutations.MutationMethod.AddNode, GraphMutations.MutationMethod.AddProperty
        };
        GraphMutations.MutationMethod[] mm2 = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.RemoveEdge, GraphMutations.MutationMethod.RemoveNode, GraphMutations.MutationMethod.RemoveProperty
        };
        GraphMutations.MutationMethod[] mm3 = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.ChangeLabelEdge, GraphMutations.MutationMethod.ChangeLabelNode, GraphMutations.MutationMethod.ChangePropertyKey
        };
        GraphMutations.MutationMethod[] mm4 = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.ChangePropertyValue
        };
        GraphMutations.MutationMethod[] mm5 = new GraphMutations.MutationMethod[]{
                GraphMutations.MutationMethod.BreakCardinality, GraphMutations.MutationMethod.BreakUnique,
                GraphMutations.MutationMethod.BreakNull, GraphMutations.MutationMethod.ChangePropertyType};

        ArrayList< GraphMutations.MutationMethod[]> mm_groups = new ArrayList<>();
        mm_groups.add(mm1);
        mm_groups.add(mm2);
        mm_groups.add(mm3);
        mm_groups.add(mm4);
        mm_groups.add(mm5);


        if (mutationSelected > 4) {
            System.err.println("\t [" + index + "]: Selected mutation method (" + mutationSelected + ") higher than all mutation methods groups (5), stopping execution");
//            for (int i = 0; i < mutationMethods.size(); i++) {
//                System.out.printf("\t\t [%d] %s\n", i, mutationMethods.get(i).name());
//            }
            for (int i = 0; i < mm_groups.size(); i++) {
                System.out.printf("\t[%d]: %s\n", i, Arrays.toString(mm_groups.get(i)));
            }

            System.exit(1);
        }
        ArrayList<GraphMutations.MutationMethod> mm_group = new ArrayList<>(List.of(mm_groups.get(mutationSelected)));
        System.out.printf("\t [%d]: mutation method: %s\n", index, mutationSelected);
        for (GraphMutations.MutationMethod mutationMethod : GraphMutations.MutationMethod.values()) {
            GraphMutations.changeMutationStatus(mutationMethod, mm_group.contains(mutationMethod));
        }


//        GraphMutations.MutationMethod active_mm = mutationMethods.get(mutationSelected);

//        if (!GraphMutations.getActiveMutationMethodList().contains(active_mm)) {
//            System.out.printf("\tSelected mutation not enabled, stopping execution: " + active_mm);
//            System.exit(1);
//        }
//
//        System.out.printf("\t [%d]: mutation method: %s\n", index, active_mm.name());
//        for (GraphMutations.MutationMethod mutationMethod : GraphMutations.MutationMethod.values()) {
//            GraphMutations.changeMutationStatus(mutationMethod, mutationMethod == active_mm);
//        }

    }
    }
