package ACO;

public class Ant {

	protected int trailSize;
	protected int trail[];
	protected boolean visited[];

	public Ant(int tourSize) {
	    this.trailSize = tourSize;
	    this.trail = new int[tourSize + 1]; // Plus one to allow returning to 'SP'
	    this.visited = new boolean[tourSize];
	}


	protected void visitCity(int currentIndex, int city) {
	    if (currentIndex + 1 < trailSize) {
	        trail[currentIndex + 1] = city;
	        visited[city] = true;
	    }
	}



	protected boolean visited(int i) {
		return visited[i];
	}

	protected double trailLength(double graph[][]) {
		double length = graph[trail[trailSize - 1]][trail[0]];
		for (int i = 0; i < trailSize - 1; i++) {
			length += graph[trail[i]][trail[i + 1]];
		}
		return length;
	}

	protected void clear() {
		for (int i = 0; i < trailSize; i++)
			visited[i] = false;
	}

}
