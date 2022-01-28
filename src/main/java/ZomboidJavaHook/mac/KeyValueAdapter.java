package ZomboidJavaHook.mac;

import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.HashMap;
import java.util.Map;

@XmlType(name="")
public class KeyValueAdapter extends XmlAdapter<MapElement, Map<String, Variant>> {
    @Override
    public Map<String, Variant> unmarshal(MapElement v) {
        var ret = new HashMap<String, Variant>();
        var elems = v.elems;
        for (var i = 0; i < elems.size(); i += 2)
            ret.put(elems.get(i).getString(), elems.get(i + 1));
        return ret;
    }

    @Override
    public MapElement marshal(Map<String, Variant> v) {
        return new MapElement(v);
    }
}
