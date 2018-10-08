package gui.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RenderedGraph2D {
	public final List<RenderedSample2D> functions;
	public final Graph2D graph;
	public final List<Ruler> rulers;
	public final List<RenderedSample2D> samples;
	
	public RenderedGraph2D(List<RenderedSample2D> functions,
			Graph2D graph, List<Ruler> rulers,
			List<RenderedSample2D> samples) {
		this.functions
				=Collections.unmodifiableList(new ArrayList<>(functions));
		this.graph=graph;
		this.rulers=Collections.unmodifiableList(new ArrayList<>(rulers));
		this.samples=Collections.unmodifiableList(new ArrayList<>(samples));
	}
}
