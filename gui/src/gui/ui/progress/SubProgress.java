package gui.ui.progress;

public class SubProgress implements Progress {
	public final double from;
	public final String message;
	public final Progress progress;
	public final double to;
	
	public SubProgress(double from, String message, Progress progress,
			double to) {
		this.from=from;
		this.message=message;
		this.progress=progress;
		this.to=to;
	}
    
    @Override
    public boolean cancellable() {
        return progress.cancellable();
    }

    @Override
    public void cancellable(boolean cancellable) {
        progress.cancellable(cancellable);
    }
	
	@Override
	public boolean cancelled() {
		return progress.cancelled();
	}
	
	@Override
	public void progress(String message, double progress) throws Throwable {
		double progress2=(1.0-progress)*from+progress*to;
		if (from>progress2) {
			progress2=from;
		}
		if (to<=progress2) {
			progress2=Math.nextDown(to);
		}
		String message2;
		if (null==this.message) {
			message2=message;
		}
		else if (null==message) {
			message2=this.message;
		}
		else {
			message2=this.message+": "+message;
		}
		this.progress.progress(message2, progress2);
	}
}
