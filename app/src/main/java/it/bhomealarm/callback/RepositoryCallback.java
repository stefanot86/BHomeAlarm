package it.bhomealarm.callback;

/**
 * Callback generico per operazioni asincrone del Repository.
 *
 * @param <T> Tipo del risultato
 */
public interface RepositoryCallback<T> {

    /**
     * Chiamato quando l'operazione ha successo.
     *
     * @param result Risultato dell'operazione
     */
    void onSuccess(T result);

    /**
     * Chiamato in caso di errore.
     *
     * @param error Eccezione verificatasi
     */
    void onError(Exception error);
}
