package it.bhomealarm.util;

/**
 * Costanti globali dell'applicazione.
 */
public final class Constants {

    private Constants() {} // No instantiation

    // ========== SMS Commands ==========
    public static final String CMD_CONF1 = "CONF1?";
    public static final String CMD_CONF2 = "CONF2?";
    public static final String CMD_CONF3 = "CONF3?";
    public static final String CMD_CONF4 = "CONF4?";
    public static final String CMD_CONF5 = "CONF5?";
    public static final String CMD_ARM_SCENARIO = "SCE:%02d";
    public static final String CMD_ARM_CUSTOM = "CUST:%s";
    public static final String CMD_DISARM = "SYS OFF";
    public static final String CMD_STATUS = "SYS?";
    public static final String CMD_SET_USER = "SET:U%02d%s";

    // ========== SMS Response Prefixes ==========
    public static final String RESP_CONF1 = "CONF1:";
    public static final String RESP_CONF2 = "CONF2:";
    public static final String RESP_CONF3 = "CONF3:";
    public static final String RESP_CONF4 = "CONF4:";
    public static final String RESP_CONF5 = "CONF5:";
    public static final String RESP_OK = "OK:";
    public static final String RESP_STATUS = "STATUS:";
    public static final String RESP_ERROR = "ERR:";

    // ========== SMS Separators ==========
    public static final char SEP_COMMAND = ':';
    public static final char SEP_FIELD = '&';
    public static final char SEP_END = '#';
    public static final char SEP_ASSIGN = '=';
    public static final char SEP_FLAGS = '.';

    // ========== Alarm Status ==========
    public static final String STATUS_ARMED = "ARMED";
    public static final String STATUS_DISARMED = "DISARMED";
    public static final String STATUS_ALARM = "ALARM";
    public static final String STATUS_TAMPER = "TAMPER";
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    // ========== Zone Constants ==========
    public static final int ZONE_COUNT = 8;
    public static final String ZONE_NOT_ENABLED = "NE";

    // ========== Scenario Constants ==========
    public static final int SCENARIO_COUNT = 16;
    public static final String SCENARIO_NOT_ENABLED = "NE";
    public static final String SCENARIO_CUSTOM = "CUSTOM";

    // ========== User Constants ==========
    public static final int USER_COUNT = 16;
    public static final String USER_NOT_ENABLED = "NE";
    public static final String USER_JOKER_PREFIX = "RJO";

    // ========== User Permission Bits ==========
    public static final int PERM_RX1 = 0b1000;
    public static final int PERM_RX2 = 0b0100;
    public static final int PERM_VERIFY = 0b0010;
    public static final int PERM_CMD_ON_OFF = 0b0001;
    public static final int PERM_ALL = 0b1111;
    public static final int PERM_NONE = 0b0000;

    // ========== Timeouts (milliseconds) ==========
    public static final int TIMEOUT_SMS_SEND = 10_000;
    public static final int TIMEOUT_SMS_RESPONSE = 60_000;
    public static final int TIMEOUT_PARSING = 5_000;
    public static final int RETRY_DELAY = 5_000;
    public static final int MAX_RETRIES = 2;

    // ========== Configuration States ==========
    public static final int CONFIG_STATE_IDLE = 0;
    public static final int CONFIG_STATE_CONF1 = 1;
    public static final int CONFIG_STATE_CONF2 = 2;
    public static final int CONFIG_STATE_CONF3 = 3;
    public static final int CONFIG_STATE_CONF4 = 4;
    public static final int CONFIG_STATE_CONF5 = 5;
    public static final int CONFIG_STATE_COMPLETE = 6;
    public static final int CONFIG_STATE_ERROR = -1;
    public static final int CONFIG_TOTAL_STEPS = 5;

    // ========== Shared Preferences Keys ==========
    public static final String PREF_NAME = "bhomealarm_prefs";
    public static final String PREF_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    public static final String PREF_ALARM_PHONE = "alarm_phone_number";
    public static final String PREF_SELECTED_SIM = "selected_sim_slot";
    public static final String PREF_LAST_STATUS = "last_status";
    public static final String PREF_LAST_CHECK_TIME = "last_check_time";
    public static final String PREF_CONFIGURED = "system_configured";

    // ========== Error Codes ==========
    public static final String ERROR_UNKNOWN_CMD = "E01";
    public static final String ERROR_INVALID_PARAM = "E02";
    public static final String ERROR_UNAUTHORIZED = "E03";
    public static final String ERROR_SYSTEM_BUSY = "E04";
    public static final String ERROR_INTERNAL = "E05";
    public static final String ERROR_TIMEOUT = "TIMEOUT";

    // ========== Intent Actions ==========
    public static final String ACTION_SMS_SENT = "it.bhomealarm.SMS_SENT";
    public static final String ACTION_SMS_DELIVERED = "it.bhomealarm.SMS_DELIVERED";
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    // ========== Notification IDs ==========
    public static final int NOTIFICATION_ID_ALARM = 1001;
    public static final int NOTIFICATION_ID_SMS = 1002;
    public static final int NOTIFICATION_ID_CONFIG = 1003;

    // ========== Request Codes ==========
    public static final int REQUEST_SMS_PERMISSION = 100;
    public static final int REQUEST_PHONE_PERMISSION = 101;
    public static final int REQUEST_CONTACT_PICKER = 102;
}
