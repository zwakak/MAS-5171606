package MAS.helpers;

import jade.core.AID;

import java.io.Serializable;
import java.time.Duration;

public class Request implements Serializable {
    private int duration;
    private int requiredNumOfEmployees;

    public Request(int duration, int requiredNumOfEmployees) {
        this.duration = duration;
        this.requiredNumOfEmployees = requiredNumOfEmployees;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRequiredNumOfEmployees() {
        return requiredNumOfEmployees;
    }

    public void setRequiredNumOfEmployees(int requiredNumOfEmployees) {
        this.requiredNumOfEmployees = requiredNumOfEmployees;
    }

    @Override
    public String toString() {
        return "Request{" +
                "duration=" + duration +
                ", requiredNumOfEmployees=" + requiredNumOfEmployees + '\'' +
                '}';
    }

    public static Request fromString(String content) {
        // Parse a string representation of a proposal
        String[] parts = content.split(",");

        int duration = Integer.parseInt(parts[0]);
        int requiredNumOfEmployees = Integer.parseInt(parts[1]);
        return new Request(duration, requiredNumOfEmployees);
    }
}
