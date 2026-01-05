package it.bhomealarm.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe helper per la gestione dei permessi runtime di Android.
 * <p>
 * Questa classe fornisce metodi statici per verificare, richiedere e gestire
 * i permessi necessari al funzionamento dell'applicazione BHomeAlarm.
 * <p>
 * I permessi gestiti includono:
 * <ul>
 *     <li>{@link Manifest.permission#SEND_SMS} - Invio SMS al sistema di allarme</li>
 *     <li>{@link Manifest.permission#RECEIVE_SMS} - Ricezione SMS dal sistema di allarme</li>
 *     <li>{@link Manifest.permission#READ_SMS} - Lettura SMS ricevuti</li>
 *     <li>{@link Manifest.permission#READ_PHONE_STATE} - Stato telefono per gestione SIM</li>
 *     <li>{@link Manifest.permission#READ_CONTACTS} - Lettura rubrica per selezione numeri</li>
 *     <li>{@link Manifest.permission#POST_NOTIFICATIONS} - Notifiche (Android 13+)</li>
 * </ul>
 * <p>
 * La classe gestisce automaticamente le differenze tra versioni Android,
 * includendo il permesso POST_NOTIFICATIONS solo su Android 13 (Tiramisu) e successivi.
 * <p>
 * La classe e' dichiarata final e ha un costruttore privato per impedirne
 * l'istanziazione (pattern utility class).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see Manifest.permission
 */
public final class PermissionHelper {

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     */
    private PermissionHelper() {} // No instantiation

    /**
     * Array contenente tutti i permessi richiesti dall'applicazione.
     * <p>
     * L'array viene inizializzato staticamente e include il permesso
     * POST_NOTIFICATIONS solo su Android 13 (API 33) e versioni successive.
     */
    public static final String[] REQUIRED_PERMISSIONS;

    static {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.RECEIVE_SMS);
        permissions.add(Manifest.permission.READ_SMS);
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.READ_CONTACTS);

        // POST_NOTIFICATIONS richiesto da Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        REQUIRED_PERMISSIONS = permissions.toArray(new String[0]);
    }

    /**
     * Verifica se tutti i permessi richiesti dall'applicazione sono stati concessi.
     *
     * @param context il Context dell'applicazione o dell'Activity
     * @return {@code true} se tutti i permessi in {@link #REQUIRED_PERMISSIONS}
     *         sono stati concessi, {@code false} se almeno uno e' mancante
     */
    public static boolean hasAllPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se i permessi necessari per l'invio e la ricezione SMS sono concessi.
     * <p>
     * Controlla specificamente SEND_SMS e RECEIVE_SMS, che sono essenziali
     * per la comunicazione con il sistema di allarme.
     *
     * @param context il Context dell'applicazione o dell'Activity
     * @return {@code true} se entrambi i permessi SMS sono concessi,
     *         {@code false} altrimenti
     */
    public static boolean hasSmsPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se il permesso di lettura della rubrica contatti e' concesso.
     * <p>
     * Questo permesso e' utilizzato per permettere all'utente di selezionare
     * il numero del sistema di allarme dalla rubrica.
     *
     * @param context il Context dell'applicazione o dell'Activity
     * @return {@code true} se il permesso READ_CONTACTS e' concesso,
     *         {@code false} altrimenti
     */
    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se il permesso per le notifiche e' concesso.
     * <p>
     * Su versioni di Android precedenti alla 13 (Tiramisu), il permesso
     * notifiche non e' richiesto a runtime, quindi il metodo restituisce
     * sempre {@code true}.
     * <p>
     * Su Android 13 e successivi, verifica il permesso POST_NOTIFICATIONS.
     *
     * @param context il Context dell'applicazione o dell'Activity
     * @return {@code true} se il permesso notifiche e' concesso o non richiesto
     *         (pre-Android 13), {@code false} se negato su Android 13+
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Restituisce l'elenco dei permessi non ancora concessi.
     * <p>
     * Utile per determinare quali permessi devono ancora essere richiesti
     * all'utente o per mostrare informazioni sui permessi mancanti.
     *
     * @param context il Context dell'applicazione o dell'Activity
     * @return un array di stringhe contenente i nomi dei permessi mancanti;
     *         array vuoto se tutti i permessi sono concessi
     */
    public static String[] getMissingPermissions(Context context) {
        List<String> missing = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        return missing.toArray(new String[0]);
    }

    /**
     * Verifica se e' necessario mostrare una spiegazione all'utente
     * per un determinato permesso.
     * <p>
     * Restituisce {@code true} se l'utente ha precedentemente negato
     * il permesso senza selezionare "Non chiedere piu'". In questo caso
     * e' consigliato mostrare una UI che spieghi perche' il permesso
     * e' necessario prima di richiederlo nuovamente.
     *
     * @param activity l'Activity da cui effettuare la richiesta
     * @param permission il nome del permesso da verificare
     * @return {@code true} se l'utente ha negato il permesso in precedenza
     *         e dovrebbe essere mostrata una spiegazione, {@code false} altrimenti
     * @see ActivityCompat#shouldShowRequestPermissionRationale(Activity, String)
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Verifica se e' necessario mostrare una spiegazione per almeno
     * uno dei permessi mancanti.
     * <p>
     * Utile per determinare se mostrare un dialog esplicativo generale
     * prima di richiedere i permessi.
     *
     * @param activity l'Activity da cui effettuare la verifica
     * @return {@code true} se almeno un permesso mancante richiede
     *         una spiegazione, {@code false} se nessuno la richiede
     */
    public static boolean shouldShowAnyRationale(Activity activity) {
        for (String permission : getMissingPermissions(activity)) {
            if (shouldShowRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Richiede all'utente i permessi mancanti tramite il dialog di sistema.
     * <p>
     * Se non ci sono permessi mancanti, il metodo non esegue alcuna azione.
     * Il risultato della richiesta verra' ricevuto in
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}.
     *
     * @param activity l'Activity da cui effettuare la richiesta
     * @param requestCode il codice richiesta per identificare il risultato
     *                    nel callback onRequestPermissionsResult
     */
    public static void requestMissingPermissions(Activity activity, int requestCode) {
        String[] missing = getMissingPermissions(activity);
        if (missing.length > 0) {
            ActivityCompat.requestPermissions(activity, missing, requestCode);
        }
    }

    /**
     * Verifica se tutti i permessi in una richiesta sono stati concessi.
     * <p>
     * Da utilizzare nel callback {@link Activity#onRequestPermissionsResult}
     * per verificare l'esito della richiesta permessi.
     *
     * @param grantResults array dei risultati restituito da onRequestPermissionsResult
     * @return {@code true} se tutti i permessi nell'array sono stati concessi
     *         ({@link PackageManager#PERMISSION_GRANTED}), {@code false} se
     *         l'array e' vuoto o almeno un permesso e' stato negato
     */
    public static boolean allPermissionsGranted(int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
