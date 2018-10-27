package gui.graph;

public interface CheckAndPost {
	void check(Graph graph) throws RendererDeathException;
	void checkAndPost(RenderedGraph graph) throws RendererDeathException;
}
