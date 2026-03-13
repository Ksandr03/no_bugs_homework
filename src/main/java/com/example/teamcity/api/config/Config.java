package com.example.teamcity.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final String CONFIG_PROPERTIES = "config.properties";
    private static Config config;           // единственный экземпляр (Singleton)
    private Properties properties;          // Java-класс для работы с key=value файлами

    private Config() {                      // ПРИВАТНЫЙ конструктор — нельзя создать снаружи
        properties = new Properties();
        loadProperties(CONFIG_PROPERTIES);
    }

    public static Config getConfig() {      // единственный способ получить объект
        if (config == null) {               // если ещё не создан — создаём
            config = new Config();
        }
        return config;                      // возвращаем единственный экземпляр
    }

    private void loadProperties(String fileName) {
        // Читаем файл config.properties из ресурсов
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(fileName)) {
            if (stream == null) {
                System.err.println("File not found " + fileName);
            }
            properties.load(stream);        // загружаем key=value пары
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return getConfig().properties.getProperty(key);  // Config.getProperty("host") -> "192.168.0.104:8111"
    }
}
