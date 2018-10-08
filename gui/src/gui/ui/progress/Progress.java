package gui.ui.progress;

public interface Progress {
	static NullProgress NULL=new NullProgress();
	
    
    boolean cancellable();
    
    void cancellable(boolean cancellable);
    
	boolean cancelled();
	
	default void checkCancelled() throws CancelledException {
		if (cancelled()) {
			throw new CancelledException();
		}
	}
	
	default void finished() throws Throwable {
		progress(null, 1.0);
	}
	
	void progress(String message, double progress) throws Throwable;
	
	default void progress(double progress) throws Throwable {
		progress(null, progress);
	}
	
	default SubProgress subProgress(double from, String message, double to) {
		return new SubProgress(from, message, this, to);
	}
}
