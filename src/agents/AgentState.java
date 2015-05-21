package agents;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

public class AgentState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// If the agent is full or not
	boolean full;
	boolean empty;
	boolean done;
	
	// Relative vector with position of the ship
	Point shipVector;
	
	// Relative vectors for obstacles, agents and gold
	ArrayList<Point> obstacleVector = new ArrayList<Point>();
	ArrayList<Point> agentsVector = new ArrayList<Point>();
	ArrayList<Point> goldVector = new ArrayList<Point>();
	
	@Override
	public String toString() {
		return "{\n" + "Full: " + full + "\nShipVector: " + shipVector
				+ "\nAgentsVector: " + agentsVector +"\nObstacleVector: "
				+ obstacleVector + "\nGoldVector: " + goldVector + "};";
	}

}
