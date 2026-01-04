package it.bhomealarm.callback;

/**
 * Callback per progresso configurazione CONF1-5.
 */
public interface OnConfigProgressListener {

    /**
     * Chiamato quando inizia la configurazione.
     */
    void onConfigStarted();

    /**
     * Chiamato ad ogni aggiornamento del progresso.
     *
     * @param currentStep Step corrente (1-5)
     * @param totalSteps Totale step (5)
     * @param message Messaggio descrittivo
     */
    void onProgressUpdate(int currentStep, int totalSteps, String message);

    /**
     * Chiamato quando uno step è completato.
     *
     * @param step Step completato (1-5)
     */
    void onStepCompleted(int step);

    /**
     * Chiamato quando la configurazione è completata con successo.
     */
    void onConfigComplete();

    /**
     * Chiamato in caso di errore durante la configurazione.
     *
     * @param step Step in cui si è verificato l'errore
     * @param errorCode Codice errore
     * @param errorMessage Messaggio errore
     */
    void onConfigError(int step, String errorCode, String errorMessage);

    /**
     * Chiamato quando la configurazione viene annullata.
     */
    void onConfigCancelled();
}
