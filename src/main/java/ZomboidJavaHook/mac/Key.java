package ZomboidJavaHook.mac;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "key")
public class Key extends PlistString {
    @SuppressWarnings("unused")
    protected Key() {}

    public Key(String value) {
        super(value);
    }
}
