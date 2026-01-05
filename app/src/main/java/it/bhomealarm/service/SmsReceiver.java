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
 * BroadcastReceiver per la gestione degli SMS in arrivo e dei callback di invio/consegna.
 * <p>
 * Questo receiver gestisce tre tipi di eventi:
 * <ul>
 *     <li><b>SMS_RECEIVED</b>: Intercetta gli SMS in arrivo e filtra quelli provenienti
 *         dal sistema di allarme configurato</li>
 *     <li><b>SMS_SENT</b>: Riceve la conferma che un SMS e' stato inviato correttamente</li>
 *     <li><b>SMS_DELIVERED</b>: Riceve la conferma che un SMS e' stato consegnato al destinatario</li>
 * </ul>
 * <p>
 * Quando un SMS viene ricevuto dal sistema di allarme, il receiver:
 * <ol>
 *     <li>Verifica che il mittente corrisponda al numero dell'allarme configurato</li>
 *     <li>Salva il messaggio nel database per lo storico</li>
 *     <li>Processa la risposta per aggiornare lo stato dell'allarme nelle SharedPreferences</li>
 *     <li>Notifica il listener per l'aggiornamento immediato dell'UI</li>
 *     <li>Blocca il broadcast per evitare la notifica SMS standard del sistema</li>
 * </ol>
 * <p>
 * Il receiver deve essere registrato nel AndroidManifest.xml con le action appropriate
 * e con priorita' alta per intercettare gli SMS prima di altre app.
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see OnSmsResultListener
 * @see SmsService
 * @see SmsParser
 */
public class SmsReceiver extends BroadcastReceiver {

    /**
     * Tag per il logging delle operazioni del receiver.
     */
    private static final String TAG = "SmsReceiver";

    /**
     * Listener statico per notificare i componenti dell'applicazione
     * quando vengono ricevuti messaggi dal sistema di allarme.
     * E' statico per permettere la registrazione prima della ricezione degli SMS.
     */
    private static OnSmsResultListener listener;

    /**
     * Imposta il listener per ricevere notifiche sui messaggi SMS ricevuti.
     * Il listener verra' chiamato sul main thread quando un SMS dal sistema
     * di allarme viene ricevuto.
     *
     * @param listener Implementazione di OnSmsResultListener, o null per rimuovere il listener
     */
    public static void setListener(OnSmsResultListener listener) {
        SmsReceiver.listener = listener;
    }

    /**
     * Restituisce il listener attualmente registrato.
     *
     * @return Il listener corrente, o null se nessun listener e' registrato
     */
    public static OnSmsResultListener getListener() {
        return listener;
    }

    /**
     * Metodo principale chiamato dal sistema Android quando viene ricevuto un broadcast.
     * Gestisce tre tipi di action:
     * <ul>
     *     <li>{@link Constants#ACTION_SMS_RECEIVED}: SMS in arrivo</li>
     *     <li>{@link Constants#ACTION_SMS_SENT}: Conferma invio SMS</li>
     *     <li>{@link Constants#ACTION_SMS_DELIVERED}: Conferma consegna SMS</li>
     * </ul>
     *
     * @param context Contesto dell'applicazione
     * @param intent  Intent contenente i dati del broadcast
     */
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

    /**
     * Gestisce la ricezione di un SMS in arrivo.
     * Estrae il mittente e il corpo del messaggio dai PDU, verifica se proviene
     * dal sistema di allarme e, in caso positivo, processa il messaggio.
     *
     * @param context Contesto dell'applicazione
     * @param intent  Intent contenente i PDU dell'SMS
     */
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
     * Salva il messaggio ricevuto nel database per lo storico delle comunicazioni.
     * Il messaggio viene salvato con direzione INCOMING e stato RECEIVED.
     *
     * @param context     Contesto dell'applicazione per accedere al repository
     * @param messageBody Corpo del messaggio SMS ricevuto
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
     * Processa la risposta SMS e salva lo stato dell'allarme nelle SharedPreferences.
     * <p>
     * Questo metodo assicura che lo stato dell'allarme sia aggiornato anche quando
     * l'applicazione non e' in primo piano. Utilizza {@link SmsParser} per interpretare
     * la risposta e estrarre lo stato corrente del sistema.
     *
     * @param context     Contesto dell'applicazione
     * @param messageBody Corpo del messaggio SMS da processare
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

    /**
     * Gestisce il callback di conferma invio SMS.
     * Recupera l'ID del messaggio e il codice risultato e li inoltra al SmsService.
     *
     * @param intent Intent contenente l'ID del messaggio
     */
    private void handleSmsSentResult(Intent intent) {
        String messageId = intent.getStringExtra("message_id");
        int resultCode = getResultCode();

        SmsService smsService = SmsService.getInstance(null);
        if (smsService != null) {
            smsService.onSmsSent(messageId, resultCode);
        }
    }

    /**
     * Gestisce il callback di conferma consegna SMS.
     * Recupera l'ID del messaggio e il codice risultato e li inoltra al SmsService.
     *
     * @param intent Intent contenente l'ID del messaggio
     */
    private void handleSmsDeliveredResult(Intent intent) {
        String messageId = intent.getStringExtra("message_id");
        int resultCode = getResultCode();

        SmsService smsService = SmsService.getInstance(null);
        if (smsService != null) {
            smsService.onSmsDelivered(messageId, resultCode);
        }
    }

    /**
     * Verifica se il mittente dell'SMS corrisponde al numero del sistema di allarme configurato.
     * Utilizza {@link PhoneNumberUtils#matches(String, String)} per un confronto flessibile
     * che gestisce diversi formati di numeri telefonici.
     *
     * @param context Contesto per accedere alle SharedPreferences
     * @param sender  Numero di telefono del mittente dell'SMS
     * @return true se il mittente corrisponde al numero dell'allarme, false altrimenti
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
     * Notifica il listener che e' stato ricevuto un messaggio dal sistema di allarme.
     * La notifica viene eseguita sul main thread tramite Handler per garantire
     * la compatibilita' con LiveData e l'aggiornamento dell'UI.
     *
     * @param sender Numero di telefono del mittente
     * @param body   Corpo del messaggio SMS
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
