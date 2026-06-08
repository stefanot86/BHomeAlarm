package it.bhomealarm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import it.bhomealarm.R;
import it.bhomealarm.controller.AlarmController;
import it.bhomealarm.util.Constants;
import it.bhomealarm.view.activity.AlarmActionActivity;

/**
 * Widget per la home screen che consente di attivare e disattivare l'allarme
 * senza aprire l'applicazione.
 * <p>
 * Il widget mostra lo stato corrente del sistema (letto da {@link AlarmController})
 * e due bottoni:
 * <ul>
 *     <li><b>Attiva</b>: apre {@link AlarmActionActivity} in modalità
 *         {@link Constants#WIDGET_MODE_ARM} per scegliere lo scenario.</li>
 *     <li><b>Disattiva</b>: apre {@link AlarmActionActivity} in modalità
 *         {@link Constants#WIDGET_MODE_DISARM} per confermare la disattivazione.</li>
 * </ul>
 * <p>
 * Essendo basato su {@link RemoteViews}, il widget non può usare ViewModel/LiveData
 * né aprire dialog: ogni interazione è veicolata da {@link PendingIntent} verso
 * l'Activity. La lettura dello stato avviene tramite il controller, mantenendo la
 * View priva di accesso diretto a {@code SmsService} o alle SharedPreferences.
 * <p>
 * L'aggiornamento dello stato visualizzato viene innescato da
 * {@link it.bhomealarm.service.SmsReceiver} (tramite {@link #updateAllWidgets(Context)})
 * quando arriva una risposta dalla centrale.
 *
 * @see AlarmController
 * @see AlarmActionActivity
 */
public class AlarmWidgetProvider extends AppWidgetProvider {

    /** Offset per il request code del PendingIntent del bottone Attiva. */
    private static final int REQ_ARM = 1;

    /** Offset per il request code del PendingIntent del bottone Disattiva. */
    private static final int REQ_DISARM = 2;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    /**
     * Forza l'aggiornamento di tutte le istanze del widget presenti sulla home screen.
     * Chiamato quando lo stato dell'allarme cambia (es. risposta SMS ricevuta).
     *
     * @param context Contesto dell'applicazione
     */
    public static void updateAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, AlarmWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        if (ids != null && ids.length > 0) {
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
        }
    }

    /**
     * Costruisce e applica le {@link RemoteViews} per una singola istanza del widget:
     * imposta testo/colore dello stato, l'ora dell'ultimo aggiornamento e i
     * {@link PendingIntent} dei due bottoni.
     */
    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        AlarmController controller = AlarmController.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_alarm);

        // Stato corrente
        String status = controller.getLastStatus();
        views.setTextViewText(R.id.widget_status_text, statusLabel(context, status));
        views.setTextColor(R.id.widget_status_text, ContextCompat.getColor(context, statusColor(status)));

        // Ora ultimo aggiornamento
        long lastCheck = controller.getLastCheckTime();
        if (lastCheck > 0) {
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(lastCheck));
            views.setTextViewText(R.id.widget_status_time,
                    context.getString(R.string.last_check_format, time));
        } else {
            views.setTextViewText(R.id.widget_status_time, "");
        }

        // Bottoni: aprono l'Activity translucida nelle due modalità
        views.setOnClickPendingIntent(R.id.widget_btn_arm,
                buildActionIntent(context, appWidgetId, Constants.WIDGET_MODE_ARM, REQ_ARM));
        views.setOnClickPendingIntent(R.id.widget_btn_disarm,
                buildActionIntent(context, appWidgetId, Constants.WIDGET_MODE_DISARM, REQ_DISARM));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Crea il PendingIntent che lancia {@link AlarmActionActivity} nella modalità data.
     * Il request code combina l'id del widget e l'azione per garantire univocità tra
     * istanze multiple del widget.
     */
    private static PendingIntent buildActionIntent(Context context, int appWidgetId, int widgetMode, int reqOffset) {
        Intent intent = new Intent(context, AlarmActionActivity.class);
        intent.putExtra(Constants.WIDGET_EXTRA_MODE, widgetMode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Dato distinto per evitare il riuso dello stesso PendingIntent tra le due azioni
        intent.setData(android.net.Uri.parse("bhomealarm://widget/" + appWidgetId + "/" + widgetMode));

        return PendingIntent.getActivity(
                context,
                appWidgetId * 10 + reqOffset,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /**
     * Mappa il valore di stato grezzo (Constants.STATUS_*) alla stringa localizzata.
     */
    private static CharSequence statusLabel(Context context, String status) {
        if (status == null) {
            return context.getString(R.string.status_unknown);
        }
        switch (status) {
            case Constants.STATUS_ARMED:
                return context.getString(R.string.status_armed);
            case Constants.STATUS_DISARMED:
                return context.getString(R.string.status_disarmed);
            case Constants.STATUS_ALARM:
                return context.getString(R.string.status_alarm);
            case Constants.STATUS_TAMPER:
                return context.getString(R.string.status_tamper);
            default:
                return context.getString(R.string.status_unknown);
        }
    }

    /**
     * Mappa il valore di stato grezzo al colore da usare per il testo.
     */
    private static int statusColor(String status) {
        if (status == null) {
            return R.color.status_unknown;
        }
        switch (status) {
            case Constants.STATUS_ARMED:
                return R.color.status_armed;
            case Constants.STATUS_DISARMED:
                return R.color.status_disarmed;
            case Constants.STATUS_ALARM:
            case Constants.STATUS_TAMPER:
                return R.color.status_alarm;
            default:
                return R.color.status_unknown;
        }
    }
}
