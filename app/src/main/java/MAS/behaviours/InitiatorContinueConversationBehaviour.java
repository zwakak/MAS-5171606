package MAS.behaviours;


import MAS.agents.BasicAgent;
import MAS.agents.InitiatorAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InitiatorContinueConversationBehaviour extends CyclicBehaviour {
    private static InitiatorContinueConversationBehaviour instance;

    public static InitiatorContinueConversationBehaviour getInstance() {
        return instance;
    }
    @Override
    public void action() {
        instance = this;
        MessageTemplate template = MessageTemplate.MatchConversationId(BasicAgent.conversationId);
        ACLMessage message = myAgent.receive(template);

        if(message!=null){
            ACLMessage reply = message.createReply();
            reply.setConversationId(BasicAgent.conversationId);
            if(message.getPerformative() == ACLMessage.PROPOSE){
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setContent("Proposals after the Deadline is automatically rejected");
                myAgent.send(reply);
            }else if(message.getPerformative() == ACLMessage.INFORM){
                ((BasicAgent)myAgent).print("Received an information from " + message.getSender().getName() +
                        " Stating that " + message.getContent(),((InitiatorAgent)myAgent).getContext());
                }else if(message.getPerformative() == ACLMessage.FAILURE){
                ((BasicAgent)myAgent).print("Assigned agent has failed to do the job reactivating the CFP behaviour",((InitiatorAgent)myAgent).getContext());
                ((InitiatorAgent)myAgent).restartBehaviours();
            }

        }else block();
    }


}
