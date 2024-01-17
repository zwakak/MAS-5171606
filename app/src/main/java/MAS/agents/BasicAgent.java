package MAS.agents;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import MAS.helpers.Constants;
import MAS.helpers.Request;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BasicAgent extends Agent {

    public static String conversationId = "fipa-contract-net-interaction-conversation";
    public void print(String message, Context context){
        System.out.println(" ******** " + this.getLocalName() + " Message ******** ");
        System.out.println(" ******** "+message+" ******** ");
        System.out.println(" ******** " + this.getLocalName() + " End Of Message  ******** ");

        Intent broadcast = new Intent();
        broadcast.setAction(Constants.COMMUNICATION_ACTION);
        broadcast.putExtra("message", "Message from " + this.getLocalName()
                + "\n\n" + message + "\n\n" +
                this.getLocalName() + " End Of Message \n");



        context.sendBroadcast(broadcast);
    }

    public void registerInDF(String serviceName, String serviceType, Agent agent) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setName(serviceName);
        sd.setType(serviceType);

        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
