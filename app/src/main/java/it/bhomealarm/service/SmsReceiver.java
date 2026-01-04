package it.bhomealarm.service;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsMessage;
import android.util.Log;

import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.PhoneNumberUtils;
import it.bhomealarm.util.SmsParser;

/**
 * BroadcastReceiver per SMS in arrivo.
 * Filtra messaggi dal sistema allarme e notifica il SmsService.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";
    private static OnSmsResultListener listener;

    /**
     * Imposta il listener per messaggi ricevuti.
     */
    public static void setListener(OnSmsResultListener listener) {
        SmsReceiver.listener = listener;
    }

    /**
     * Restituisce il listener attuale.
     */
    public static OnSmsResultListener getListener() {
        return listener;
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
            Log.d(TAG, "SMS ricevuto dall'allarme: " + messageBody);

            // Salva sempre nel database
            saveToDatabase(context, messageBody);

            // Processa la risposta e aggiorna lo stato nelle SharedPreferences
            processAndSaveStatus(context, messageBody);

            // Notifica il listener se presente (per aggiornamento UI immediato)
            notifyMessageReceived(sender, messageBody);

            // Abort broadcast per non mostrare notifica SMS standard
            abortBroadcast();
        }
    }

    /**
     * Salva il messaggio nel database.
     */
    private void saveToDatabase(Context context, String messageBody) {
        try {
            AlarmRepository repository = AlarmRepository.getInstance((Application) context.getApplicationContext());
            SmsLog log = new SmsLog();
            log.setMessage(messageBody);
            log.setDirection(SmsLog.DIRECTION_INCOMING);
            log.setStatus(SmsLog.STATUS_RECEIVED);
            log.setTimestamp(System.currentTimeMillis());
            repository.insertSmsLog(log);
        } catch (Exception e) {
            Log.e(TAG, "Errore salvataggio SMS nel database", e);
        }
    }

    /**
     * Processa la risposta SMS e salva lo stato nelle SharedPreferences.
     * Questo assicura che lo stato sia aggiornato anche se l'app non è in foreground.
     */
    private void processAndSaveStatus(Context context, String messageBody) {
        try {
            String responseType = SmsParser.identifyResponse(messageBody);

            if ("OK".equals(responseType) || "STATUS".equals(responseType)) {
                SmsParser.ResponseData data = SmsParser.parseResponse(messageBody);

                if (data.success && data.status != null) {
                    SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString(Constants.PREF_LAST_STATUS, data.status)
                            .putLong(Constants.PREF_LAST_CHECK_TIME, System.currentTimeMillis())
                            .apply();
                    Log.d(TAG, "Stato salvato: " + data.status);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Errore processamento risposta SMS", e);
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
     * Esegue sul main thread per sicurezza con LiveData.
     */
    private void notifyMessageReceived(String sender, String body) {
        if (listener != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                // Ricontrolla listener perché potrebbe essere cambiato
                if (listener != null) {
                    listener.onSmsReceived(sender, body);
                }
            });
        }
    }
}
