package gui.ui.progress;

import java.io.PrintStream;

public class PrintStreamProgress implements Progress {
	public final boolean newLine;
	public final PrintStream stream;
	
	public PrintStreamProgress(boolean newLine, PrintStream stream) {
		this.newLine=newLine;
		this.stream=stream;
	}
    
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
		String line=(null==message)
				?String.format("%1$5.1f%%", Math.floor(1000.0*progress)/10.0)
				:String.format("%1$5.1f%%: %2$s",
						Math.floor(1000.0*progress)/10.0, message);
		if (newLine) {
			stream.println(line);
		}
		else {
			stream.print("\r");
			stream.print(line);
			stream.print(" ");
			if (1.0<=progress) {
				stream.println();
			}
		}
	}
}
