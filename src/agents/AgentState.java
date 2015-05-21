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
	
	// If the agent is empty or not
	boolean empty;
	
	// If there are any more rocks to collect
	boolean done;
	
	/*
	 * Empty + Done are used because many of the agents were not full and didn't
	 * return to base to complete the process
	 * 
	 * done is needed for non full agents to complete the goal
	 * empty is needed for these agents to free the base after
	 * delivering their rocks(gold)
	 */
	
	// Relative vector with position of the ship
	// It's a transformation of a beacon that works for these agents
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
