package nl.ipo.cds.etl.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class BlockingExecutor implements Executor {
	
	private static final Log logger = LogFactory.getLog(BlockingExecutor.class);
	
	private BlockingQueue<Runnable> queue = new SynchronousQueue<Runnable>();  
	
	public BlockingExecutor(int numberOfThreads) {
		for(int i = 0; i < numberOfThreads; i++) {
			Thread t = new Thread(null, null, "executor-thread-" + i) {
				
				@Override
				public void run() {
					try {
						for(;;) {
							Runnable r = queue.take();
							try {		
								logger.debug("starting runnable");
								r.run();
							} catch(Throwable t) {
								logger.error(t);
								System.exit(1);
							}
						}
					} catch(InterruptedException e) {
						logger.error(e);
						System.exit(1);
					}
				}
			};
			
			t.setPriority(Thread.MIN_PRIORITY);			
			t.start();
		}
	}

	@Override
	public void execute(Runnable command) {
		try {
			queue.put(command);
		} catch(InterruptedException e) {
			logger.debug(e);
		}
	}
}
