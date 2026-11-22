package com.dimas.minidoodle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Mini Doodle API")
                .version("v1")
                .description("""
                        Meeting scheduling service. Users manage FREE/BUSY time slots in
                        their personal calendar, book free slots as meetings, and query an
                        aggregated availability view over any time frame.
                        """));
    }
}
