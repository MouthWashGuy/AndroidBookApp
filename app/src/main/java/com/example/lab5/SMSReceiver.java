package com.example.lab5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // get messages and shove them into array
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        // for each message
        for (SmsMessage currentMessage : messages) {
            String message = currentMessage.getDisplayMessageBody();
            // create intent and broadcast it
            Intent myIntent = new Intent();
            myIntent.setAction("MySMS");
            myIntent.putExtra("bookData", message);
            context.sendBroadcast(myIntent);
        }
    }
}