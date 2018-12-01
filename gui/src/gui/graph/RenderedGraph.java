package gui.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RenderedGraph {
	public final boolean completed;
	public final List<RenderedSample> functions;
	public final Graph graph;
	public final List<Ruler> rulers;
	public final List<RenderedSample> samples;
	
	public RenderedGraph(boolean completed, List<RenderedSample> functions,
			Graph graph, List<Ruler> rulers,
			List<RenderedSample> samples) {
		this.completed=completed;
		this.functions
				=Collections.unmodifiableList(new ArrayList<>(functions));
		this.graph=graph;
		this.rulers=Collections.unmodifiableList(new ArrayList<>(rulers));
		this.samples=Collections.unmodifiableList(new ArrayList<>(samples));
	}
	
	public RenderedGraph(Graph graph) {
		this(false, Arrays.asList(), graph, Arrays.asList(), Arrays.asList());
	}
}
