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

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class Md5Hash100CharString {

    private static final HashFunction MD5 = Hashing.md5();

    @Param({
            "AwXHEwdNWzqBZrrmwHNpQpAyzlGYyvaIbXImpcbjgPqOcakAJWrjzxviexzMejlansMmrSYPtgHgyavuerxWcuNNBGBZNijsQxjD",
            "짔ⴺ益わ悻띾欉츆袛蕷脆鑁怂躛娨꿜氓ᦉ杸Ȳ辐㮳풫줈㣥牨禗磿昩ꐲꚦ訆衂㔕徝謏ﭬ䆾꺘蓿ꀞϓ爲Йſ寐疪쿪坤ﵹ鹗䖪㣂垵ⶱ䱛眍㳮뻎퉼졪㭚悌㫢ꂲ摼斝ᙪ尥븑护鑋꽗헌ꏀ먲봙λᡑェຮ忧䉍㽘㱴揇꺸㾷該牕퐎㗽쎳뮥岧庆䔓颤Z鮐",
    })
    private String string;

    @Benchmark
    public void unencoded(Blackhole blackhole) {
        HashCode hash = MD5.hashUnencodedChars(string);
        blackhole.consume(hash);
    }

    @Benchmark
    public void defaultEncoding(Blackhole blackhole) {
        HashCode hash = MD5.hashBytes(string.getBytes());
        blackhole.consume(hash);
    }

    @Benchmark
    public void utf16(Blackhole blackhole) {
        HashCode hash = MD5.hashBytes(string.getBytes(Charsets.UTF_16));
        blackhole.consume(hash);
    }

    @Benchmark
    public void utf8(Blackhole blackhole) {
        HashCode hash = MD5.hashBytes(string.getBytes(Charsets.UTF_8));
        blackhole.consume(hash);
    }
}
