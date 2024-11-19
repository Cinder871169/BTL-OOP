import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getInt(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}