package com.spellboundmc.wizardduels.other;

import com.opencsv.CSVReader;
import com.spellboundmc.wizardduels.Config;
import com.spellboundmc.wizardduels.WizardDuels;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;

public class Translation {
    public static HashMap<String, String> EN_ENGLISH, DE_GERMAN, RU_RUSSIAN, UA_UKRAINIAN, LOL_LOLCAT, LA_LATIN, ES_SPAIN;

    public static void init() {
        EN_ENGLISH = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "en.csv")));
        DE_GERMAN = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "de.csv")));
        RU_RUSSIAN = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "ru.csv")));
        UA_UKRAINIAN = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "ua.csv")));
        LOL_LOLCAT = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "lol.csv")));
        LA_LATIN = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "la.csv")));
        ES_SPAIN = new HashMap<>(readCsvFile(new File(WizardDuels.DATA_FOLDER, "es.csv")));
    }

    public static String get(Locale locale, String key) {
        key = key.toLowerCase(Locale.ROOT).replace(" ", "");

        String en = EN_ENGLISH.get(key);
        if (en == null) {
            WizardDuels.LOG.warning("Translation \"" + key + "\" was not found! Returning the key now.");
            return key;
        }

        if (!Config.LOCALIZATION) return en;

        return switch (locale.getLanguage().toUpperCase()) {
            case "DE" -> DE_GERMAN.get(key);
            case "RU" -> RU_RUSSIAN.get(key);
            case "UA" -> UA_UKRAINIAN.get(key);
            case "LOL" -> LOL_LOLCAT.get(key);
            case "LA" -> LA_LATIN.get(key);
            case "ES" -> ES_SPAIN.get(key);
            default -> en;
        };
    }

    public static String get(Locale locale, String key, Object... args) {
        return String.format(get(locale, key), args);
    }

    public static @NotNull HashMap<String, String> readCsvFile(File file) {
        HashMap<String, String> map = new HashMap<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                map.put(line[0], line[1]);
            }
        } catch (Exception e) {
            WizardDuels.LOG.severe("Couldn't find file : " + file);
        }

        return map;
    }
}
