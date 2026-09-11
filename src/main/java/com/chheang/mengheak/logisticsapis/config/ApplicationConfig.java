package com.chheang.mengheak.logisticsapis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "config")
@Getter
@Setter
public class ApplicationConfig {

    private Security security = new Security();
    private Pagination pagination = new Pagination();
    private Bootstrap bootstrap = new Bootstrap();

    @Getter
    @Setter
    public static class Security {
        private Jwt jwt = new Jwt();
        private Cors cors = new Cors();
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private String issuer;
        private int accessTokenMinutes = 60;
        private int refreshTokenDays = 14;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Pagination {
        private String baseUrl;
        private Map<String, String> uri = new HashMap<>();

        public String getUrlByResource(String resource) {
            return baseUrl == null ? "" : baseUrl.concat(uri.getOrDefault(resource, ""));
        }
    }

    @Getter
    @Setter
    public static class Bootstrap {
        private boolean seedEnabled;
        private String adminEmail;
        private String adminPassword;
    }
}
