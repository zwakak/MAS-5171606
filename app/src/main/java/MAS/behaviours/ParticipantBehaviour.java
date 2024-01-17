package MAS.behaviours;

import MAS.agents.BasicAgent;
import MAS.agents.ParticipantAgent;
import MAS.helpers.Proposal;
import MAS.helpers.Request;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;

public class ParticipantBehaviour extends CyclicBehaviour {
    int numEmployees;
    double costPerEmployee;

    public ParticipantBehaviour(int numEmployees, double costPerEmployee) {
        this.numEmployees = numEmployees;
        this.costPerEmployee = costPerEmployee;
    }

    @Override
    public void action() {
        MessageTemplate template =  MessageTemplate.MatchConversationId(ParticipantAgent.conversationId);
        ACLMessage message = myAgent.receive(template);
        if(message!=null){
            ACLMessage reply;
            reply= message.createReply();
            reply.setConversationId(ParticipantAgent.conversationId);
            try {
                if(message.getPerformative() == ACLMessage.CFP) {
                    Request request = (Request) message.getContentObject();

                    if (request.getRequiredNumOfEmployees() <= numEmployees) {
                        double cost = calculateCost(request.getDuration(), request.getRequiredNumOfEmployees());
                        reply.setPerformative(ACLMessage.PROPOSE);
                        reply.setContentObject(new Proposal(
                                myAgent.getAID(),
                                cost,
                                1
                        ));
                    } else {
                        ((BasicAgent)myAgent).print("I do not have enough resources, refusing to propose ...",((ParticipantAgent)myAgent).getContext());

                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Do not have enough resources");
                    }
                }else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                    ((BasicAgent)myAgent).print("My Proposal has been accepted\nProceeding with the job", ((ParticipantAgent)myAgent).getContext());
                    Thread.sleep(1000);

                    if (Math.random() < .2) {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("failed to do the job");
                    }else {
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("job is done");
                    }

                }else if(message.getPerformative() == ACLMessage.REJECT_PROPOSAL){
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("confirm proposal rejection");

                }else if (message.getPerformative() == ACLMessage.CANCEL){
                    reply.setPerformative(ACLMessage.INFORM);

                    reply.setContent("Request has been canceled by " + message.getSender().getName());
                }
            } catch (UnreadableException | InterruptedException| IOException e) {
                e.printStackTrace();
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
                reply.setContent("Something is wrong with the message");
            }
            myAgent.send(reply);
        }else block();
    }

    private double calculateCost(int numOfWorkingDays, int requiredNumOfEmployees){
        return  requiredNumOfEmployees * costPerEmployee * numOfWorkingDays;
    }
}
