package com.eventmgmt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps the physical QR code / certificate folders on disk to public URLs.
 *
 * Without this, files saved to src/main/resources/static/qr-codes/
 * and static/certificates/ at runtime would NOT be served, because
 * Spring Boot only auto-serves classpath static resources that existed
 * at build time, not files written after the JAR is packaged.
 *
 * Maps:
 *   /api/qr-codes/{file}     → {qr-code-dir}/{file}
 *   /api/certificates/{file} → {certificate-dir}/{file}
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.qr-code-dir}")
    private String qrCodeDir;

    @Value("${app.file.certificate-dir}")
    private String certificateDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/qr-codes/**")
                .addResourceLocations("file:" + normalize(qrCodeDir));

        registry.addResourceHandler("/certificates/**")
                .addResourceLocations("file:" + normalize(certificateDir));
    }

    private String normalize(String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}