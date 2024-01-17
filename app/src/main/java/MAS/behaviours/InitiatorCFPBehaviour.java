package MAS.behaviours;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import MAS.agents.BasicAgent;
import MAS.agents.InitiatorAgent;
import MAS.helpers.Constants;
import MAS.helpers.Request;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class InitiatorCFPBehaviour extends OneShotBehaviour {
    private Request request;

    private static InitiatorCFPBehaviour instance;

    public static InitiatorCFPBehaviour getInstance() {
        return instance;
    }


    public InitiatorCFPBehaviour(Request request) {
        this.request = request;
    }

    @Override
    public void onStart() {
        super.onStart();
        instance = this;
    }

    @Override
    public void action() {
        ((BasicAgent)myAgent).print("Begining call for proposal action for request " + request.toString(), ((InitiatorAgent)myAgent).getContext());

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.setContent("Software Development Project: Build a Web Application");
        cfp.setConversationId(InitiatorAgent.conversationId);
        try {
            cfp.setContentObject(this.request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("participant-service");
        sd.setName("participant");
        template.addServices(sd);

        try {
            // Search for agents providing the specified service
            DFAgentDescription[] result = DFService.search(myAgent, template);

            // Check if any agents were found
            if (result.length > 0) {
                for (DFAgentDescription dfd : result) {
                    AID participant = dfd.getName();
                    cfp.addReceiver(participant);
                    //System.out.println("Found agent: " + participant.getName());
                }
            } else {
                System.out.println("No agents found providing the service.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.SECOND, Constants.PROPOSE_DEADLINE_SECONDS);

        cfp.setReplyByDate(cal.getTime());
        myAgent.send(cfp);


        myAgent.addBehaviour(new InitiatorReceiveProposalsBehaviour(myAgent, cal.getTime()));

    }


}
