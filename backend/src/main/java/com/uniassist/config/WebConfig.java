package com.uniassist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Also allow serving frontend directory directly if hosted via Spring Boot
        File frontendDir = new File("../frontend");
        if (frontendDir.exists()) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:" + frontendDir.getAbsolutePath() + "/");
        }
    }
}
