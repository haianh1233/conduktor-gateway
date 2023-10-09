package io.conduktor.gateway.tls;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TrustStoreConfig {
    @NotBlank
    private String trustStorePath;
    @NotBlank
    private String trustStorePassword;
    @NotBlank
    private String keyPassword;
    @NotBlank
    private String trustStoreType = "jks";
    @Min(1)
    @Max(Integer.MAX_VALUE)
    private long updateIntervalMsecs = 600000L;

    public void setUpdateIntervalMsecs(long updateIntervalMsecs) {
        this.updateIntervalMsecs = Math.max(1, updateIntervalMsecs);
    }
}
