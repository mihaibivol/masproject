package agents;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.tools.sniffer.Message;

public class Task2Carrier extends Task1Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Set<Point> unclaimedGoldLocations = new HashSet<Point>();
	Set<Point> claimedGoldLocations = new HashSet<Point>();
	boolean goldInfoWasUpdated = true;
	
	AgentState state = null;

	@Override
	protected void setup() {
		System.out.println("Agent started");
		// Subscribe to the world
		ACLMessage m = new ACLMessage(ACLMessage.SUBSCRIBE);
		m.setContent("Task2Carrier");
		m.addReceiver(new AID("world", AID.ISLOCALNAME));
		send(m);
		
		DFAgentDescription template = new DFAgentDescription();
		template.setName(getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("CarrierAgents");
		serviceDescription.setName("CarrierAgents");
		template.addServices(serviceDescription);
		try {
			DFService.register(this, template);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}


		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 4208263599671903298L;

			@Override
			public void action() {
				ACLMessage msg = receive(World.ConversationType.GOLD_DISCOVERY.getTemplate());

				if (msg != null) {
					try {
						HashSet<Point> newPoints = (HashSet<Point>) msg.getContentObject();
						newPoints.removeAll(claimedGoldLocations);
						int size = unclaimedGoldLocations.size();
						unclaimedGoldLocations.addAll(newPoints);
						if (size != unclaimedGoldLocations.size()) {
							goldInfoWasUpdated = true;
						}

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
					
			}
		});
		
		addBehaviour(new MoveBehaviour(this));
		addBehaviour(new HandleClaimsBehaviour());
		
		/** TODO add a behavior to speak with other agents so they can get
		 * the gold coordinates and decide together a gathering plan
		 * 
		 * e.g. Each one will have a set of targets so they don't run for the same things
		 */
		
	}
	
	public void updateIntentions() {
		if (!goldInfoWasUpdated && claimedGoldLocations.size() > 0) {
			return;
		}
		
		if (state == null) {
			return;
		}
		
		Point average = getAverage();
			
		Point bestPoint = null;
		for (Point unclaimed : unclaimedGoldLocations) {
			if (bestPoint == null || bestPoint.distance(average) > unclaimed.distance(average)) {
				bestPoint = unclaimed;
			}
		}
		
		if (bestPoint == null) {
			return;
		}
		
		ACLMessage claimMessage = World.ConversationType.CLAIM_GOLD.createNewMessage();
		try {
			claimMessage.setContentObject(new ClaimRequest(bestPoint, average.distance(bestPoint)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		World.broadcast("CarrierAgents", this, claimMessage);
		claimedGoldLocations.add(bestPoint);
		unclaimedGoldLocations.remove(bestPoint);
		
		goldInfoWasUpdated = false;
	}
	
	public Point getAverage() {
		// get average
		Point average = new Point(state.shipVector);
		average.x = -average.x;
		average.y = -average.y;
		int count = 1;
		for (Point p : claimedGoldLocations) {
			average.x += p.x;
			average.y += p.y;
			count++;
		}
		average.x /= count;
		average.y /= count;
		return average;
	}
	
	private class HandleClaimsBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			ACLMessage claim = receive(World.ConversationType.CLAIM_GOLD.getTemplate());

			if (claim != null) {
				try {
					ClaimRequest claimRequest = (ClaimRequest) claim.getContentObject();
					Point average = getAverage();
					if (claimRequest.distance < average.distance(claimRequest.point)) {
						claimedGoldLocations.remove(claimRequest.point);
						unclaimedGoldLocations.remove(claimRequest.point);
					} else if (claimedGoldLocations.contains(claimRequest.point)) {
						ACLMessage claimMessage = claim.createReply();
						try {
							claimMessage.setContentObject(new ClaimRequest(claimRequest.point,
																			average.distance(claimRequest.point)));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						myAgent.send(claimMessage);
					}
					
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
			} else {
				block();
			}
		}	
	}
	
	private class MoveBehaviour extends TickerBehaviour {

		private static final long serialVersionUID = -300417219676801915L;
		
		public MoveBehaviour(Agent agent) {
			// same ticker as other agents
			super(agent, 50);
			

		}

		@Override
		public void onTick() {
			MessageTemplate template = World.ConversationType.AGENT_STATE.getTemplate();
			ACLMessage sm = World.ConversationType.AGENT_STATE.createNewMessage();
			sm.addReceiver(new AID("world", AID.ISLOCALNAME));
			sm.setReplyWith("state" + System.currentTimeMillis());
			
			send(sm);
			ACLMessage reply = blockingReceive(template);
			
			try {
				state = (AgentState)reply.getContentObject();
			} catch (UnreadableException e) { 
				return;
			}
			
			if (state.done) {
				ACLMessage m = new ACLMessage(ACLMessage.CFP);
				String dir;
				if (state.empty) {
					dir = randomChoice(getPossibleDirs(new Point(1000, 1000)));
				} else {
					dir = randomChoice(getPossibleDirs(state.shipVector));
				}
				
				if (state.obstacleVector.contains(destFromDir(dir)))
					dir = randomChoice(getComplement(null));
				if (state.agentsVector.contains(destFromDir(dir)))
					dir = randomChoice(getComplement(null));
				
				m.addReceiver(new AID("world", AID.ISLOCALNAME));
				m.setContent(dir);
				send(m);
				return;
			}
			
			
			updateIntentions();
			
			// get closest point
			Point bestPoint = null;
			Point currentGlobalPosition = new Point(state.shipVector);
			currentGlobalPosition.x = -currentGlobalPosition.x;
			currentGlobalPosition.y = -currentGlobalPosition.y;
			
			ArrayList<Point> pleaseRemoveMe = new ArrayList<Point>();
			for (Point p : claimedGoldLocations) {
				double distance = p.distance(currentGlobalPosition);
				if (distance == 0) {
					pleaseRemoveMe.add(p);
				} else if (bestPoint == null || distance < bestPoint.distance(currentGlobalPosition)) {
					bestPoint = p;
				}
			}
				
			for (Point p : pleaseRemoveMe) {
				claimedGoldLocations.remove(p);
			}
			
			if (bestPoint == null) {
				System.out.println(myAgent.getName() + " nowhere togo: " + claimedGoldLocations);
				return;
			}


			Point dir = new Point(bestPoint);
			dir.x += state.shipVector.x;
			dir.y += state.shipVector.y;
			String command = randomChoice(getPossibleDirs(dir));
			
			if (state.obstacleVector.contains(destFromDir(command)))
				command = randomChoice(getComplement(null));
			if (state.agentsVector.contains(destFromDir(command)))
				command = randomChoice(getComplement(null));
			
			
			//System.out.println(state);
			ACLMessage m = new ACLMessage(ACLMessage.CFP);
			m.addReceiver(new AID("world", AID.ISLOCALNAME));
			m.setContent(command);
			send(m);

		}
		
	}

	
}
