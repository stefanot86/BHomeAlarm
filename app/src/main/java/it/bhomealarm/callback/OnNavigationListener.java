package it.bhomealarm.callback;

/**
 * Interfaccia callback per la gestione degli eventi di navigazione.
 * <p>
 * Questa interfaccia definisce i metodi di callback per gestire le richieste
 * di navigazione tra le diverse schermate dell'applicazione BHomeAlarm.
 * <p>
 * Tipicamente implementata dall'Activity principale che contiene il
 * NavController per gestire la navigazione tra i Fragment.
 * <p>
 * Le destinazioni sono identificate tramite gli ID delle risorse
 * definiti nel navigation graph (R.id.xxx).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 */
public interface OnNavigationListener {

    /**
     * Chiamato quando e' richiesta la navigazione verso una specifica schermata.
     * <p>
     * Utilizzare questo metodo per navigazioni generiche verso qualsiasi
     * destinazione definita nel navigation graph.
     *
     * @param destinationId l'ID della risorsa di destinazione (R.id.xxx)
     *                      come definito nel navigation graph
     */
    void onNavigateTo(int destinationId);

    /**
     * Chiamato quando e' richiesta la navigazione alla schermata precedente.
     * <p>
     * Equivale alla pressione del tasto back, rimuove la schermata corrente
     * dallo stack di navigazione e torna alla precedente.
     */
    void onNavigateBack();

    /**
     * Chiamato quando e' richiesta la navigazione alla schermata principale (Home).
     * <p>
     * Tipicamente svuota lo stack di navigazione e torna alla dashboard
     * principale dell'applicazione.
     */
    void onNavigateHome();

    /**
     * Chiamato quando e' richiesta la navigazione alla schermata di dettaglio utente.
     * <p>
     * Apre la schermata che mostra i dettagli e le opzioni di configurazione
     * per un utente specifico del sistema di allarme.
     *
     * @param userId l'identificatore dell'utente da visualizzare,
     *               valore compreso tra 0 (Joker) e 16
     */
    void onNavigateToUserDetail(int userId);

    /**
     * Chiamato quando e' richiesta la navigazione alla schermata di selezione zone.
     * <p>
     * Apre la schermata che permette all'utente di selezionare manualmente
     * le zone da attivare per un'attivazione personalizzata dell'allarme.
     */
    void onNavigateToZoneSelection();
}
