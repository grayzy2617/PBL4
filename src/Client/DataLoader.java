package Client;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

public class DataLoader {
    private static final Gson GSON = new Gson();

    // Try multiple ways to load a JSON file and parse into given type
    private static <T> List<T> loadList(String filePath, java.lang.reflect.Type type) {
        // 1) try given path directly
        try (FileReader reader = new FileReader(filePath)) {
            return GSON.fromJson(reader, type);
        } catch (FileNotFoundException fnf) {
            // fall through to try other locations
        } catch (Exception e) {
            System.err.println("Failed to parse JSON from path: " + filePath);
            e.printStackTrace();
            return null;
        }

        // 2) try relative to working directory (user.dir)
        String alt = Paths.get(System.getProperty("user.dir"), filePath).toString();
        try (FileReader reader = new FileReader(alt)) {
            System.out.println("Loaded JSON from: " + alt);
            return GSON.fromJson(reader, type);
        } catch (FileNotFoundException fnf) {
            // continue
        } catch (Exception e) {
            System.err.println("Failed to parse JSON from path: " + alt);
            e.printStackTrace();
            return null;
        }

        // 3) try loading from classpath (for resources packaged inside jar)
        String cpPath = filePath;
        // remove leading src/ if present
        if (cpPath.startsWith("src/")) cpPath = cpPath.substring(4);
        InputStream is = DataLoader.class.getClassLoader().getResourceAsStream(cpPath);
        if (is != null) {
            try (InputStreamReader reader = new InputStreamReader(is)) {
                System.out.println("Loaded JSON from classpath resource: " + cpPath);
                return GSON.fromJson(reader, type);
            } catch (Exception e) {
                System.err.println("Failed to parse JSON from classpath resource: " + cpPath);
                e.printStackTrace();
                return null;
            }
        }

        System.err.println("Could not find JSON file (tried given path, user.dir+path, and classpath): " + filePath);
        return null;
    }

    public static List<Node> loadNodes(String filePath) {
        return loadList(filePath, new TypeToken<List<Node>>() {
        }.getType());
    }

    public static List<Link> loadLinks(String filePath) {
        return loadList(filePath, new TypeToken<List<Link>>() {
        }.getType());
    }
}
