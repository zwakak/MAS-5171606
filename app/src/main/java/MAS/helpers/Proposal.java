package MAS.helpers;
import jade.core.AID;

import java.io.Serializable;
import java.util.Objects;

public class Proposal implements Serializable {

    private final AID agentIdentifier;
    private final double cost;
    private final int duration;

    public Proposal(AID agentIdentifier, double cost, int duration) {
        this.agentIdentifier = agentIdentifier;
        this.cost = cost;
        this.duration = duration;
    }

    public AID getAgentIdentifier() {
        return agentIdentifier;
    }

    public double getCost() {
        return cost;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isBetterThan(Proposal other) {

        double thisScore = calculateScore();
        double otherScore = other.calculateScore();
        return thisScore < otherScore;
    }

    private double calculateScore() {
        return cost * duration;
    }

    @Override
    public String toString() {
        return "Proposal{" +
                "participant=" + agentIdentifier.getLocalName() +
                ", cost=" + cost +
                ", duration=" + duration +

                '}';
    }

    public static Proposal fromString(String content) {
        String[] parts = content.split(",");
        AID participant = new AID(parts[0], AID.ISGUID);
        int cost = Integer.parseInt(parts[1]);
        int duration = Integer.parseInt(parts[2]);
        return new Proposal(participant, cost, duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proposal proposal = (Proposal) o;
        return Double.compare(proposal.cost, cost) == 0 && duration == proposal.duration && Objects.equals(agentIdentifier, proposal.agentIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentIdentifier, cost, duration);
    }
}
