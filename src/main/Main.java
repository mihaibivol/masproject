package main;

import jade.Boot;

public class Main {
	public static void main(String [] args) {
		String agentCmdLine = "world:agents.World()";
		int noAgents = 20;
		for (int i = 0; i < noAgents; i++) {
			agentCmdLine += ";agent" + i + ":agents.Task1Agent()";
		}
		Boot.main(new String[] {"-agents",
				agentCmdLine});
	}
}
