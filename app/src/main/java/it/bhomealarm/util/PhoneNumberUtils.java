package it.bhomealarm.util;

import android.text.TextUtils;

/**
 * Utility per normalizzazione e confronto numeri telefonici.
 */
public final class PhoneNumberUtils {

    private PhoneNumberUtils() {} // No instantiation

    /**
     * Normalizza un numero telefonico rimuovendo spazi, trattini e prefisso italiano.
     *
     * @param phoneNumber Numero da normalizzare
     * @return Numero normalizzato o stringa vuota se null
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
     * Confronta due numeri telefonici dopo normalizzazione.
     * Confronta le ultime 9 cifre per gestire variazioni nei prefissi.
     *
     * @param number1 Primo numero
     * @param number2 Secondo numero
     * @return true se i numeri corrispondono
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
     * Verifica se una stringa è un numero telefonico valido.
     *
     * @param phoneNumber Numero da verificare
     * @return true se valido (minimo 5 caratteri, solo cifre e simboli telefono)
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
     * Formatta un numero per la visualizzazione.
     *
     * @param phoneNumber Numero da formattare
     * @return Numero formattato con prefisso +39 se italiano
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
     * Maschera un numero telefonico per log sicuri.
     * Es: +393331234567 → +39***4567
     *
     * @param phoneNumber Numero da mascherare
     * @return Numero mascherato
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
