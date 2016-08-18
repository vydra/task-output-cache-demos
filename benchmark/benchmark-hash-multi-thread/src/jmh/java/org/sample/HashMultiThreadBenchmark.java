/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class HashMultiThreadBenchmark {

    private static final int FILE_COUNT = 1024;
    private static final HashFunction MD5 = Hashing.md5();
    private final List<File> dataFiles = Lists.newArrayListWithCapacity(FILE_COUNT);

    @Setup
    public void setupData() throws IOException {
        byte[] data = new byte[1024 * 1024];
        Random random = new Random(123412341234L);
        for (int i = 0; i < FILE_COUNT; i++) {
            random.nextBytes(data);
            File dataFile = new File("data" + i + ".bin");
            Files.write(data, dataFile);
            dataFiles.add(dataFile);
            if (i % 128 == 0) {
                System.out.print(".");
            }
        }
        System.out.printf("Created %d test files ", FILE_COUNT);
    }

    private Queue<File> testFiles;

    @Setup(Level.Iteration)
    public void flushCaches() throws IOException {
        Runtime.getRuntime().exec("sync");
        Runtime.getRuntime().exec("sudo purge");
        testFiles = Queues.newConcurrentLinkedQueue(dataFiles);
    }

    @TearDown
    public void tearDown() throws IOException {
        for (File dataFile : dataFiles) {
            //noinspection ResultOfMethodCallIgnored
            dataFile.delete();
        }
    }

    private void hashSingleFile(Blackhole blackhole) throws IOException {
        File file = testFiles.poll();
        if (file == null) {
            throw new RuntimeException("No more files");
        }
        HashCode hash = Files.hash(file, MD5);
        blackhole.consume(hash);
    }

    @Benchmark
    @Threads(1)
    public void runWith1Thread(Blackhole blackhole) throws IOException {
        hashSingleFile(blackhole);
    }

    @Benchmark
    @Threads(2)
    public void runWith2Threads(Blackhole blackhole) throws IOException {
        hashSingleFile(blackhole);
    }

    @Benchmark
    @Threads(4)
    public void runWith4Threads(Blackhole blackhole) throws IOException {
        hashSingleFile(blackhole);
    }

    @Benchmark
    @Threads(8)
    public void runWith8Threads(Blackhole blackhole) throws IOException {
        hashSingleFile(blackhole);
    }
}
