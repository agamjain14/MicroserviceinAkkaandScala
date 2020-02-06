package net.cs.core.api.conf;



import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.typesafe.config.*;

abstract class AbstractConfigDecorator implements Config {

    private final Config delegatedConfig;


    protected AbstractConfigDecorator(final Config delegateConfig) {
        this.delegatedConfig = delegateConfig;
    }

    @Override
    public ConfigObject root() {
        return delegatedConfig.root();
    }

    @Override
    public ConfigOrigin origin() {
        return delegatedConfig.origin();
    }

    @Override
    public Config withFallback(ConfigMergeable other) {
        return delegatedConfig.withFallback(other);
    }

    @Override
    public Config resolve() {
        return delegatedConfig.resolve();
    }

    @Override
    public Config resolve(ConfigResolveOptions options) {
        return delegatedConfig.resolve(options);
    }

    @Override
    public boolean isResolved() {
        return delegatedConfig.isResolved();
    }

    @Override
    public Config resolveWith(Config source) {
        return delegatedConfig.resolveWith(source);
    }

    @Override
    public Config resolveWith(Config source, ConfigResolveOptions options) {
        return delegatedConfig.resolveWith(source, options);
    }

    @Override
    public void checkValid(Config reference, String... restrictToPaths) {
        delegatedConfig.checkValid(reference, restrictToPaths);
    }

    @Override
    public boolean hasPath(String path) {
        return delegatedConfig.hasPath(path);
    }

    @Override
    public boolean isEmpty() {
        return delegatedConfig.isEmpty();
    }

    @Override
    public Set<Entry<String, ConfigValue>> entrySet() {
        return delegatedConfig.entrySet();
    }

    @Override
    public boolean getBoolean(String path) {
        return delegatedConfig.getBoolean(path);
    }

    @Override
    public Number getNumber(String path) {
        return delegatedConfig.getNumber(path);
    }

    @Override
    public int getInt(String path) {
        return delegatedConfig.getInt(path);
    }

    @Override
    public long getLong(String path) {
        return delegatedConfig.getLong(path);
    }

    @Override
    public double getDouble(String path) {
        return delegatedConfig.getDouble(path);
    }

    @Override
    public String getString(String path) {
        return delegatedConfig.getString(path);
    }

    @Override
    public ConfigObject getObject(String path) {
        return delegatedConfig.getObject(path);
    }

    @Override
    public Config getConfig(String path) {
        return delegatedConfig.getConfig(path);
    }

    @Override
    public Object getAnyRef(String path) {
        return delegatedConfig.getAnyRef(path);
    }

    @Override
    public ConfigValue getValue(String path) {
        return delegatedConfig.getValue(path);
    }

    @Override
    public Long getBytes(String path) {
        return delegatedConfig.getBytes(path);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Long getMilliseconds(String path) {
        return delegatedConfig.getMilliseconds(path);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Long getNanoseconds(String path) {
        return delegatedConfig.getNanoseconds(path);
    }

    @Override
    public long getDuration(String path, TimeUnit unit) {
        return delegatedConfig.getDuration(path, unit);
    }

    @Override
    public ConfigList getList(String path) {
        return delegatedConfig.getList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return delegatedConfig.getBooleanList(path);
    }

    @Override
    public List<Number> getNumberList(String path) {
        return delegatedConfig.getNumberList(path);
    }

    @Override
    public List<Integer> getIntList(String path) {
        return delegatedConfig.getIntList(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        return delegatedConfig.getLongList(path);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return delegatedConfig.getDoubleList(path);
    }

    public List<String> getStringList(String path) {
        return delegatedConfig.getStringList(path);
    }

    @Override
    public List<? extends ConfigObject> getObjectList(String path) {
        return delegatedConfig.getObjectList(path);
    }

    @Override
    public List<? extends Config> getConfigList(String path) {
        return delegatedConfig.getConfigList(path);
    }

    @Override
    public List<?> getAnyRefList(String path) {
        return delegatedConfig.getAnyRefList(path);
    }

    @Override
    public List<Long> getBytesList(String path) {
        return delegatedConfig.getBytesList(path);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Long> getMillisecondsList(String path) {
        return delegatedConfig.getMillisecondsList(path);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<Long> getNanosecondsList(String path) {
        return delegatedConfig.getNanosecondsList(path);
    }

    @Override
    public List<Long> getDurationList(String path, TimeUnit unit) {
        return delegatedConfig.getDurationList(path, unit);
    }

    @Override
    public Config withOnlyPath(String path) {
        return delegatedConfig.withOnlyPath(path);
    }

    @Override
    public Config withoutPath(String path) {
        return delegatedConfig.withoutPath(path);
    }

    @Override
    public Config atPath(String path) {
        return delegatedConfig.atPath(path);
    }

    @Override
    public Config atKey(String key) {
        return delegatedConfig.atKey(key);
    }

    @Override
    public Config withValue(String path, ConfigValue value) {
        return delegatedConfig.withValue(path, value);
    }

    @Override
    public boolean hasPathOrNull(String path) {
        return delegatedConfig.hasPathOrNull(path);
    }

    @Override
    public boolean getIsNull(String path) {
        return delegatedConfig.getIsNull(path);
    }

    @Override
    public ConfigMemorySize getMemorySize(String path) {
        return delegatedConfig.getMemorySize(path);
    }

    @Override
    public Duration getDuration(String path) {
        return delegatedConfig.getDuration(path);
    }

    @Override
    public List<ConfigMemorySize> getMemorySizeList(String path) {
        return delegatedConfig.getMemorySizeList(path);
    }

    @Override
    public List<Duration> getDurationList(String path) {
        return delegatedConfig.getDurationList(path);
    }


}
