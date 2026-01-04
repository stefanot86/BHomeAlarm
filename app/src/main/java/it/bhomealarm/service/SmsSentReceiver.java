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
 * BroadcastReceiver per gestire i callback di invio e consegna SMS.
 * Registrato nel AndroidManifest per ricevere:
 * - ACTION_SMS_SENT: quando l'SMS è stato inviato
 * - ACTION_SMS_DELIVERED: quando l'SMS è stato consegnato
 */
public class SmsSentReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsSentReceiver";

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
