package it.bhomealarm.callback;

/**
 * Interfaccia callback per il monitoraggio del progresso della configurazione.
 * <p>
 * Questa interfaccia definisce i metodi di callback per tracciare il processo
 * di configurazione iniziale del sistema, che consiste nel recupero delle
 * informazioni CONF1-CONF5 dal sistema di allarme BHome.
 * <p>
 * Il processo di configurazione si articola in 5 step:
 * <ol>
 *     <li>CONF1: Versione firmware, flags utente, zone</li>
 *     <li>CONF2: Scenari 1-8</li>
 *     <li>CONF3: Scenari 9-16</li>
 *     <li>CONF4: Utenti 1-8 e utente Joker</li>
 *     <li>CONF5: Utenti 9-16</li>
 * </ol>
 * <p>
 * Implementare questa interfaccia per aggiornare l'interfaccia utente
 * durante il processo di configurazione (progress bar, messaggi di stato, etc.).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see it.bhomealarm.util.Constants#CONFIG_TOTAL_STEPS
 */
public interface OnConfigProgressListener {

    /**
     * Chiamato quando inizia il processo di configurazione.
     * <p>
     * Utilizzare questo callback per preparare l'interfaccia utente,
     * ad esempio mostrando un indicatore di caricamento.
     */
    void onConfigStarted();

    /**
     * Chiamato ad ogni aggiornamento del progresso della configurazione.
     * <p>
     * Questo callback puo' essere invocato piu' volte durante uno stesso step,
     * ad esempio durante l'attesa della risposta SMS o durante il parsing.
     *
     * @param currentStep lo step corrente (valore da 1 a 5)
     * @param totalSteps il numero totale di step (sempre 5)
     * @param message un messaggio descrittivo dello stato corrente,
     *                ad esempio "Invio CONF1..." o "Elaborazione risposta..."
     */
    void onProgressUpdate(int currentStep, int totalSteps, String message);

    /**
     * Chiamato quando uno step di configurazione e' stato completato con successo.
     * <p>
     * Indica che i dati dello step specificato sono stati ricevuti e salvati
     * correttamente nel database locale.
     *
     * @param step il numero dello step completato (valore da 1 a 5)
     */
    void onStepCompleted(int step);

    /**
     * Chiamato quando l'intero processo di configurazione e' completato con successo.
     * <p>
     * A questo punto tutti i dati del sistema di allarme (zone, scenari, utenti)
     * sono stati salvati nel database locale e l'applicazione e' pronta all'uso.
     */
    void onConfigComplete();

    /**
     * Chiamato in caso di errore durante il processo di configurazione.
     * <p>
     * L'errore puo' verificarsi in qualsiasi step, ad esempio per timeout
     * nella risposta SMS, errore di parsing o errore dal sistema di allarme.
     *
     * @param step lo step in cui si e' verificato l'errore (valore da 1 a 5)
     * @param errorCode il codice errore (vedi {@link it.bhomealarm.util.Constants}
     *                  per i codici definiti)
     * @param errorMessage la descrizione testuale dell'errore
     */
    void onConfigError(int step, String errorCode, String errorMessage);

    /**
     * Chiamato quando il processo di configurazione viene annullato dall'utente.
     * <p>
     * Puo' verificarsi quando l'utente preme il pulsante indietro o annulla
     * esplicitamente l'operazione durante il processo di configurazione.
     */
    void onConfigCancelled();
}
