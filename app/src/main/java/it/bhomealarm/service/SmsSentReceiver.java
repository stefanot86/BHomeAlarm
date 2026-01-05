package it.bhomealarm.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * BroadcastReceiver dedicato alla gestione dei callback di invio e consegna SMS.
 * <p>
 * Questo receiver e' registrato nel AndroidManifest.xml per ricevere due tipi di broadcast:
 * <ul>
 *     <li><b>{@link Constants#ACTION_SMS_SENT}</b>: Notifica quando un SMS e' stato
 *         trasmesso con successo alla rete cellulare</li>
 *     <li><b>{@link Constants#ACTION_SMS_DELIVERED}</b>: Notifica quando un SMS
 *         e' stato effettivamente consegnato al destinatario</li>
 * </ul>
 * <p>
 * Per ogni evento, il receiver:
 * <ol>
 *     <li>Estrae l'ID univoco del messaggio dall'intent</li>
 *     <li>Verifica il codice risultato dell'operazione</li>
 *     <li>Aggiorna lo stato del messaggio nel database tramite {@link AlarmRepository}</li>
 *     <li>Notifica il {@link SmsService} per propagare l'evento ai listener</li>
 * </ol>
 * <p>
 * Gli stati possibili del messaggio nel database sono:
 * <ul>
 *     <li>PENDING: In attesa di invio</li>
 *     <li>SENT: Inviato alla rete</li>
 *     <li>DELIVERED: Consegnato al destinatario</li>
 *     <li>FAILED: Invio fallito con messaggio di errore</li>
 * </ul>
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see SmsService
 * @see SmsLog
 * @see AlarmRepository
 */
public class SmsSentReceiver extends BroadcastReceiver {

    /**
     * Tag per il logging delle operazioni del receiver.
     */
    private static final String TAG = "SmsSentReceiver";

    /**
     * Metodo principale chiamato dal sistema Android quando viene ricevuto un broadcast
     * relativo allo stato di invio o consegna di un SMS.
     * <p>
     * Gestisce le action {@link Constants#ACTION_SMS_SENT} e {@link Constants#ACTION_SMS_DELIVERED},
     * aggiornando il database e notificando il SmsService del risultato.
     *
     * @param context Contesto dell'applicazione
     * @param intent  Intent contenente l'action e l'ID del messaggio
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String messageId = intent.getStringExtra("message_id");
        if (messageId == null) {
            Log.w(TAG, "Received intent without message_id");
            return;
        }

        String action = intent.getAction();
        int resultCode = getResultCode();

        Log.d(TAG, "Received action: " + action + ", messageId: " + messageId + ", resultCode: " + resultCode);

        AlarmRepository repository = AlarmRepository.getInstance((android.app.Application) context.getApplicationContext());
        SmsService smsService = SmsService.getInstance(context);

        switch (action) {
            case Constants.ACTION_SMS_SENT:
                handleSmsSent(repository, smsService, messageId, resultCode);
                break;

            case Constants.ACTION_SMS_DELIVERED:
                handleSmsDelivered(repository, smsService, messageId, resultCode);
                break;

            default:
                Log.w(TAG, "Unknown action: " + action);
                break;
        }
    }

    /**
     * Gestisce il callback di conferma invio SMS.
     * <p>
     * Se l'invio e' avvenuto con successo (RESULT_OK), aggiorna lo stato del messaggio
     * a SENT nel database. In caso di errore, salva lo stato FAILED insieme al
     * messaggio di errore descrittivo.
     *
     * @param repository Repository per l'accesso al database
     * @param smsService Servizio SMS per notificare i listener
     * @param messageId  ID univoco del messaggio
     * @param resultCode Codice risultato dell'operazione (Activity.RESULT_OK per successo)
     */
    private void handleSmsSent(AlarmRepository repository, SmsService smsService,
                               String messageId, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "SMS sent successfully: " + messageId);
            repository.updateSmsLogStatus(messageId, SmsLog.STATUS_SENT);
            smsService.onSmsSent(messageId, resultCode);
        } else {
            String errorMessage = getErrorMessage(resultCode);
            Log.e(TAG, "SMS send failed: " + messageId + ", error: " + errorMessage);
            repository.updateSmsLogStatusWithError(messageId, SmsLog.STATUS_FAILED, errorMessage);
            smsService.onSmsSent(messageId, resultCode);
        }
    }

    /**
     * Gestisce il callback di conferma consegna SMS.
     * <p>
     * Se la consegna e' avvenuta con successo (RESULT_OK), aggiorna lo stato del messaggio
     * a DELIVERED nel database. In caso di errore nel report di consegna, non modifica
     * lo stato a FAILED perche' l'SMS potrebbe essere stato consegnato ma il report
     * di consegna non e' disponibile (dipende dall'operatore).
     *
     * @param repository Repository per l'accesso al database
     * @param smsService Servizio SMS per notificare i listener
     * @param messageId  ID univoco del messaggio
     * @param resultCode Codice risultato dell'operazione (Activity.RESULT_OK per successo)
     */
    private void handleSmsDelivered(AlarmRepository repository, SmsService smsService,
                                    String messageId, int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "SMS delivered successfully: " + messageId);
            repository.updateSmsLogStatus(messageId, SmsLog.STATUS_DELIVERED);
            smsService.onSmsDelivered(messageId, resultCode);
        } else {
            Log.w(TAG, "SMS delivery report: " + messageId + ", resultCode: " + resultCode);
            // Non cambiamo lo stato a FAILED perché l'SMS potrebbe essere stato
            // consegnato ma il report di consegna non è disponibile
            smsService.onSmsDelivered(messageId, resultCode);
        }
    }

    /**
     * Converte un codice di errore SmsManager in un messaggio descrittivo in italiano.
     * <p>
     * I codici di errore sono definiti in {@link SmsManager} e includono:
     * <ul>
     *     <li>RESULT_ERROR_GENERIC_FAILURE: Errore generico di invio</li>
     *     <li>RESULT_ERROR_NO_SERVICE: Nessun servizio di rete</li>
     *     <li>RESULT_ERROR_NULL_PDU: Errore nel formato PDU</li>
     *     <li>RESULT_ERROR_RADIO_OFF: Radio cellulare disattivata</li>
     *     <li>RESULT_ERROR_LIMIT_EXCEEDED: Limite SMS raggiunto</li>
     *     <li>RESULT_ERROR_SHORT_CODE_NOT_ALLOWED: Numero breve non consentito</li>
     *     <li>RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED: Numero breve bloccato permanentemente</li>
     * </ul>
     *
     * @param resultCode Codice errore restituito da SmsManager
     * @return Messaggio di errore descrittivo in italiano
     */
    private String getErrorMessage(int resultCode) {
        switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "Errore generico di invio";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "Nessun servizio di rete disponibile";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "Errore PDU nullo";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "Radio cellulare spenta";
            case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED:
                return "Limite SMS superato";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED:
                return "Numero breve non consentito";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED:
                return "Numero breve mai consentito";
            default:
                return "Errore sconosciuto (codice: " + resultCode + ")";
        }
    }
}
