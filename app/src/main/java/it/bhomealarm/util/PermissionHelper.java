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
 * Helper per gestione permessi runtime Android.
 */
public final class PermissionHelper {

    private PermissionHelper() {} // No instantiation

    // Permessi richiesti dall'app
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
     * Verifica se tutti i permessi richiesti sono stati concessi.
     *
     * @param context Context
     * @return true se tutti i permessi sono concessi
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
     * Verifica se i permessi SMS sono concessi.
     *
     * @param context Context
     * @return true se i permessi SMS sono concessi
     */
    public static boolean hasSmsPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se il permesso di lettura contatti è concesso.
     *
     * @param context Context
     * @return true se il permesso è concesso
     */
    public static boolean hasContactsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Verifica se il permesso notifiche è concesso (sempre true pre-Android 13).
     *
     * @param context Context
     * @return true se il permesso è concesso
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Restituisce la lista dei permessi mancanti.
     *
     * @param context Context
     * @return Array di permessi non ancora concessi
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
     * Verifica se è necessario mostrare spiegazione per un permesso.
     *
     * @param activity Activity
     * @param permission Permesso da verificare
     * @return true se l'utente ha precedentemente negato il permesso
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Verifica se è necessario mostrare spiegazione per almeno un permesso.
     *
     * @param activity Activity
     * @return true se è necessario mostrare spiegazione
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
     * Richiede i permessi mancanti.
     *
     * @param activity Activity
     * @param requestCode Codice richiesta
     */
    public static void requestMissingPermissions(Activity activity, int requestCode) {
        String[] missing = getMissingPermissions(activity);
        if (missing.length > 0) {
            ActivityCompat.requestPermissions(activity, missing, requestCode);
        }
    }

    /**
     * Verifica il risultato della richiesta permessi.
     *
     * @param grantResults Array risultati
     * @return true se tutti i permessi sono stati concessi
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
