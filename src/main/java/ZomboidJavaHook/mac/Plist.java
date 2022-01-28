package ZomboidJavaHook.mac;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.uptheinter.interceptify.util.Util;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "plist")
@XmlRootElement(name = "plist")
public class Plist {
    @XmlAttribute(name = "version")
    protected String version;

    @XmlElement(name = "dict")
    @XmlJavaTypeAdapter(KeyValueAdapter.class)
    protected Map<String, Variant> dict;

    private static final SAXParser sp;
    private static final Unmarshaller um;
    private static final Marshaller mu;
    static {
        SAXParser spTmp = null;
        Unmarshaller umTmp = null;
        Marshaller muTmp = null;
        try {
            var ctx = JAXBContext.newInstance(Plist.class);
            var spf = SAXParserFactory.newInstance();
            spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            spTmp = spf.newSAXParser();
            spTmp.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "all");
            umTmp = ctx.createUnmarshaller();
            muTmp = ctx.createMarshaller();
        } catch (Exception e) {
            Util.DebugError(e);
        }
        sp = spTmp;
        um = umTmp;
        mu = muTmp;
    }

    public Map<String, Variant> getDict() {
        return dict;
    }

    public static Plist deserialise(InputStream in) throws SAXException, JAXBException {
        var src = new SAXSource(sp.getXMLReader(), new InputSource(in));
        return (Plist)um.unmarshal(in);
    }

    public static void serialise(Plist inst, OutputStream out) throws JAXBException {
        mu.marshal(inst, out);
    }
}
