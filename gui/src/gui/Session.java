package gui;

import gui.io.Database;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Session implements AutoCloseable {
	public interface KeepAlive extends AutoCloseable {
		@Override
		public void close();
	}
	
	public final Database database;
	public final ScheduledExecutorService executor;
	private int keepAlives;
	private final Object lock=new Object();
	
	public Session(Database database) {
		this.database=database;
		this.executor=Executors.newScheduledThreadPool(
				Runtime.getRuntime().availableProcessors());
	}
	
	@Override
	public void close() {
		executor.shutdown();
	}
	
	public KeepAlive keepAlive() {
		KeepAlive result=new KeepAlive() {
			private boolean closed;
			
			@Override
			public void close() {
				synchronized (lock) {
					if (closed) {
						return;
					}
					closed=true;
					--keepAlives;
					if (0!=keepAlives) {
						return;
					}
				}
				executor.shutdown();
			}
		};
		synchronized (lock) {
			++keepAlives;
		}
		return result;
	}
}
