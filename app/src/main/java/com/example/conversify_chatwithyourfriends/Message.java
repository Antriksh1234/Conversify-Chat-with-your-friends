package com.example.conversify_chatwithyourfriends;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    String sender;
    String text;
    int hour,minutes;
    private @ServerTimestamp  Date timestamp;
    Message(){
        //Do nothing , created only for firebase as otherwise it gives error
    }
    Message(String sender,String text,int hour,int minutes){
        this.sender = sender;
        this.text = text;
        this.hour = hour;
        this.minutes = minutes;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHour() {
        return hour;
    }
}
