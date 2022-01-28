package ZomboidJavaHook.mac;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dict")
public class Dict {
    @XmlJavaTypeAdapter(KeyValueAdapter.class)
    protected Map<String, Variant> items;
}
