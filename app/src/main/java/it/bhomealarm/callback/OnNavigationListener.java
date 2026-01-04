package it.bhomealarm.callback;

/**
 * Callback per eventi di navigazione.
 */
public interface OnNavigationListener {

    /**
     * Richiesta navigazione a schermata.
     *
     * @param destinationId ID destinazione (R.id.xxx)
     */
    void onNavigateTo(int destinationId);

    /**
     * Richiesta navigazione indietro.
     */
    void onNavigateBack();

    /**
     * Richiesta navigazione a home.
     */
    void onNavigateHome();

    /**
     * Richiesta navigazione a dettaglio utente.
     *
     * @param userId ID utente
     */
    void onNavigateToUserDetail(int userId);

    /**
     * Richiesta navigazione a selezione zone.
     */
    void onNavigateToZoneSelection();
}
