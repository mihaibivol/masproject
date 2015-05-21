package agents;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class Task2Carrier extends Task1Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		System.out.println("Agent started");
		// Subscribe to the world
		ACLMessage m = new ACLMessage(ACLMessage.SUBSCRIBE);
		m.setContent("Task2Carrier");
		m.addReceiver(new AID("world", AID.ISLOCALNAME));
		send(m);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/** TODO add a behavior to speak with other agents so they can get
		 * the gold coordinates and decide together a gathering plan
		 * 
		 * e.g. Each one will have a set of targets so they don't run for the same things
		 */
	}
	
}
