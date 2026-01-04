package it.bhomealarm.callback;

/**
 * Callback per risultati operazioni SMS.
 */
public interface OnSmsResultListener {

    /**
     * Chiamato quando l'SMS è stato inviato con successo.
     *
     * @param messageId ID del messaggio
     */
    void onSmsSent(String messageId);

    /**
     * Chiamato quando l'SMS è stato consegnato.
     *
     * @param messageId ID del messaggio
     */
    void onSmsDelivered(String messageId);

    /**
     * Chiamato quando si riceve una risposta SMS dall'allarme.
     *
     * @param sender Numero mittente
     * @param body Contenuto del messaggio
     */
    void onSmsReceived(String sender, String body);

    /**
     * Chiamato in caso di errore nell'invio SMS.
     *
     * @param errorCode Codice errore
     * @param errorMessage Messaggio errore
     */
    void onSmsError(int errorCode, String errorMessage);

    /**
     * Chiamato quando scade il timeout di attesa risposta.
     */
    void onSmsTimeout();
}
