package com.segurosbolivar.polizas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "app.polizas")
public class ConfiguracionPolizas {

    private BigDecimal ipc;
    private String apiKey;

    public BigDecimal getIpc() {
        return ipc;
    }

    public void setIpc(BigDecimal ipc) {
        this.ipc = ipc;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
