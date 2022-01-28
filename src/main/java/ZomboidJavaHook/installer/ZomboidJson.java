package ZomboidJavaHook.installer;

import jakarta.json.JsonObject;

import java.util.List;

public class ZomboidJson {
    public String mainClass;
    public List<String> classpath;
    public List<String> vmArgs;
    public JsonObject windows;
}
