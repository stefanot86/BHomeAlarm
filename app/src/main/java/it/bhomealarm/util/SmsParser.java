package it.bhomealarm.util;

import java.util.ArrayList;
import java.util.List;

import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;

/**
 * Parser per le risposte SMS ricevute dal sistema di allarme BHome.
 * <p>
 * Questa classe fornisce metodi statici per interpretare e convertire
 * i messaggi SMS ricevuti dal sistema di allarme in oggetti Java utilizzabili
 * dall'applicazione.
 * <p>
 * Formati SMS supportati:
 * <ul>
 *     <li><b>CONF1:</b> Configurazione base (versione, flags, zone)</li>
 *     <li><b>CONF2/CONF3:</b> Scenari di attivazione (1-8 e 9-16)</li>
 *     <li><b>CONF4/CONF5:</b> Utenti autorizzati (1-8 e 9-16)</li>
 *     <li><b>OK:</b> Conferma operazione completata</li>
 *     <li><b>STATUS:</b> Stato corrente del sistema</li>
 *     <li><b>ERR:</b> Errore nell'esecuzione del comando</li>
 *     <li><b>SYS:</b> Formato alternativo per lo stato del sistema</li>
 * </ul>
 * <p>
 * I messaggi utilizzano i separatori definiti in {@link Constants}:
 * <ul>
 *     <li>{@code :} - separatore comando/dati</li>
 *     <li>{@code &} - separatore campi</li>
 *     <li>{@code =} - assegnazione valore</li>
 *     <li>{@code .} - separatore flags</li>
 *     <li>{@code #} - terminatore messaggio</li>
 * </ul>
 * <p>
 * La classe e' dichiarata final e ha un costruttore privato per impedirne
 * l'istanziazione (pattern utility class).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see Constants
 */
public final class SmsParser {

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     */
    private SmsParser() {} // No instantiation

    /**
     * Classe contenitore per i dati estratti dalla risposta CONF1.
     * <p>
     * CONF1 contiene le informazioni base del sistema:
     * <ul>
     *     <li>Versione firmware</li>
     *     <li>Tipo utente (MAIN o secondario)</li>
     *     <li>Flags permessi dell'utente</li>
     *     <li>Elenco delle 8 zone con i rispettivi nomi</li>
     * </ul>
     */
    public static class Conf1Data {
        /** Versione firmware del sistema (es. "1.00"). */
        public String version;

        /** Indica se l'utente e' il principale (MAIN) del sistema. */
        public boolean isMain;

        /** Permesso ricezione notifiche RX1 (allarme). */
        public boolean rx1;

        /** Permesso ricezione notifiche RX2 (avvisi). */
        public boolean rx2;

        /** Permesso ricezione conferme operazioni. */
        public boolean verify;

        /** Permesso invio comandi ON/OFF. */
        public boolean cmdOnOff;

        /** Lista delle zone configurate nel sistema. */
        public List<Zone> zones = new ArrayList<>();
    }

    /**
     * Classe contenitore per i dati di una risposta generica (OK, STATUS, ERR).
     * <p>
     * Utilizzata per rappresentare l'esito di comandi come attivazione,
     * disattivazione e richiesta stato.
     */
    public static class ResponseData {
        /** Indica se l'operazione ha avuto successo. */
        public boolean success;

        /** Stato del sistema (ARMED, DISARMED, ALARM, TAMPER, UNKNOWN). */
        public String status;

        /** Messaggio descrittivo opzionale. */
        public String message;

        /** Codice errore in caso di fallimento. */
        public String errorCode;

        /** Nome dello scenario attivo (se il sistema e' armato). */
        public String scenario;

        /** Elenco zone attive (formato dipende dalla risposta). */
        public String zones;
    }

    /**
     * Parsa una risposta CONF1 dal sistema di allarme.
     * <p>
     * Formato atteso: {@code CONF1:VV.VV&FLAGS.PPPP&Z1=nome1&Z2=nome2&...&Z8=nome8&}
     * <p>
     * Dove:
     * <ul>
     *     <li>VV.VV = versione firmware</li>
     *     <li>FLAGS = tipo utente (MAIN o altro)</li>
     *     <li>PPPP = 4 bit permessi (rx1, rx2, verify, cmdOnOff)</li>
     *     <li>Zn=nome = nome della zona n (1-8)</li>
     * </ul>
     *
     * @param response il messaggio SMS ricevuto dal sistema
     * @return un oggetto {@link Conf1Data} con i dati estratti,
     *         oppure {@code null} se il messaggio e' null, non inizia con
     *         "CONF1:" o si verifica un errore di parsing
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

    /**
     * Parsa i flags e i permessi utente da un campo CONF1.
     * <p>
     * Formato atteso: {@code FLAGS.PPPP} dove FLAGS e' "MAIN" o altro
     * e PPPP sono 4 caratteri '0' o '1' che rappresentano i permessi.
     *
     * @param field il campo da parsare
     * @param data l'oggetto Conf1Data da popolare con i dati estratti
     */
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

    /**
     * Parsa una definizione di zona da un campo CONF1.
     * <p>
     * Formato atteso: {@code Zn=nome} dove n e' il numero zona (1-8)
     * e nome e' il nome assegnato alla zona.
     *
     * @param field il campo da parsare (es. "Z1=Ingresso")
     * @return un oggetto {@link Zone} configurato, oppure {@code null}
     *         se il formato non e' valido
     */
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
     * Parsa una risposta CONF2 o CONF3 contenente gli scenari di attivazione.
     * <p>
     * Formato atteso: {@code CONFx:Snn=nome&Snn=nome&...}
     * <p>
     * CONF2 contiene scenari 1-8, CONF3 contiene scenari 9-16.
     *
     * @param response il messaggio SMS ricevuto dal sistema
     * @return una lista di oggetti {@link Scenario} estratti dal messaggio;
     *         lista vuota se il messaggio e' null o non valido
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
     * Parsa una risposta CONF4 o CONF5 contenente gli utenti autorizzati.
     * <p>
     * Formato atteso: {@code CONFx:Rnn=nome&...&RJO=joker}
     * <p>
     * CONF4 contiene utenti 1-8 e l'utente Joker, CONF5 contiene utenti 9-16.
     * L'utente Joker (prefisso "RJO") e' un utente speciale con permessi elevati.
     *
     * @param response il messaggio SMS ricevuto dal sistema
     * @return una lista di oggetti {@link User} estratti dal messaggio;
     *         lista vuota se il messaggio e' null o non valido
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
     * Parsa una risposta generica a comandi (OK, ERR, STATUS o SYS).
     * <p>
     * Gestisce i seguenti formati:
     * <ul>
     *     <li>{@code OK:ARMED:scenario} - Operazione completata, sistema armato</li>
     *     <li>{@code OK:DISARMED} - Operazione completata, sistema disarmato</li>
     *     <li>{@code STATUS:stato&SCE=scenario&ZONES=zone} - Stato del sistema</li>
     *     <li>{@code ERR:codice} - Errore nell'esecuzione</li>
     *     <li>{@code SYS: ON/OFF} - Formato alternativo stato (multilinea)</li>
     * </ul>
     *
     * @param response il messaggio SMS ricevuto dal sistema
     * @return un oggetto {@link ResponseData} con i dati estratti;
     *         se il messaggio e' null o vuoto, restituisce un ResponseData
     *         con success=false e errorCode=TIMEOUT
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
     * Parsa il formato reale di risposta stato multilinea.
     * <p>
     * Formato atteso (su piu' righe):
     * <pre>
     * SYS: ON
     * SCE:---
     * ZONES:cont giorno;cont notte;volumetrici
     * 230V: KO
     * BATT: OK
     * </pre>
     *
     * @param content il contenuto del messaggio da parsare
     * @param data l'oggetto ResponseData da popolare con i dati estratti
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

    /**
     * Parsa i dettagli di una risposta OK.
     * <p>
     * Formato atteso: {@code stato:scenario} (es. "ARMED:Casa")
     *
     * @param details i dettagli dopo il prefisso "OK:"
     * @param data l'oggetto ResponseData da popolare
     */
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

    /**
     * Parsa i dettagli di una risposta STATUS.
     * <p>
     * Formato atteso: {@code stato&SCE=scenario&ZONES=zone}
     *
     * @param details i dettagli dopo il prefisso "STATUS:"
     * @param data l'oggetto ResponseData da popolare
     */
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
     * Verifica se una risposta SMS indica che ci sono altri messaggi in arrivo.
     * <p>
     * Le risposte lunghe possono essere suddivise su piu' SMS. Un messaggio
     * che termina con il carattere '&amp;' indica che seguira' una continuazione.
     *
     * @param response il messaggio SMS da verificare
     * @return {@code true} se il messaggio termina con '&amp;' (separatore campo),
     *         indicando che ci sono altri messaggi in arrivo;
     *         {@code false} altrimenti o se il messaggio e' null/vuoto
     */
    public static boolean hasContinuation(String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        char last = response.charAt(response.length() - 1);
        return last == Constants.SEP_FIELD;
    }

    /**
     * Verifica se una risposta SMS e' terminata correttamente.
     * <p>
     * Un messaggio che termina con il carattere '#' indica che la
     * comunicazione e' completa e non seguiranno altri messaggi.
     *
     * @param response il messaggio SMS da verificare
     * @return {@code true} se il messaggio termina con '#' (terminatore),
     *         {@code false} altrimenti o se il messaggio e' null/vuoto
     */
    public static boolean isTerminated(String response) {
        if (response == null || response.isEmpty()) {
            return false;
        }
        char last = response.charAt(response.length() - 1);
        return last == Constants.SEP_END;
    }

    /**
     * Identifica il tipo di risposta SMS in base al prefisso.
     * <p>
     * Analizza l'inizio del messaggio per determinare quale tipo di
     * risposta e' stata ricevuta dal sistema di allarme.
     *
     * @param response il messaggio SMS da identificare
     * @return una stringa identificativa del tipo di risposta:
     *         <ul>
     *             <li>"CONF1", "CONF2", "CONF3", "CONF4", "CONF5" - configurazioni</li>
     *             <li>"OK" - operazione completata</li>
     *             <li>"STATUS" - stato del sistema (include formato "SYS:")</li>
     *             <li>"ERROR" - errore</li>
     *             <li>{@code null} - tipo non riconosciuto o messaggio null</li>
     *         </ul>
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

    /**
     * Rimuove il carattere terminatore da una stringa SMS.
     * <p>
     * Rimuove l'ultimo carattere se e' '#' (terminatore) o '&amp;' (separatore campo).
     *
     * @param content il contenuto da processare
     * @return il contenuto senza il terminatore finale, oppure il contenuto
     *         originale se non termina con un carattere terminatore
     */
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
