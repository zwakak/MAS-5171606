package MAS.agents;


import android.content.Context;

import MAS.behaviours.ParticipantBehaviour;

public class ParticipantAgent extends BasicAgent{
    private int numEmployees;
    private double costPerEmployee;

    private Context context;


    @Override
    protected void setup() {
        super.setup();

        this.registerInDF("participant", "participant-service", this);
        Object[] args = getArguments();
        if(args!=null&& args.length>0){
            context = (Context) args[0];
            numEmployees = Integer.parseInt(args[1].toString());
            costPerEmployee = Double.parseDouble(args[2].toString());
        }
        addBehaviour(new ParticipantBehaviour(numEmployees, costPerEmployee));
    }



    @Override
    protected void takeDown() {
        super.takeDown();
    }

    public Context getContext() {
        return context;
    }
}
