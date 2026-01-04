package it.bhomealarm.callback;

/**
 * Callback per azioni utente sull'allarme.
 */
public interface OnAlarmActionListener {

    /**
     * Richiesta attivazione allarme con scenario.
     *
     * @param scenarioId ID scenario (1-16)
     */
    void onArmRequested(int scenarioId);

    /**
     * Richiesta attivazione allarme con zone personalizzate.
     *
     * @param zoneMask Maschera zone (8 caratteri 0/1)
     */
    void onArmCustomRequested(String zoneMask);

    /**
     * Richiesta disattivazione allarme.
     */
    void onDisarmRequested();

    /**
     * Richiesta verifica stato sistema.
     */
    void onStatusRequested();

    /**
     * Richiesta apertura impostazioni.
     */
    void onSettingsRequested();

    /**
     * Richiesta selezione SIM prima dell'invio.
     */
    void onSimSelectionRequested();
}
