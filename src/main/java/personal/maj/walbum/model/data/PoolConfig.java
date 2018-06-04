package personal.maj.walbum.model.data;

/**
 * Created by MAJ on 2018/6/4.
 */
public class PoolConfig extends Data {

    private String provider;

    private String minPoolSize;

    private String initialPoolSize;

    private String maxPoolSize;

    private String maxIdleTime;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(String minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public String getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(String initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(String maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public String getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(String maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }
}
