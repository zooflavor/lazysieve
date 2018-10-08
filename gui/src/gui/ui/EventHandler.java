package gui.ui;

@FunctionalInterface
public interface EventHandler<E> {
	void handle(E event) throws Throwable;
}
