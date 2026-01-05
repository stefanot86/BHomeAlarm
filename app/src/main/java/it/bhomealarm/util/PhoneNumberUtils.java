package it.bhomealarm.util;

import android.text.TextUtils;

/**
 * Classe di utilita per la gestione dei numeri telefonici.
 * <p>
 * Fornisce metodi statici per:
 * <ul>
 *     <li>Normalizzazione dei numeri telefonici (rimozione spazi, trattini, prefissi)</li>
 *     <li>Confronto tra numeri telefonici con tolleranza per variazioni di formato</li>
 *     <li>Validazione del formato dei numeri telefonici</li>
 *     <li>Formattazione per la visualizzazione</li>
 *     <li>Mascheramento per log sicuri (privacy)</li>
 * </ul>
 * <p>
 * La classe gestisce specificamente i numeri italiani, riconoscendo e gestendo
 * i prefissi internazionali +39 e 0039.
 * <p>
 * La classe e' dichiarata final e ha un costruttore privato per impedirne
 * l'istanziazione (pattern utility class).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 */
public final class PhoneNumberUtils {

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     */
    private PhoneNumberUtils() {} // No instantiation

    /**
     * Normalizza un numero telefonico rimuovendo caratteri di formattazione
     * e il prefisso internazionale italiano.
     * <p>
     * Operazioni eseguite:
     * <ol>
     *     <li>Rimozione di spazi, trattini e parentesi</li>
     *     <li>Rimozione del prefisso +39 se presente</li>
     *     <li>Rimozione del prefisso 0039 se presente</li>
     * </ol>
     *
     * @param phoneNumber il numero telefonico da normalizzare, puo' essere null
     * @return il numero normalizzato senza formattazione e prefisso internazionale,
     *         oppure stringa vuota se il parametro e' null o vuoto
     */
    public static String normalize(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }

        // Rimuovi spazi, trattini, parentesi
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Rimuovi prefisso internazionale italiano
        if (cleaned.startsWith("+39")) {
            cleaned = cleaned.substring(3);
        } else if (cleaned.startsWith("0039")) {
            cleaned = cleaned.substring(4);
        }

        return cleaned;
    }

    /**
     * Confronta due numeri telefonici dopo averli normalizzati.
     * <p>
     * Il confronto viene effettuato sulle ultime 9 cifre per gestire
     * variazioni nei prefissi (internazionali, nazionali). Questo approccio
     * garantisce che numeri come "+393331234567" e "3331234567" siano
     * riconosciuti come equivalenti.
     * <p>
     * Se uno dei numeri ha meno di 9 cifre, viene effettuato un confronto
     * esatto dopo la normalizzazione.
     *
     * @param number1 il primo numero telefonico da confrontare
     * @param number2 il secondo numero telefonico da confrontare
     * @return {@code true} se i numeri corrispondono (stesse ultime 9 cifre
     *         o uguali dopo normalizzazione), {@code false} altrimenti
     *         o se uno dei parametri e' null/vuoto
     */
    public static boolean matches(String number1, String number2) {
        String n1 = normalize(number1);
        String n2 = normalize(number2);

        if (TextUtils.isEmpty(n1) || TextUtils.isEmpty(n2)) {
            return false;
        }

        // Confronta le ultime 9 cifre (per gestire variazioni)
        if (n1.length() >= 9 && n2.length() >= 9) {
            return n1.substring(n1.length() - 9)
                    .equals(n2.substring(n2.length() - 9));
        }

        return n1.equals(n2);
    }

    /**
     * Verifica se una stringa rappresenta un numero telefonico valido.
     * <p>
     * Un numero e' considerato valido se:
     * <ul>
     *     <li>Ha almeno 5 caratteri (dopo la rimozione di spazi e trattini)</li>
     *     <li>Contiene solo cifre, con un eventuale '+' iniziale</li>
     * </ul>
     *
     * @param phoneNumber il numero telefonico da verificare
     * @return {@code true} se il numero e' valido secondo i criteri sopra,
     *         {@code false} se null, vuoto, troppo corto o contiene caratteri non validi
     */
    public static boolean isValid(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Minimo 5 caratteri
        if (cleaned.length() < 5) {
            return false;
        }

        // Solo cifre, + iniziale opzionale
        return cleaned.matches("\\+?\\d+");
    }

    /**
     * Formatta un numero telefonico per la visualizzazione all'utente.
     * <p>
     * Se il numero normalizzato ha 10 cifre e inizia con '3' (cellulare)
     * o '0' (fisso), viene aggiunto il prefisso internazionale italiano
     * "+39 " per una visualizzazione standard.
     *
     * @param phoneNumber il numero telefonico da formattare
     * @return il numero formattato con prefisso "+39 " se italiano,
     *         oppure il numero normalizzato, oppure stringa vuota se null
     */
    public static String format(String phoneNumber) {
        String normalized = normalize(phoneNumber);

        if (TextUtils.isEmpty(normalized)) {
            return "";
        }

        // Aggiungi prefisso italiano se non presente e il numero sembra italiano
        if (normalized.length() == 10 && (normalized.startsWith("3") || normalized.startsWith("0"))) {
            return "+39 " + normalized;
        }

        return normalized;
    }

    /**
     * Maschera un numero telefonico per utilizzo in log e visualizzazioni
     * dove e' richiesta la protezione della privacy.
     * <p>
     * Il numero viene mascherato mostrando solo le ultime 4 cifre,
     * precedute da "***".
     * <p>
     * Esempi:
     * <ul>
     *     <li>"+393331234567" diventa "***4567"</li>
     *     <li>"3331234567" diventa "***4567"</li>
     *     <li>"1234" diventa "***" (numero troppo corto)</li>
     * </ul>
     *
     * @param phoneNumber il numero telefonico da mascherare
     * @return il numero mascherato nel formato "***XXXX" dove XXXX sono
     *         le ultime 4 cifre, oppure "***" se il numero ha 4 o meno cifre,
     *         oppure stringa vuota se null
     */
    public static String mask(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }

        String normalized = normalize(phoneNumber);

        if (normalized.length() <= 4) {
            return "***";
        }

        return "***" + normalized.substring(normalized.length() - 4);
    }
}
