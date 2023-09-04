package app;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class AppProperties {
    public static List<String> keywords;
    public static String fileCorpusPrefix;
    public static Long dirCrawlerSleepTime;
    public static Long fileScanningSizeLimit;
    public static Integer hopCount;
    public static Long urlRefreshTime;
    public static void initProperties() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties properties = new Properties();
        try {
            properties.load(loader.getResourceAsStream("application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Object key : properties.keySet()) {
            String keyString = key.toString();

            switch (keyString) {
                case "keywords" -> keywords = List.of(properties.getProperty(keyString).split(","));
                case "file_corpus_prefix" -> fileCorpusPrefix = properties.getProperty(keyString);
                case "hop_count" -> hopCount = Integer.valueOf(properties.getProperty(keyString));
                case "file_scanning_size_limit" -> fileScanningSizeLimit = Long.valueOf(properties.getProperty(keyString));
                case "url_refresh_time" -> urlRefreshTime = Long.valueOf(properties.getProperty(keyString));
                case "dir_crawler_sleep_time" -> dirCrawlerSleepTime = Long.valueOf(properties.getProperty(keyString));
            }
        }
    }

}
