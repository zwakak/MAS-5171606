package MAS.agents;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.logging.Level;

import MAS.behaviours.InitiatorCFPBehaviour;
import MAS.behaviours.InitiatorContinueConversationBehaviour;
import MAS.behaviours.InitiatorReceiveProposalsBehaviour;
import MAS.helpers.Request;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class InitiatorAgent extends BasicAgent {

   private Request request;
   private Context context;

    public static int nResponders;
    @Override
    protected void setup() {
        super.setup();

        this.registerInDF("initiator", "initiator-service", this);
        Object[] args = getArguments();

        if(args!=null&& args.length>0) {
            context = (Context) getArguments()[0];
            request = new Request(Integer.parseInt(getArguments()[1].toString()),
                    Integer.parseInt(getArguments()[2].toString()));
            nResponders = Integer.parseInt(getArguments()[3].toString());
        }
        addBehaviour(new InitiatorCFPBehaviour(request));
    }

    public Request getRequest() {
        return request;
    }

    public void restartBehaviours(){
        removeBehaviour(InitiatorCFPBehaviour.getInstance());
        removeBehaviour(InitiatorReceiveProposalsBehaviour.getInstance());
        removeBehaviour(InitiatorContinueConversationBehaviour.getInstance());

        // Add the new behavior
        addBehaviour(new InitiatorCFPBehaviour(request));

        // Activate all the behaviors
        doWake();

    }


    public Context getContext() {
        return context;
    }
}
