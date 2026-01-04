package it.bhomealarm.service;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.util.Constants;

/**
 * Service per invio SMS con supporto Dual-SIM.
 */
public class SmsService {

    private static SmsService instance;

    private final Context context;
    private OnSmsResultListener listener;
    private int selectedSimSlot = 0;

    private SmsService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized SmsService getInstance(Context context) {
        if (instance == null) {
            instance = new SmsService(context);
        }
        return instance;
    }

    // ========== Configuration ==========

    /**
     * Imposta il listener per risultati SMS.
     */
    public void setListener(OnSmsResultListener listener) {
        this.listener = listener;
    }

    /**
     * Imposta lo slot SIM da utilizzare.
     *
     * @param simSlot Indice SIM (0 o 1)
     */
    public void setSelectedSimSlot(int simSlot) {
        this.selectedSimSlot = simSlot;
    }

    /**
     * Restituisce lo slot SIM selezionato.
     */
    public int getSelectedSimSlot() {
        return selectedSimSlot;
    }

    // ========== SIM Information ==========

    /**
     * Verifica se il dispositivo ha capacità Dual-SIM.
     *
     * @return true se supporta Dual-SIM
     */
    public boolean isDualSim() {
        return getSimCount() > 1;
    }

    /**
     * Restituisce il numero di SIM disponibili.
     *
     * @return Numero di SIM attive
     */
    public int getSimCount() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 1;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);
            List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();
            return subscriptions != null ? subscriptions.size() : 1;
        }

        return 1;
    }

    /**
     * Restituisce informazioni sulle SIM disponibili.
     *
     * @return Lista di SimInfo
     */
    public List<SimInfo> getAvailableSims() {
        List<SimInfo> sims = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            sims.add(new SimInfo(0, "SIM Predefinita", ""));
            return sims;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);
            List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();

            if (subscriptions != null) {
                for (SubscriptionInfo info : subscriptions) {
                    String displayName = info.getDisplayName() != null
                            ? info.getDisplayName().toString()
                            : "SIM " + (info.getSimSlotIndex() + 1);
                    String carrierName = info.getCarrierName() != null
                            ? info.getCarrierName().toString()
                            : "";

                    sims.add(new SimInfo(
                            info.getSimSlotIndex(),
                            displayName,
                            carrierName
                    ));
                }
            }
        }

        if (sims.isEmpty()) {
            sims.add(new SimInfo(0, "SIM Predefinita", ""));
        }

        return sims;
    }

    /**
     * Info su una SIM.
     */
    public static class SimInfo {
        public final int slot;
        public final String displayName;
        public final String carrierName;

        public SimInfo(int slot, String displayName, String carrierName) {
            this.slot = slot;
            this.displayName = displayName;
            this.carrierName = carrierName;
        }
    }

    // ========== Send SMS ==========

    /**
     * Invia un SMS.
     *
     * @param phoneNumber Numero destinatario
     * @param message Testo del messaggio
     * @return ID univoco del messaggio
     */
    public String sendSms(String phoneNumber, String message) {
        return sendSms(phoneNumber, message, selectedSimSlot);
    }

    /**
     * Invia un SMS con SIM specifica.
     *
     * @param phoneNumber Numero destinatario
     * @param message Testo del messaggio
     * @param simSlot Slot SIM da utilizzare
     * @return ID univoco del messaggio
     */
    public String sendSms(String phoneNumber, String message, int simSlot) {
        String messageId = UUID.randomUUID().toString();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onSmsError(Activity.RESULT_CANCELED, "Permesso SMS non concesso");
            }
            return null;
        }

        try {
            SmsManager smsManager = getSmsManager(simSlot);

            // Crea PendingIntent per callback invio
            Intent sentIntent = new Intent(Constants.ACTION_SMS_SENT);
            sentIntent.putExtra("message_id", messageId);
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    context,
                    0,
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Crea PendingIntent per callback consegna
            Intent deliveredIntent = new Intent(Constants.ACTION_SMS_DELIVERED);
            deliveredIntent.putExtra("message_id", messageId);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(
                    context,
                    0,
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Invia SMS
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    sentPI,
                    deliveredPI
            );

            return messageId;

        } catch (Exception e) {
            if (listener != null) {
                listener.onSmsError(-1, "Errore invio SMS: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Invia un comando al sistema allarme.
     *
     * @param alarmPhoneNumber Numero del sistema allarme
     * @param command Comando da inviare
     * @return ID univoco del messaggio
     */
    public String sendCommand(String alarmPhoneNumber, String command) {
        return sendSms(alarmPhoneNumber, command, selectedSimSlot);
    }

    private SmsManager getSmsManager(int simSlot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {

                List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();
                if (subscriptions != null && simSlot < subscriptions.size()) {
                    int subscriptionId = subscriptions.get(simSlot).getSubscriptionId();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return context.getSystemService(SmsManager.class)
                                .createForSubscriptionId(subscriptionId);
                    } else {
                        return SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
                    }
                }
            }
        }

        // Fallback a SmsManager default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getSystemService(SmsManager.class);
        } else {
            return SmsManager.getDefault();
        }
    }

    // ========== Callbacks from BroadcastReceiver ==========

    /**
     * Chiamato da SmsResultReceiver quando SMS inviato.
     */
    public void onSmsSent(String messageId, int resultCode) {
        if (listener != null) {
            if (resultCode == Activity.RESULT_OK) {
                listener.onSmsSent(messageId);
            } else {
                listener.onSmsError(resultCode, getErrorMessage(resultCode));
            }
        }
    }

    /**
     * Chiamato da SmsResultReceiver quando SMS consegnato.
     */
    public void onSmsDelivered(String messageId, int resultCode) {
        if (listener != null) {
            if (resultCode == Activity.RESULT_OK) {
                listener.onSmsDelivered(messageId);
            }
        }
    }

    private String getErrorMessage(int resultCode) {
        switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "Errore generico";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "Nessun servizio";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "PDU nullo";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "Radio spenta";
            default:
                return "Errore sconosciuto (" + resultCode + ")";
        }
    }
}
