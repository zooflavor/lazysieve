package gui.ui.progress;

public class NullProgress implements Progress {
    @Override
    public boolean cancellable() {
        return false;
    }
    
    @Override
    public void cancellable(boolean cancellable) {
    }
    
	@Override
	public boolean cancelled() {
		return false;
	}
	
	@Override
	public void progress(String message, double progress) throws Throwable {
	}
}
