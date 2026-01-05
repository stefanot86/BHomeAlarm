package it.bhomealarm.callback;

/**
 * Interfaccia callback per le azioni utente sul sistema di allarme.
 * <p>
 * Questa interfaccia definisce i metodi di callback per gestire le richieste
 * dell'utente relative al controllo del sistema di allarme BHome:
 * <ul>
 *     <li>Attivazione con scenario predefinito</li>
 *     <li>Attivazione con zone personalizzate</li>
 *     <li>Disattivazione</li>
 *     <li>Verifica stato</li>
 *     <li>Accesso alle impostazioni</li>
 *     <li>Selezione SIM</li>
 * </ul>
 * <p>
 * Tipicamente implementata da Activity o ViewModel che gestiscono
 * la logica di business per l'invio dei comandi SMS al sistema di allarme.
 *
 * @author BHomeAlarm Team
 * @version 1.0
 */
public interface OnAlarmActionListener {

    /**
     * Chiamato quando l'utente richiede l'attivazione dell'allarme
     * utilizzando uno scenario predefinito.
     * <p>
     * Lo scenario determina quali zone saranno attive. Gli scenari
     * sono configurati nel sistema di allarme e recuperati tramite CONF2/CONF3.
     *
     * @param scenarioId l'identificatore dello scenario da attivare,
     *                   valore compreso tra 1 e 16
     * @see it.bhomealarm.util.Constants#CMD_ARM_SCENARIO
     */
    void onArmRequested(int scenarioId);

    /**
     * Chiamato quando l'utente richiede l'attivazione dell'allarme
     * con una selezione personalizzata delle zone.
     * <p>
     * Permette all'utente di selezionare manualmente quali zone attivare,
     * indipendentemente dagli scenari predefiniti.
     *
     * @param zoneMask la maschera delle zone da attivare, rappresentata come
     *                 stringa di 8 caratteri '0' o '1', dove '1' indica zona attiva.
     *                 Esempio: "11001100" attiva le zone 1, 2, 5 e 6.
     * @see it.bhomealarm.util.Constants#CMD_ARM_CUSTOM
     */
    void onArmCustomRequested(String zoneMask);

    /**
     * Chiamato quando l'utente richiede la disattivazione dell'allarme.
     * <p>
     * Disattiva completamente il sistema di allarme, rendendo inattive
     * tutte le zone di rilevamento.
     *
     * @see it.bhomealarm.util.Constants#CMD_DISARM
     */
    void onDisarmRequested();

    /**
     * Chiamato quando l'utente richiede la verifica dello stato del sistema.
     * <p>
     * Invia una richiesta al sistema di allarme per ottenere lo stato
     * corrente (armato/disarmato, scenario attivo, zone attive, etc.).
     *
     * @see it.bhomealarm.util.Constants#CMD_STATUS
     */
    void onStatusRequested();

    /**
     * Chiamato quando l'utente richiede l'apertura delle impostazioni.
     * <p>
     * Tipicamente apre una schermata per modificare le configurazioni
     * dell'applicazione come il numero del sistema di allarme o la SIM da usare.
     */
    void onSettingsRequested();

    /**
     * Chiamato quando l'utente richiede la selezione della SIM da utilizzare.
     * <p>
     * Su dispositivi dual-SIM, permette all'utente di scegliere quale SIM
     * utilizzare per l'invio degli SMS al sistema di allarme.
     * Questo callback viene tipicamente invocato prima dell'invio di un comando.
     */
    void onSimSelectionRequested();
}
