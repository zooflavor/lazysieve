package gui.ui.progress;

import gui.ui.MessageException;

public class CancelledException extends MessageException {
    private static final long serialVersionUID=0l;
    
    public CancelledException() {
        super("megszakítva");
    }
}
