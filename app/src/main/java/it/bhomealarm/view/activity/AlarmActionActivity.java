package it.bhomealarm.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.bhomealarm.R;
import it.bhomealarm.controller.AlarmController;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * Activity translucida lanciata dal widget home screen per attivare o disattivare
 * l'allarme senza aprire l'intera applicazione.
 * <p>
 * Funge da "mini-schermata" che appare sopra la home screen e mostra:
 * <ul>
 *     <li><b>{@link #MODE_ARM}</b>: la lista degli scenari abilitati; al tocco di
 *         uno scenario invia il comando di attivazione e si chiude.</li>
 *     <li><b>{@link #MODE_DISARM}</b>: un dialog di conferma; alla conferma invia
 *         il comando di disattivazione e si chiude.</li>
 * </ul>
 * <p>
 * Essendo una View, delega tutta la logica di invio comandi e di lettura dello stato
 * a {@link AlarmController}: non chiama mai direttamente {@code SmsService} né le
 * SharedPreferences. Gestisce inoltre i casi limite di numero allarme non configurato
 * e di permesso {@link Manifest.permission#SEND_SMS} non concesso (richiedendolo, dato
 * che a differenza del widget questa è una vera Activity).
 *
 * @see AlarmController
 * @see it.bhomealarm.widget.AlarmWidgetProvider
 * @see Constants#WIDGET_MODE_ARM
 * @see Constants#WIDGET_MODE_DISARM
 */
public class AlarmActionActivity extends AppCompatActivity {

    /** Controller per i comandi dell'allarme (fonte unica). */
    private AlarmController controller;

    /** Modalità corrente, letta dall'Intent. */
    private int mode = Constants.WIDGET_MODE_ARM;

    /** Evita di ricostruire il dialog scenari ad ogni emissione del LiveData. */
    private boolean dialogShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getIntent().getIntExtra(Constants.WIDGET_EXTRA_MODE, Constants.WIDGET_MODE_ARM);
        controller = AlarmController.getInstance(this);

        // Numero allarme non configurato: avvisa e apri l'app per la configurazione.
        if (!controller.isPhoneConfigured()) {
            Toast.makeText(this, R.string.error_no_phone, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            return;
        }

        // Permesso SMS: se manca, richiedilo (siamo una Activity, quindi possiamo).
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, Constants.REQUEST_SMS_PERMISSION);
            return;
        }

        proceed();
    }

    /**
     * Avvia il flusso corrispondente alla modalità richiesta.
     */
    private void proceed() {
        if (mode == Constants.WIDGET_MODE_DISARM) {
            showDisarmDialog();
        } else {
            showArmDialog();
        }
    }

    // ========== MODE_ARM ==========

    /**
     * Osserva gli scenari dal repository e mostra il dialog di selezione con i soli
     * scenari abilitati. Al tocco di uno scenario invia il comando e chiude l'Activity.
     */
    private void showArmDialog() {
        AlarmRepository.getInstance(getApplication()).getAllScenarios().observe(this, scenarios -> {
            if (dialogShown) {
                return;
            }
            dialogShown = true;

            final List<Scenario> enabled = new ArrayList<>();
            if (scenarios != null) {
                for (Scenario s : scenarios) {
                    if (s.isEnabled()) {
                        enabled.add(s);
                    }
                }
            }

            if (enabled.isEmpty()) {
                Toast.makeText(this, R.string.users_empty, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            CharSequence[] names = new CharSequence[enabled.size()];
            for (int i = 0; i < enabled.size(); i++) {
                names[i] = enabled.get(i).getName();
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dialog_arm_title)
                    .setItems(names, (dialog, which) -> sendArm(enabled.get(which)))
                    .setNegativeButton(R.string.action_cancel, (dialog, which) -> finish())
                    .setOnCancelListener(dialog -> finish())
                    .show();
        });
    }

    /**
     * Invia il comando di attivazione per lo scenario scelto.
     * Gli scenari personalizzati (slot &gt; 100) usano il comando a zone, gli altri
     * il comando a scenario predefinito.
     *
     * @param scenario Lo scenario selezionato dall'utente
     */
    private void sendArm(Scenario scenario) {
        String messageId;
        if (scenario.isCustom()) {
            StringBuilder zones = new StringBuilder();
            for (Integer zone : scenario.getIncludedZones()) {
                zones.append(zone);
            }
            messageId = controller.armWithCustomZones(zones.toString());
        } else {
            messageId = controller.armWithScenario(scenario.getSlot());
        }
        toastResult(messageId);
        finish();
    }

    // ========== MODE_DISARM ==========

    /**
     * Mostra il dialog di conferma disattivazione; alla conferma invia il comando.
     */
    private void showDisarmDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_disarm_title)
                .setMessage(R.string.dialog_disarm_message)
                .setPositiveButton(R.string.dialog_send, (dialog, which) -> {
                    toastResult(controller.disarm());
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    // ========== Helpers ==========

    /**
     * Mostra un toast in base all'esito dell'invio (messageId non null = inviato).
     *
     * @param messageId messageId restituito dal controller, o null in caso di errore
     */
    private void toastResult(String messageId) {
        int msg = (messageId != null) ? R.string.widget_sms_sent : R.string.error_sms_send;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceed();
            } else {
                Toast.makeText(this, R.string.error_no_permission, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
