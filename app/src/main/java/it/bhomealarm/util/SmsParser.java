package it.bhomealarm.util;

import java.util.ArrayList;
import java.util.List;

import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;

/**
 * Parser per risposte SMS dal sistema allarme.
 */
public final class SmsParser {

    private SmsParser() {} // No instantiation

    /**
     * Dati estratti da CONF1.
     */
    public static class Conf1Data {
        public String version;
        public boolean isMain;
        public boolean rx1;
        public boolean rx2;
        public boolean verify;
        public boolean cmdOnOff;
        public List<Zone> zones = new ArrayList<>();
    }

    /**
     * Dati risposta generica.
     */
    public static class ResponseData {
        public boolean success;
        public String status;
        public String message;
        public String errorCode;
        public String scenario;
        public String zones;
    }

    /**
     * Parsa risposta CONF1.
     * Formato: CONF1:VV.VV&FLAGS.PPPP&Z1=nome1&Z2=nome2&...&Z8=nome8&
     *
     * @param response Risposta SMS
     * @return Dati parsati o null se errore
     */
    public static Conf1Data parseConf1(String response) {
        if (response == null || !response.startsWith(Constants.RESP_CONF1)) {
            return null;
        }

        Conf1Data data = new Conf1Data();

        try {
            // Rimuovi prefisso "CONF1:"
            String content = response.substring(6);
            // Rimuovi terminatore se presente
            content = removeTerminator(content);

            String[] fields = content.split("&");

            for (String field : fields) {
                if (field.isEmpty()) continue;

                if (field.contains(".") && !field.contains("=")) {
                    // FLAGS.PPPP o versione
                    if (field.matches("\\d+\\.\\d+")) {
                        // Versione firmware
                        data.version = field;
                    } else {
                        // FLAGS.PPPP
                        parseFlags(field, data);
                    }
                } else if (field.startsWith("Z") && field.contains("=")) {
                    // Zn=nome
                    Zone zone = parseZone(field);
                    if (zone != null) {
                        data.zones.add(zone);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return data;
    }

    private static void parseFlags(String field, Conf1Data data) {
        String[] parts = field.split("\\.");
        if (parts.length >= 1) {
            data.isMain = "MAIN".equals(parts[0]);
        }
        if (parts.length >= 2 && parts[1].length() == 4) {
            data.rx1 = parts[1].charAt(0) == '1';
            data.rx2 = parts[1].charAt(1) == '1';
            data.verify = parts[1].charAt(2) == '1';
            data.cmdOnOff = parts[1].charAt(3) == '1';
        }
    }

    private static Zone parseZone(String field) {
        // Z1=Ingresso
        if (field.length() < 4 || field.charAt(1) < '1' || field.charAt(1) > '8') {
            return null;
        }

        int zoneNumber = field.charAt(1) - '0';
        String zoneName = field.substring(3);

        Zone zone = new Zone();
        zone.setSlot(zoneNumber);
        zone.setName(zoneName);
        zone.setEnabled(!Constants.ZONE_NOT_ENABLED.equals(zoneName));

        return zone;
    }

    /**
     * Parsa risposta CONF2 o CONF3 (scenari).
     * Formato: CONFx:Snn=nome&Snn=nome&...
     *
     * @param response Risposta SMS
     * @return Lista scenari parsati
     */
    public static List<Scenario> parseScenarios(String response) {
        List<Scenario> scenarios = new ArrayList<>();

        if (response == null ||
                (!response.startsWith(Constants.RESP_CONF2) && !response.startsWith(Constants.RESP_CONF3))) {
            return scenarios;
        }

        try {
            String content = response.substring(6);
            content = removeTerminator(content);

            String[] fields = content.split("&");

            for (String field : fields) {
                if (field.startsWith("S") && field.contains("=")) {
                    // Snn=nome
                    int scenarioNum = Integer.parseInt(field.substring(1, 3));
                    String scenarioName = field.substring(4);

                    Scenario s = new Scenario();
                    s.setSlot(scenarioNum);
                    s.setName(scenarioName);
                    s.setEnabled(!Constants.SCENARIO_NOT_ENABLED.equals(scenarioName));
                    scenarios.add(s);
                }
            }
        } catch (Exception e) {
            // Return partial results
        }

        return scenarios;
    }

    /**
     * Parsa risposta CONF4 o CONF5 (utenti).
     * Formato: CONFx:Rnn=nome&...&RJO=joker
     *
     * @param response Risposta SMS
     * @return Lista utenti parsati
     */
    public static List<User> parseUsers(String response) {
        List<User> users = new ArrayList<>();

        if (response == null ||
                (!response.startsWith(Constants.RESP_CONF4) && !response.startsWith(Constants.RESP_CONF5))) {
            return users;
        }

        try {
            String content = response.substring(6);
            content = removeTerminator(content);

            String[] fields = content.split("&");

            for (String field : fields) {
                if (!field.contains("=")) continue;

                String prefix = field.substring(0, 3);
                String name = field.substring(4);

                User user = new User();

                if (Constants.USER_JOKER_PREFIX.equals(prefix)) {
                    // Joker user
                    user.setSlot(0);
                    user.setName(name);
                    user.setJoker(true);
                    user.setEnabled(true);
                    users.add(user);
                } else if (prefix.matches("R\\d{2}")) {
                    // Regular user Rnn
                    int userNum = Integer.parseInt(prefix.substring(1));
                    user.setSlot(userNum);
                    user.setName(name);
                    user.setEnabled(!Constants.USER_NOT_ENABLED.equals(name));
                    users.add(user);
                }
            }
        } catch (Exception e) {
            // Return partial results
        }

        return users;
    }

    /**
     * Parsa risposta a comandi (OK/ERR/STATUS).
     *
     * @param response Risposta SMS
     * @return Dati risposta parsati
     */
    public static ResponseData parseResponse(String response) {
        ResponseData data = new ResponseData();

        if (response == null || response.isEmpty()) {
            data.success = false;
            data.errorCode = Constants.ERROR_TIMEOUT;
            return data;
        }

        String content = removeTerminator(response);

        if (content.startsWith(Constants.RESP_OK)) {
            data.success = true;
            String details = content.substring(3);
            parseOkDetails(details, data);
        } else if (content.startsWith(Constants.RESP_STATUS)) {
            data.success = true;
            String details = content.substring(7);
            parseStatusDetails(details, data);
        } else if (content.startsWith(Constants.RESP_ERROR)) {
            data.success = false;
            data.errorCode = content.substring(4);
        } else if (content.startsWith("SYS:") || content.startsWith("SYS :")) {
            // Formato reale: SYS: ON/OFF con altre righe
            data.success = true;
            parseRealStatusFormat(content, data);
        } else {
            data.success = false;
            data.errorCode = Constants.ERROR_UNKNOWN_CMD;
        }

        return data;
    }

    /**
     * Parsa il formato reale di risposta stato:
     * SYS: ON
     * SCE:---
     * ZONES:cont giorno;cont notte;volumetrici;porta;zona 7
     * 230V: KO
     * BATT: OK
     */
    private static void parseRealStatusFormat(String content, ResponseData data) {
        String[] lines = content.split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("SYS:") || line.startsWith("SYS :")) {
                // Estrai stato: ON = ARMED, OFF = DISARMED
                String sysValue = line.substring(line.indexOf(":") + 1).trim();
                if ("ON".equalsIgnoreCase(sysValue)) {
                    data.status = Constants.STATUS_ARMED;
                } else if ("OFF".equalsIgnoreCase(sysValue)) {
                    data.status = Constants.STATUS_DISARMED;
                } else if (sysValue.toUpperCase().contains("ALARM")) {
                    data.status = Constants.STATUS_ALARM;
                } else if (sysValue.toUpperCase().contains("TAMPER")) {
                    data.status = Constants.STATUS_TAMPER;
                } else {
                    data.status = Constants.STATUS_UNKNOWN;
                }
            } else if (line.startsWith("SCE:")) {
                // Scenario attivo
                String scenario = line.substring(4).trim();
                if (!"---".equals(scenario) && !scenario.isEmpty()) {
                    data.scenario = scenario;
                }
            } else if (line.startsWith("ZONES:")) {
                // Zone attive
                data.zones = line.substring(6).trim();
            }
            // 230V e BATT sono informativi, non li processiamo per ora
        }
    }

    private static void parseOkDetails(String details, ResponseData data) {
        // OK:ARMED:scenario_name o OK:DISARMED
        String[] parts = details.split(":");
        if (parts.length >= 1) {
            data.status = parts[0];
        }
        if (parts.length >= 2) {
            data.scenario = parts[1];
        }
    }

    private static void parseStatusDetails(String details, ResponseData data) {
        // STATUS:ARMED&SCE=Casa&ZONES=1234
        String[] parts = details.split("&");
        for (String part : parts) {
            if (part.contains("=")) {
                String[] kv = part.split("=");
                if (kv.length == 2) {
                    if ("SCE".equals(kv[0])) {
                        data.scenario = kv[1];
                    } else if ("ZONES".equals(kv[0])) {
                        data.zones = kv[1];
                    }
                }
            } else {
                data.status = part;
            }
        }
    }

    /**
     * Verifica se la risposta indica continuazione (termina con &).
     *
     * @param response Risposta SMS
     * @return true se ci sono altri messaggi in arrivo
     */
    public static boolean hasContinuation(String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        char last = response.charAt(response.length() - 1);
        return last == Constants.SEP_FIELD;
    }

    /**
     * Verifica se la risposta è terminata (termina con #).
     *
     * @param response Risposta SMS
     * @return true se la comunicazione è terminata
     */
    public static boolean isTerminated(String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        char last = response.charAt(response.length() - 1);
        return last == Constants.SEP_END;
    }

    /**
     * Identifica il tipo di risposta.
     *
     * @param response Risposta SMS
     * @return Prefisso identificativo (CONF1, OK, STATUS, etc.) o null
     */
    public static String identifyResponse(String response) {
        if (response == null) return null;

        if (response.startsWith(Constants.RESP_CONF1)) return "CONF1";
        if (response.startsWith(Constants.RESP_CONF2)) return "CONF2";
        if (response.startsWith(Constants.RESP_CONF3)) return "CONF3";
        if (response.startsWith(Constants.RESP_CONF4)) return "CONF4";
        if (response.startsWith(Constants.RESP_CONF5)) return "CONF5";
        if (response.startsWith(Constants.RESP_OK)) return "OK";
        if (response.startsWith(Constants.RESP_STATUS)) return "STATUS";
        if (response.startsWith(Constants.RESP_ERROR)) return "ERROR";

        // Formato alternativo: SYS: ON/OFF (risposta stato reale)
        if (response.startsWith("SYS:") || response.startsWith("SYS :")) return "STATUS";

        return null;
    }

    private static String removeTerminator(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        char last = content.charAt(content.length() - 1);
        if (last == Constants.SEP_END || last == Constants.SEP_FIELD) {
            return content.substring(0, content.length() - 1);
        }
        return content;
    }
}
