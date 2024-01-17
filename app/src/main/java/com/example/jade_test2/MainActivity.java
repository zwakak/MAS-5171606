package com.example.jade_test2;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.logging.Level;

import MAS.agents.InitiatorAgent;
import MAS.agents.ParticipantAgent;
import MAS.helpers.Constants;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class MainActivity extends Activity implements OnClickListener {
    private Logger logger = Logger.getJADELogger(this.getClass().getName());
    private LinearLayout containerLayout;

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    private MyReceiver myReceiver;

    private TextView tvNumParticipants;
    private Button btnPlus, btnMinus;
    private TextView tvResults;
    private EditText etDuration, etRequiredNumOfEmployees;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerLayout = findViewById(R.id.containerLayout);

        tvNumParticipants = findViewById(R.id.tv_num_participants);
        tvResults = findViewById(R.id.tv_results);
        modifyAgentsLayout();

        btnMinus = findViewById(R.id.btn_minus);
        btnMinus.setOnClickListener(this);

        btnPlus = findViewById(R.id.btn_plus);
        btnPlus.setOnClickListener(this);

        etRequiredNumOfEmployees = findViewById(R.id.et_required_num_employees);
        etDuration = findViewById(R.id.et_duration);

        myReceiver = new MyReceiver();


        IntentFilter communicationFilter = new IntentFilter();
        communicationFilter.addAction(Constants.COMMUNICATION_ACTION);
        registerReceiver(myReceiver, communicationFilter);


        Button button = (Button) findViewById(R.id.btn_start_simulation);
        button.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(myReceiver);
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        logger.log(Level.INFO, "Destroy activity!");
    }






    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
        @Override
        public void onSuccess(AgentController agent) {
        }

        @Override
        public void onFailure(Throwable throwable) {
            logger.log(Level.INFO, "Nickname already in use!");
        }
    };




    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.log(Level.INFO, "Received intent " + action);
            if (action.equalsIgnoreCase(Constants.COMMUNICATION_ACTION)) {
                tvResults.setText(tvResults.getText() + "\n"
                        + intent.getStringExtra("message").toString() +"\n\n");
            }
        }
    }


    public void startSimulation(final String nickname, final String host,
                                final String port,
                                final RuntimeCallback<AgentController> agentStartupCallback) {

        tvResults.setText("");
        final Properties profile = new Properties();
        profile.setProperty(Profile.MAIN_HOST, host);
        profile.setProperty(Profile.MAIN_PORT, port);
        profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
        profile.setProperty(Profile.JVM, Profile.ANDROID);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            profile.setProperty(Profile.LOCAL_HOST,
                    AndroidHelper.getLocalIPAddress());
        }
        // Emulator: this is not really needed on a real device
        profile.setProperty(Profile.LOCAL_PORT, "2000");

        if (microRuntimeServiceBinder == null) {
            serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                    logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
                    startContainer(profile, agentStartupCallback);
                };

                public void onServiceDisconnected(ComponentName className) {
                    microRuntimeServiceBinder = null;
                    logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
                }
            };
            logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");

            bindService(new Intent(getApplicationContext(),
                            MicroRuntimeService.class), serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
            startContainer(profile, agentStartupCallback);
        }
    }

    private void startContainer(Properties profile,
                                final RuntimeCallback<AgentController> agentStartupCallback) {
        microRuntimeServiceBinder.stopAgentContainer(new RuntimeCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (!MicroRuntime.isRunning()) {
                    microRuntimeServiceBinder.startAgentContainer(profile,
                            new RuntimeCallback<Void>() {
                                @Override
                                public void onSuccess(Void thisIsNull) {
                                    logger.log(Level.INFO, "Successfully start of the container...");
                                    startMAS();
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    logger.log(Level.SEVERE, "Failed to start the container...");
                                }
                            });
                } else {
                    startMAS();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });


    }

    private void startMAS() {

        ArrayList<Object> initiatorArgs = new ArrayList<>();
        initiatorArgs.add(getApplicationContext());
        initiatorArgs.add(etDuration.getText().toString());
        initiatorArgs.add(etRequiredNumOfEmployees.getText().toString());
        initiatorArgs.add(tvNumParticipants.getText().toString());

        startAgent(InitiatorAgent.class.getName(), "Initiator", initiatorArgs.toArray(), agentStartupCallback);

        int numberOfParticipants = Integer.parseInt(tvNumParticipants.getText().toString());

        for (int i =0; i< numberOfParticipants; i++){
            ArrayList<Object> participantArgs = new ArrayList<>();
            participantArgs.add(getApplicationContext());
            EditText etNumOfEmployees = containerLayout.findViewWithTag("et_agent_" + i + "_num_of_employees");
            participantArgs.add(etNumOfEmployees.getText().toString());

            EditText etCostPerEmployee = containerLayout.findViewWithTag("et_agent_" + i + "_cost_per_employee");
            participantArgs.add(etCostPerEmployee.getText().toString());


            startAgent(ParticipantAgent.class.getName(), ((EditText)containerLayout.getChildAt(i)
                            .findViewWithTag("et_agent_" + i + "_name")).getText().toString(),
                    participantArgs.toArray(), agentStartupCallback);
        }
    }

    private void startAgent(String className, final String nickname, Object[] args,
                            final RuntimeCallback<AgentController> agentStartupCallback) {

        microRuntimeServiceBinder.startAgent(nickname,
                className,
                args,
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        logger.log(Level.INFO, "Successfully start of the "
                                + className + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            agentStartupCallback.onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        logger.log(Level.SEVERE, "Failed to start the "
                                + className + "...");
                        agentStartupCallback.onFailure(throwable);
                    }


                });
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.btn_minus){
            int currentNumParticipants = Integer.parseInt(tvNumParticipants.getText().toString());
            if(currentNumParticipants>4){
                currentNumParticipants--;
                tvNumParticipants.setText(currentNumParticipants);
                modifyAgentsLayout();
            }else Toast.makeText(getApplicationContext(), "Minimum allowed number of participants is 4", Toast.LENGTH_SHORT).show();
        }else if(id == R.id.btn_plus){
            int currentNumParticipants = Integer.parseInt(tvNumParticipants.getText().toString());
            currentNumParticipants++;
            tvNumParticipants.setText(currentNumParticipants+"");
            modifyAgentsLayout();

        }else if(id == R.id.btn_start_simulation) {
            String host = "10.0.2.2";
            String port = "1099";

            startSimulation("nickname", host, port, agentStartupCallback);
        }
    }

    private void modifyAgentsLayout() {

        int numOfParticipants = Integer.parseInt(tvNumParticipants.getText().toString());
        containerLayout.removeAllViews();
        // Add EditText fields for agent parameters
        for (int i = 0; i < numOfParticipants; i++) {
            LinearLayout agentLayout = new LinearLayout(this);
            agentLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams agentLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            agentLayoutParams.setMargins(0, 20, 0, 20);
            agentLayout.setLayoutParams(agentLayoutParams);
            // Add TextView for agent label
            TextView agentLabel = new TextView(this);
            agentLabel.setTextColor(getResources().getColor(R.color.black, null));
            agentLabel.setText("Agent " + (i+1));
            agentLayout.addView(agentLabel);

            LinearLayout.LayoutParams etAgentNameLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            etAgentNameLayoutParams.setMargins(0, 40, 0, 40);

            EditText etAgentName = new EditText(this);

            etAgentName.setTag("et_agent_" + i + "_name");
            etAgentName.setLayoutParams(etAgentNameLayoutParams);
            etAgentName.setMaxLines(1);
            etAgentName.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
            etAgentName.setHint("Nickname of agent " + (i + 1));
            agentLayout.addView(etAgentName);

            EditText etNumOfEmployees = new EditText(this);
            etNumOfEmployees.setTag("et_agent_" + i + "_num_of_employees");

            etNumOfEmployees.setLayoutParams(etAgentNameLayoutParams);
            etNumOfEmployees.setMaxLines(1);
            etNumOfEmployees.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
            etNumOfEmployees.setHint("Number of Employees");
            etNumOfEmployees.setInputType(InputType.TYPE_CLASS_NUMBER);
            agentLayout.addView(etNumOfEmployees);

            EditText etCostPerEmployee = new EditText(this);
            etCostPerEmployee.setTag("et_agent_" + i + "_cost_per_employee");

            etCostPerEmployee.setLayoutParams(etAgentNameLayoutParams);
            etCostPerEmployee.setMaxLines(1);
            etCostPerEmployee.setBackground(getResources().getDrawable(R.drawable.edit_text_background));
            etCostPerEmployee.setHint("Cost per Employee");
            etCostPerEmployee.setInputType(InputType.TYPE_CLASS_NUMBER);
            agentLayout.addView(etCostPerEmployee);


            View separator = new View(this);
            separator.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 2));
            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            separator.setLayoutParams(agentLayoutParams);
            agentLayout.addView(separator);

            // Add the agentLayout to the main container
            containerLayout.addView(agentLayout);
            // Add a separator line between agents
        }


    }



}
