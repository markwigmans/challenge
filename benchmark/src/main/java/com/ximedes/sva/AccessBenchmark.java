package com.ximedes.sva;

import org.apache.commons.collections.FastArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by mawi on 19/08/2016.
 */
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AccessBenchmark {

    private HashMap<Integer, Integer> hmap;
    private TreeMap<Integer, Integer> tmap;
    private int xs[];
    private static int sxs[];
    private FastArrayList fal;

    @Param({"10000", "100000"})
    int size;

    @Setup
    public void setup() {
        xs = new int[size];
        sxs = new int[size];
        fal = new FastArrayList(size);
        fal.setFast(true);

        hmap = new HashMap<>(size);
        tmap = new TreeMap<>();

        IntStream.range(0, size).forEach(i -> {
            hmap.put(i, i);
            tmap.put(i, i);
            xs[i] = i;
            fal.add(i, i);
        });
    }

    @Benchmark
    public void testHashMap(Blackhole bh) throws InterruptedException {
        IntStream.range(0, size).forEach(x -> bh.consume(hmap.get(x)));
    }

    @Benchmark
    public void testTreeMap(Blackhole bh) throws InterruptedException {
        IntStream.range(0, size).forEach(x -> bh.consume(tmap.get(x)));
    }

    @Benchmark
    public void testArray(Blackhole bh) throws InterruptedException {
        IntStream.range(0, size).forEach(x -> bh.consume(xs[x]));
    }

    @Benchmark
    public void testStaticArray(Blackhole bh) throws InterruptedException {
        IntStream.range(0, size).forEach(x -> bh.consume(sxs[x]));
    }

    @Benchmark
    public void testFastArray(Blackhole bh) throws InterruptedException {
        IntStream.range(0, size).forEach(x -> bh.consume(fal.get(x)));
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(AccessBenchmark.class.getSimpleName())
                //.addProfiler(StackProfiler.class)
                .build();

        new Runner(opt).run();
    }
}
