package ZomboidJavaHook.mac;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapElement {
    @XmlElements({
            @XmlElement(name = "key", type = Key.class, required = false),
            @XmlElement(name = "string", type = PlistString.class, required = false),
            @XmlElement(name = "array", type = Array.class, required = false),
    })
    public List<Variant> elems;

    @SuppressWarnings("unused")
    private MapElement() {}

    public MapElement(Map<String, Variant> m) {
        elems = new ArrayList<>();
        m.forEach((k, v) -> {
            elems.add(new Key(k));
            elems.add(v);
        });
    }
}
