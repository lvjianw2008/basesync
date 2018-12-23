package com.bd.basesync.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nc.config")
@PropertySource("classpath:/ncConfig.properties")
public class NcConfig {

    private String NcName;
    private String insql;

    public String getNcName() {
        return NcName;
    }

    public void setNcName(String ncName) {
        NcName = ncName;
    }

    public String getInsql() {
        return insql;
    }

    public void setInsql(String insql) {
        this.insql = insql;
    }
}
