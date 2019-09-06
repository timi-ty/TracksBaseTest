package com.example.tracksbasetest;

import java.io.Serializable;
import java.util.List;

public class BroadcastMessageData implements Serializable {

    private List<Double> broadcastEpicentre;

    private String broadcastMessage;

    public BroadcastMessageData(){}

    public BroadcastMessageData(List<Double> broadcastEpicentre, String broadcastMessage) {
        this.broadcastEpicentre = broadcastEpicentre;
        this.broadcastMessage = broadcastMessage;
    }

    public List<Double> getBroadcastEpicentre() {
        return broadcastEpicentre;
    }

    public void setBroadcastEpicentre(List<Double> broadcastEpicentre) {
        this.broadcastEpicentre = broadcastEpicentre;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(String broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    @Override
    public String toString() {
        return "BroadcastMessageData{" +
                "broadcastEpicentre=" + broadcastEpicentre +
                ", broadcastMessage='" + broadcastMessage + '\'' +
                '}';
    }
}
