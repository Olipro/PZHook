package ZomboidJavaHook.mac;

import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

import java.util.List;

@XmlType(name = "string")
public class PlistString implements Variant {
    @XmlValue
    String value;

    protected PlistString() {}

    public PlistString(String value) {
        this.value = value;
    }

    public List<Variant> getArray() {
        return null;
    }

    public String getString() {
        return value;
    }
}
