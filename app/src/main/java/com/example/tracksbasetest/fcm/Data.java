package com.example.tracksbasetest.fcm;

import com.example.tracksbasetest.BroadcastMessageData;

public class Data {
    private BroadcastMessageData messageData;
    private String dataType;

    public Data(BroadcastMessageData messageData, String dataType) {
        this.messageData = messageData;
        this.dataType = dataType;
    }

    public Data() {}

    public BroadcastMessageData getMessageData() {
        return messageData;
    }

    public String getDataType() {
        return dataType;
    }

    public void setMessageData(BroadcastMessageData messageData) {
        this.messageData = messageData;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "Data{" +
                "messageData='" + messageData.toString() + '\'' +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}
