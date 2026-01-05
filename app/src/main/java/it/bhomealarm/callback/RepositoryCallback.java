package it.bhomealarm.callback;

/**
 * Interfaccia callback generica per le operazioni asincrone del Repository.
 * <p>
 * Questa interfaccia definisce un pattern callback type-safe per gestire
 * i risultati delle operazioni asincrone eseguite dai repository
 * dell'applicazione (es. operazioni su database, chiamate di rete).
 * <p>
 * Utilizzo tipico:
 * <pre>{@code
 * repository.loadData(new RepositoryCallback<List<Zone>>() {
 *     @Override
 *     public void onSuccess(List<Zone> result) {
 *         // Gestisci i dati ricevuti
 *     }
 *
 *     @Override
 *     public void onError(Exception error) {
 *         // Gestisci l'errore
 *     }
 * });
 * }</pre>
 *
 * @param <T> il tipo del risultato atteso dall'operazione asincrona
 * @author BHomeAlarm Team
 * @version 1.0
 */
public interface RepositoryCallback<T> {

    /**
     * Chiamato quando l'operazione asincrona e' completata con successo.
     * <p>
     * Il risultato puo' essere null se l'operazione non restituisce dati
     * (es. operazioni di scrittura) o se il dato richiesto non esiste.
     *
     * @param result il risultato dell'operazione, di tipo T;
     *               puo' essere null a seconda dell'operazione eseguita
     */
    void onSuccess(T result);

    /**
     * Chiamato quando l'operazione asincrona fallisce con un errore.
     * <p>
     * L'eccezione fornisce dettagli sulla causa del fallimento,
     * che puo' includere errori di database, errori di rete,
     * errori di parsing o altre eccezioni runtime.
     *
     * @param error l'eccezione che descrive l'errore verificatosi
     */
    void onError(Exception error);
}
