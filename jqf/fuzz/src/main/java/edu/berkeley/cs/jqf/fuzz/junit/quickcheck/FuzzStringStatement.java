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
package edu.berkeley.cs.jqf.fuzz.junit.quickcheck;

import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.internal.ParameterTypeContext;
import com.pholser.junit.quickcheck.internal.generator.GeneratorRepository;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.guidance.Guidance;
import edu.berkeley.cs.jqf.fuzz.guidance.GuidanceException;
import edu.berkeley.cs.jqf.fuzz.guidance.Result;
import edu.berkeley.cs.jqf.fuzz.guidance.TimeoutException;
import edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing;
import edu.berkeley.cs.jqf.fuzz.junit.TrialRunner;
import edu.berkeley.cs.jqf.fuzz.random.NoGuidance;
import edu.berkeley.cs.jqf.fuzz.repro.ReproGuidance;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.MethodGenericsContext;

import java.io.*;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.berkeley.cs.jqf.fuzz.guidance.Result.*;

/**
 *
 * A JUnit {@link Statement} that will be run using guided fuzz
 * testing.
 *
 * @author Rohan Padhye
 */
public class FuzzStringStatement extends Statement {

    private final FrameworkMethod method;
    private final TestClass testClass;
    private final MethodGenericsContext generics;
    private final GeneratorRepository generatorRepository;
    private final List<Class<?>> expectedExceptions;
    private final List<Throwable> failures = new ArrayList<>();
//    private final Guidance guidance;
    private boolean skipExceptionSwallow;

    public FuzzStringStatement(FrameworkMethod method, TestClass testClass,
                               GeneratorRepository generatorRepository) {
        this.method = method;
        this.testClass = testClass;
        this.generics = GenericsResolver.resolve(testClass.getJavaClass())
                .method(method.getMethod());
        this.generatorRepository = generatorRepository;
        this.expectedExceptions = Arrays.asList(method.getMethod().getExceptionTypes());
//        this.guidance = fuzzGuidance;
        this.skipExceptionSwallow = Boolean.getBoolean("jqf.failOnDeclaredExceptions");

    }


    /**
     * Run the test.
     *
     * @throws Throwable if the test fails
     */
    @Override
    public void evaluate() throws Throwable {
        // Construct generators for each parameter
        List<Generator<?>> generators = Arrays.stream(method.getMethod().getParameters())
                .map(this::createParameterTypeContext)
                .map(this::produceGenerator)
                .collect(Collectors.toList());

        // Get the currently registered fuzz guidance
        Guidance guidance = GuidedFuzzing.getCurrentGuidance();
        int count = 0;
        // If nothing is set, default to random or repro
        if (guidance == null) {
            // Check for @Fuzz(repro=)
            String repro = method.getAnnotation(Fuzz.class).repro();
            if (repro.isEmpty()) {
                guidance = new NoGuidance(GuidedFuzzing.DEFAULT_MAX_TRIALS, System.err);
            } else {
                guidance = new ReproGuidance(new File(repro), null);
            }
        }

        // Keep fuzzing until no more input or I/O error with guidance
        try {

            // Keep fuzzing as long as guidance wants to
            while (guidance.hasInput()) {
                Result result = INVALID;
                Throwable error = null;

                // Initialize guided fuzzing using a file-backed random number source
                String currentFile;
                try {
                    Object[] args;
                    try {

                        // Generate input values
                        //System.out.println("StreamBackedRandom");
                        StringBuilder stringBuilder = new StringBuilder();
                        String line = null;
                        //TMP, THIS NEEDS TO BECOME AN STRING INPUT TO THE FILE
                        InputStream inputStream = guidance.getInput();
                        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println("StreamBackedRandom getValue: "+stringBuilder.toString());
                        currentFile = stringBuilder.toString();
                        Object[] args2 = {currentFile};
                        args = args2;

                    } catch (IllegalStateException e) {
                        if (e.getCause() instanceof EOFException) {
                            // This happens when we reach EOF before reading all the random values.
                            // Treat this as an assumption failure, so that the guidance considers the
                            // generated input as INVALID
                            throw new AssumptionViolatedException("StreamBackedRandom does not have enough data", e.getCause());
                        } else {
                            throw e;
                        }
                    } catch (AssumptionViolatedException e) {
                        // Propagate assumption violations out
                        throw e;
                    } catch (GuidanceException e) {
                        // Throw the guidance exception outside to stop fuzzing
                        System.out.println("Error throw 2");

                        throw e;
                    } catch (Throwable e) {
                        // Throw the guidance exception outside to stop fuzzing
                        System.out.println("Error throw 3");

                        throw new GuidanceException(e);
                    } finally {
                        // System.out.println(randomFile.getTotalBytesRead() + " random bytes read");
                    }

                    // Attempt to run the trial and submit spark job
                    new TrialRunner(testClass.getJavaClass(), method, args).run();

                    // If we reached here, then the trial must be a success
                    result = SUCCESS;
                } catch (GuidanceException e) {
                    // Throw the guidance exception outside to stop fuzzing
                    System.out.println("Error throw 4");
                    throw e;
                }
                catch (AssumptionViolatedException e) {
                    result = INVALID;
                    error = e;
                } catch (TimeoutException e) {
                    result = TIMEOUT;
                    error = e;
                } catch (Throwable e) {

                    // Check if this exception was expected
                    if (isExceptionExpected(e.getClass())) {
                        result = SUCCESS; // Swallow the error
                    } else {
                        result = FAILURE;
                        error = e;
                        failures.add(e);
                    }
                }

                // Inform guidance about the outcome of this trial
                guidance.handleResult(result, error);


            }
        } catch (GuidanceException e) {
            System.err.println("Fuzzing stopped due to guidance exception: " + e.getMessage());
            e.printStackTrace();
        }

        if (failures.size() > 0) {
            if (failures.size() == 1) {
//                throw failures.get(0);
            } else {
                // Not sure if we should report each failing run,
                // as there may be duplicates
//                throw new MultipleFailureException(failures);
            }
        }

    }

    /**
     * Returns whether an exception is expected to be thrown by a trial method
     *
     * @param e the class of an exception that is thrown
     * @return <tt>true</tt> if e is a subclass of any exception specified
     * in the <tt>throws</tt> clause of the trial method.
     */
    private boolean isExceptionExpected(Class<? extends Throwable> e) {
        for (Class<?> expectedException : expectedExceptions) {
            if (expectedException.isAssignableFrom(e)) {
                return true;
            }
        }
        return false;
    }

    private ParameterTypeContext createParameterTypeContext(Parameter parameter) {
        Executable exec = parameter.getDeclaringExecutable();
        String declarerName = exec.getDeclaringClass().getName() + '.' + exec.getName();

        return ParameterTypeContext.forParameter(parameter, generics).annotate(parameter);
//        return new ParameterTypeContext(
//                parameter.getName(),
//                parameter.getAnnotatedType(),
//                declarerName,
//                typeVariables)
//                .allowMixedTypes(true).annotate(parameter);
    }

    private Generator<?> produceGenerator(ParameterTypeContext parameter) {
        Generator<?> generator = generatorRepository.generatorFor(parameter);
        generator.provide(generatorRepository);
        generator.configure(parameter.annotatedType());
        return generator;
    }
}
