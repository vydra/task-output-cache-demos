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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class MissingFiles {

    private File missingFile;
    private File existingFile;

    @Setup
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setup() throws IOException {
        missingFile = new File("missing.txt");
        missingFile.delete();
        existingFile = new File("missing.txt");
        existingFile.createNewFile();
    }

    @TearDown
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void tearDown() {
        existingFile.delete();
    }

    @Benchmark
    public void existingFileExists(Blackhole blackhole) {
        blackhole.consume(existingFile.exists());
    }

    @Benchmark
    public void missingFileExists(Blackhole blackhole) {
        blackhole.consume(missingFile.exists());
    }

    @Benchmark
    public void existingFileLength(Blackhole blackhole) {
        blackhole.consume(existingFile.length());
    }

    @Benchmark
    public void missingFileLength(Blackhole blackhole) {
        blackhole.consume(missingFile.length());
    }

    @Benchmark
    public void existingFileLastModified(Blackhole blackhole) {
        blackhole.consume(existingFile.lastModified());
    }

    @Benchmark
    public void missingFileLastModified(Blackhole blackhole) {
        blackhole.consume(missingFile.lastModified());
    }
}
