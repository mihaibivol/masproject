package main;

import jade.Boot;

public class Main {
	public static void main(String [] args) {
		String agentCmdLine = "world:agents.World()";
		int noAgentsT1 = 0;
		for (int i = 0; i < noAgentsT1; i++) {
			agentCmdLine += ";agentT1" + i + ":agents.Task1Agent()";
		}
		/** TODO change this to test task 2 **/
		int noAgentsT2S = 40;
		for (int i = 0; i < noAgentsT2S; i++) {
			agentCmdLine += ";agentT2S" + i + ":agents.Task2Search()";
		}
		int noAgentsT2C = 15;
		for (int i = 0; i < noAgentsT2C; i++) {
			agentCmdLine += ";agentT2C" + i + ":agents.Task2Carrier()";
		}
		Boot.main(new String[] {"-agents",
				agentCmdLine});
	}
}
