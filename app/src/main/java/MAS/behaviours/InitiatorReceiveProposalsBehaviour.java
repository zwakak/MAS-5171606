package MAS.behaviours;

import MAS.agents.BasicAgent;
import MAS.agents.InitiatorAgent;
import MAS.helpers.Proposal;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Future;

public class InitiatorReceiveProposalsBehaviour extends WakerBehaviour {
    private static InitiatorReceiveProposalsBehaviour instance;

    public static InitiatorReceiveProposalsBehaviour getInstance() {
        return instance;
    }
    ArrayList<Proposal> receivedProposals = new ArrayList<>();

    public InitiatorReceiveProposalsBehaviour(Agent a, Date wakeupDate) {
        super(a, wakeupDate);
        instance = this;
    }

    @Override
    protected void onWake() {
        super.onWake();

        for(int i =0; i<InitiatorAgent.nResponders; i++) {
            ACLMessage reply = myAgent.receive(MessageTemplate.MatchConversationId((InitiatorAgent.conversationId)));

            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.PROPOSE) {
                    Proposal proposal;
                    try {
                        proposal = (Proposal) reply.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }

                    if (proposal != null) {
                        receivedProposals.add(proposal);
                    }
                }else if (reply.getPerformative() == ACLMessage.REFUSE){
                    ((BasicAgent) myAgent).print("Agent " + reply.getSender().getName() + " refused to propose and it's message is " + "* " + reply.getContent() + " *", ((InitiatorAgent)myAgent).getContext());
                }
            }
        }

    }

    @Override
    public int onEnd() {
        Proposal bestProposal = null;

        if(receivedProposals.size() ==0){
            ((BasicAgent) myAgent).print("No proposals received.",((InitiatorAgent)myAgent).getContext());
        }else {
            if(receivedProposals.size() < InitiatorAgent.nResponders){
                ((BasicAgent) myAgent).print("received only " +receivedProposals.size() + " proposals out of " + InitiatorAgent.nResponders + " expected", ((InitiatorAgent)myAgent).getContext());
            }

            bestProposal = evaluateProposals();
            if (bestProposal != null) {
                ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                accept.setConversationId(BasicAgent.conversationId);
                accept.addReceiver(bestProposal.getAgentIdentifier());
                accept.setContent("Task assigned to you");
                myAgent.send(accept);
                ((BasicAgent) myAgent).print("Task assigned to " + bestProposal.getAgentIdentifier().getLocalName(), ((InitiatorAgent)myAgent).getContext());
                receivedProposals.remove(bestProposal);
                receivedProposals.forEach(proposal -> {
                    ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    reject.setConversationId(BasicAgent.conversationId);
                    reject.addReceiver(proposal.getAgentIdentifier());
                    reject.setContent("Your Proposal has been refused");
                    myAgent.send(reject);
                });
            } else {

                ((BasicAgent) myAgent).print("No valid proposals received.", ((InitiatorAgent)myAgent).getContext());
            }
        }
        myAgent.addBehaviour(new InitiatorContinueConversationBehaviour());

        return super.onEnd();
    }

    protected Proposal evaluateProposals() {
        Proposal bestProposal=null;
        for (Proposal proposal : receivedProposals) {
            if(bestProposal == null || proposal.isBetterThan(bestProposal)){
                bestProposal = proposal;
            }
        }

        if(bestProposal!=null) {
            ((BasicAgent) myAgent).print("Best proposal is with cost " + bestProposal.getCost()
                    + " received from : " + bestProposal.getAgentIdentifier().getName(), ((InitiatorAgent)myAgent).getContext());
        }
        return bestProposal;
    }
}
