package it.bhomealarm.callback;

/**
 * Interfaccia callback per la gestione dei risultati delle operazioni SMS.
 * <p>
 * Questa interfaccia definisce i metodi di callback per monitorare il ciclo
 * di vita di un messaggio SMS inviato al sistema di allarme BHome:
 * <ul>
 *     <li>Invio del messaggio</li>
 *     <li>Conferma di consegna</li>
 *     <li>Ricezione della risposta</li>
 *     <li>Gestione errori e timeout</li>
 * </ul>
 * <p>
 * Implementare questa interfaccia per ricevere notifiche sullo stato
 * delle comunicazioni SMS con il sistema di allarme.
 *
 * @author BHomeAlarm Team
 * @version 1.0
 */
public interface OnSmsResultListener {

    /**
     * Chiamato quando l'SMS e' stato inviato con successo dalla coda di invio.
     * <p>
     * Nota: questo callback indica che l'SMS e' stato accettato dal sistema
     * operativo per l'invio, non che sia stato effettivamente consegnato.
     *
     * @param messageId l'identificatore univoco del messaggio inviato,
     *                  utilizzabile per tracciare la risposta corrispondente
     */
    void onSmsSent(String messageId);

    /**
     * Chiamato quando l'SMS e' stato consegnato al destinatario.
     * <p>
     * Questo callback conferma che il messaggio ha raggiunto il sistema
     * di allarme. Richiede che il report di consegna sia abilitato.
     *
     * @param messageId l'identificatore univoco del messaggio consegnato
     */
    void onSmsDelivered(String messageId);

    /**
     * Chiamato quando si riceve una risposta SMS dal sistema di allarme.
     * <p>
     * Il contenuto del messaggio dovra' essere parsato utilizzando
     * {@link it.bhomealarm.util.SmsParser} per estrarre i dati strutturati.
     *
     * @param sender il numero telefonico del mittente (sistema di allarme)
     * @param body il contenuto testuale del messaggio SMS ricevuto
     */
    void onSmsReceived(String sender, String body);

    /**
     * Chiamato in caso di errore durante l'invio dell'SMS.
     * <p>
     * Gli errori comuni includono: mancanza di credito, nessun segnale,
     * SIM non presente, permessi mancanti.
     *
     * @param errorCode il codice numerico dell'errore (costanti Android SmsManager)
     * @param errorMessage la descrizione testuale dell'errore
     */
    void onSmsError(int errorCode, String errorMessage);

    /**
     * Chiamato quando scade il timeout di attesa per la risposta SMS.
     * <p>
     * Indica che il sistema di allarme non ha risposto entro il tempo
     * massimo previsto (definito in {@link it.bhomealarm.util.Constants#TIMEOUT_SMS_RESPONSE}).
     * Possibili cause: sistema spento, fuori copertura, errore nella ricezione.
     */
    void onSmsTimeout();
}
