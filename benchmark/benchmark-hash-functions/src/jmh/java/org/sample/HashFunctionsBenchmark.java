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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.Benchmark;

public class HashFunctionsBenchmark {

    private static final byte[] data = RandomStringUtils.random(32768).getBytes();
    private static final HashFunction MD5 = Hashing.md5();
    private static final HashFunction SHA1 = Hashing.sha1();
    private static final HashFunction CRC32 = Hashing.adler32();
    private static final HashFunction ADLER32 = Hashing.adler32();
    private static final HashFunction SIP24 = Hashing.sipHash24();
    private static final HashFunction MURMUR3 = Hashing.murmur3_128();

    @Benchmark
    public void md5() {
        MD5.hashBytes(data);
    }

    @Benchmark
    public void adler32() {
        ADLER32.hashBytes(data);
    }

    @Benchmark
    public void crc32() {
        CRC32.hashBytes(data);
    }

    @Benchmark
    public void sha1() {
        SHA1.hashBytes(data);
    }

    @Benchmark
    public void sip24() {
        SIP24.hashBytes(data);
    }

    @Benchmark
    public void murmur3() {
        MURMUR3.hashBytes(data);
    }
}
