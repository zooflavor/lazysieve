package gui.sieve;

public enum Measure {
	OPERATIONS("műveletek"), NANOSECS("nanoszekundum");
	
	public final String name;
	
	private Measure(String name) {
		this.name=name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
