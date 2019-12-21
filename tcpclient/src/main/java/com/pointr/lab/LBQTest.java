package com.pointr.lab;

/**
 * @author Created by sboesch on July 13, 2010
 */

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LBQTest {
	static CyclicBarrier barrier;
	static CountDownLatch latch;
	static BlockingQueue<String> queue = new LinkedBlockingQueue<String>((int)15e6);
	static AtomicLong counter = new AtomicLong();
	static AtomicLong totalData = new AtomicLong();

	public static void main(String[] args) throws Exception {
		if (args.length!=4) {
			System.err.println("Usage: LBQTest <nProducers> <nConsumers> <nLoops> <payloadSize>");
			System.exit(-1);
		}
		System.out.println("Starting test at " + new Date() + " with queue capacity " + queue.remainingCapacity());
		long start = System.currentTimeMillis();
		int nProducers = Integer.parseInt(args[0]);
		int nConsumers = Integer.parseInt(args[1]);
		int nLoops = Integer.parseInt(args[2]);
		int dataSize = Integer.parseInt(args[3]);
		int nThreads = nProducers + nConsumers;
		int nEntries = nProducers * nLoops;
		int avgDataPerConsumer = (int)(nLoops * nProducers * dataSize / nConsumers);
		latch = new CountDownLatch(nThreads);
		barrier = new CyclicBarrier(nThreads);
		for (int i = 0; i < nConsumers; i++) {
			Consumer c = new Consumer(i, nEntries, avgDataPerConsumer);
		}
		for (int i = 0; i < nProducers; i++) {
			Producer p = new Producer(i, nLoops, dataSize);
		}
		latch.await();
		double avg = 1000.0 * nEntries / (System.currentTimeMillis()-start);
		trace("*** TEST COMPLETED: NumProducers="+nProducers + " NumConsumers="+nConsumers +
			" DataSize="+dataSize+" TotalEntries="+counter.get() + " TotalBytes="+totalData.get()+
			" in " + ((1.0*System.currentTimeMillis()-start)/1000.0) +"sec for avg="+ avg + " takes/sec ***");
	}

	static class Producer extends Thread {
		private int threadNum;
		int nLoops;
		int dataSize;

		Producer(int threadNum, int nLoops, int dataSize) {
			super("Producer-" + threadNum);
			this.threadNum = threadNum;
			this.nLoops = nLoops;
			this.dataSize = dataSize;
			this.start();
		}

		public void run() {
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < nLoops; i++) {
				String val = null;
				String prefix = getName() + " loop " + i+": ";
				String payload = prefix + ipsum100.substring(0,dataSize-prefix.length());
				queue.offer(payload);
//				if (i == nLoops - 1) {
//					trace("Last value: " + val);
//				}

				//            Thread.yield();
				//            while (queue.isEmpty()) {
				//                try {
				//                    Thread.sleep(0, 1);
				//                } catch (InterruptedException e) {
				//                    trace("interrupted");
				//                }
				//            }
			}
			if (threadNum % 50 == 0) {
        trace(getName() + " completed. Current queueSize is " + queue.size());
      }
			latch.countDown();
		}


		static String ipsum = "ed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, "
			+ " totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."
			+ " Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui "
			+ " ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci "
			+ " velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima "
			+ " veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel"
		  + " eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo "
		  + " voluptas nulla pariatur?";

		static String ipsum100 = null;

    static {
      StringBuilder sb = new StringBuilder(ipsum);
      for (int i = 0; i < 3; i++) {
        sb.append(ipsum);
      }
      ipsum100 = sb.toString();
    }
	}


	static class Consumer extends Thread {
    private final int avgDataPerConsumer;
    private int threadNum;
		int nEntries;

		Consumer(int threadNum, int nEntries, int avgDataPerConsumer) {
			super("Consumer-" + threadNum);
			this.threadNum = threadNum;
			this.nEntries = nEntries;
			this.avgDataPerConsumer = avgDataPerConsumer;
			this.start();
		}

		public void run() {
			try {
				barrier.await();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String val;
			String lastVal = null;
			int localData = 0;
			boolean printed = false;
			do {
				val = null;
				try {
					val = queue.poll(1, TimeUnit.SECONDS);
					if (val!=null) {
						totalData.addAndGet(val.length());
						localData += val.length();
						lastVal = val;
					}
				} catch (InterruptedException e) {
					trace("InterruptedException on take");
					val = null;
				}
			if (!printed && localData > 1.25 * avgDataPerConsumer /* && threadNum % 10 == 0 */) {
        trace("Consumer " + getName() + " is hungry: used up " + localData + " bytes");
        printed = true;
      }
			} while ((val!=null) && counter.incrementAndGet() < nEntries);
			if (threadNum % 50 == 0) {
        trace("Consumer " + getName() + " completed. Counter is " + counter.get() + " Length(Last value) = " + lastVal.length());
      }
			latch.countDown();
		}

		void trace(String msg) {
			System.out.println(new Date() + " " + getName() + ": " + msg);
		}
	}

	static void trace(String msg) {
		System.out.println(new Date() + " " + "Main: " + msg);
	}

}


