package personal.maj.walbum.model.data;

import personal.maj.walbum.model.Model;

/**
 * Created by MAJ on 2018/5/29.
 */
public class ConnectionData extends Data {

    private String userName;

    private String password;

    private String host;

    private String port;

    private String dbName;

    private String provider;

    private PoolConfig config;

    public ConnectionData() {
    }

    public ConnectionData(String userName, String password, String host, String port, String dbName, String provider, PoolConfig config) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.dbName = dbName;
        this.provider = provider;
        this.config = config;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDbName() {
        return dbName;
    }

    public String getProvider() {
        return provider;
    }

    public String getUrl() {
        return "";
    }

    public PoolConfig getConfig() {
        return config;
    }

}
