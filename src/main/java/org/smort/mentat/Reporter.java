package org.smort.mentat;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

/*
 * Reporter is used for processing the Integers from the {@RequestHandler} and
 * to output a summary of unique and duplicate counts on a 10 second cadeance. 
 * 
 * This is basically a consumer. 
 */
final class Reporter implements Runnable {
    private final BlockingQueue<Integer> workQueue;
    private final BitSet cache = new BitSet(1_000_000_000);
    private final Timer timer = new Timer();
    private final Object lock = new Object();
    int newUniqueCount = 0;
    int newDuplicateCount = 0;

    /*
     * Creates a Reporter that consumes from the given BlockingQueue. 
     */
    Reporter(BlockingQueue<Integer> workQueue) {
        this.workQueue = workQueue;
        timer.schedule(new Summary(), 0, 10_000);
    }

    @Override
    public void run() {
        // Overwrites file on startup 
        // Default buffer size of 8192 before writing to disc (minimize flushes)
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("numbers.log")))) {
            while (true) {
                int input = workQueue.take();
                // Note: if cache was shared across threads these operations would
                // need to be within synchronized block. BitSets are not thread-safe.
                boolean duplicate = cache.get(input);
                synchronized (lock) {
                    if (duplicate) {
                        newDuplicateCount++;
                        continue;
                    }
                    newUniqueCount++;
                }
                cache.set(input);
                writer.write(input + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Reporter interrupted. Exiting.");
            // Stop the Summary task
            timer.cancel();
        }
    }

    /*
     * Summary is a task that  runs on a set duration which
     * outputs a report to stdout. 
     */
    private final class Summary extends TimerTask {
        int uniqueTotalCount = 0;

        @Override
        public void run() {
            // Didn't use atomics because values could change between accessing 
            // newUniqueCount and newDuplicateCount so rather synchronize 
            synchronized (lock) {
                uniqueTotalCount += newUniqueCount;
                System.out.printf("Received %d unique numbers, %d duplicates. Unique total: %d\n",
                        newUniqueCount, newDuplicateCount, uniqueTotalCount);
                newUniqueCount = 0;
                newDuplicateCount = 0;
            }

        }
    }
}
