package personal.maj.walbum;

import personal.maj.walbum.model.data.ConnectionData;
import personal.maj.walbum.session.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by MAJ on 2018/5/28.
 */
public class WhiteAlbum {

    private static WhiteAlbum whiteAlbum = new WhiteAlbum();

    private String configPath = "WhiteAlbum.xml";

    private Map<String, ConnectionData> connectionDataMap = new HashMap<>();

    private WhiteAlbum() {
    }

    public static WhiteAlbum get() {
        return whiteAlbum;
    }

    public Session open() {
        return null;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
