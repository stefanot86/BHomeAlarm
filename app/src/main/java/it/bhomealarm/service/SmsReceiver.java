package it.bhomealarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;

import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.PhoneNumberUtils;

/**
 * BroadcastReceiver per SMS in arrivo.
 * Filtra messaggi dal sistema allarme e notifica il SmsService.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static OnSmsResultListener listener;

    /**
     * Imposta il listener per messaggi ricevuti.
     */
    public static void setListener(OnSmsResultListener listener) {
        SmsReceiver.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (Constants.ACTION_SMS_RECEIVED.equals(action)) {
            handleIncomingSms(context, intent);
        } else if (Constants.ACTION_SMS_SENT.equals(action)) {
            handleSmsSentResult(intent);
        } else if (Constants.ACTION_SMS_DELIVERED.equals(action)) {
            handleSmsDeliveredResult(intent);
        }
    }

    private void handleIncomingSms(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0) {
            return;
        }

        String format = bundle.getString("format");
        StringBuilder fullMessage = new StringBuilder();
        String sender = null;

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (sender == null) {
                sender = smsMessage.getOriginatingAddress();
            }
            fullMessage.append(smsMessage.getMessageBody());
        }

        if (sender == null || fullMessage.length() == 0) {
            return;
        }

        // Verifica se il messaggio è dal sistema allarme
        if (isFromAlarm(context, sender)) {
            String messageBody = fullMessage.toString();
            notifyMessageReceived(sender, messageBody);

            // Abort broadcast per non mostrare notifica SMS standard
            // Nota: Richiede priority alta nel manifest
            abortBroadcast();
        }
    }

    private void handleSmsSentResult(Intent intent) {
        String messageId = intent.getStringExtra("message_id");
        int resultCode = getResultCode();

        SmsService smsService = SmsService.getInstance(null);
        if (smsService != null) {
            smsService.onSmsSent(messageId, resultCode);
        }
    }

    private void handleSmsDeliveredResult(Intent intent) {
        String messageId = intent.getStringExtra("message_id");
        int resultCode = getResultCode();

        SmsService smsService = SmsService.getInstance(null);
        if (smsService != null) {
            smsService.onSmsDelivered(messageId, resultCode);
        }
    }

    /**
     * Verifica se il mittente è il sistema allarme configurato.
     *
     * @param context Context
     * @param sender Numero mittente
     * @return true se è il numero dell'allarme
     */
    private boolean isFromAlarm(Context context, String sender) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String alarmPhone = prefs.getString(Constants.PREF_ALARM_PHONE, "");

        if (alarmPhone.isEmpty()) {
            return false;
        }

        return PhoneNumberUtils.matches(sender, alarmPhone);
    }

    /**
     * Notifica il listener che è stato ricevuto un messaggio dall'allarme.
     */
    private void notifyMessageReceived(String sender, String body) {
        if (listener != null) {
            listener.onSmsReceived(sender, body);
        }
    }
}
