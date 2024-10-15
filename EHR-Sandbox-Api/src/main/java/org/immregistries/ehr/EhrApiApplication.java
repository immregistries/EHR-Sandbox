package org.immregistries.ehr;


import org.immregistries.ehr.api.entities.BulkImportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@ServletComponentScan
//@EnableJpaRepositories()
@Import({
        FhirConfig.class
})
public class EhrApiApplication extends SpringBootServletInitializer {
    public static String VERSION = "1.2.3-SNAPSHOT-6";

    private static final Logger logger = LoggerFactory.getLogger(EhrApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EhrApiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EhrApiApplication.class);
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOriginPatterns("*")
//						.allowedOrigins("http://localhost:8080", "http://localhost:4200", "http://localhost:9091")
                        .allowedMethods("GET", "POST", "PUT");
            }
        };
    }


//    @Bean
//    public ServletWebServerFactory servletWebServerFactory() {
//        ServletWebServerFactory servletWebServerFactory = new ServletWebServerFactory() {
//            @Override
//            public WebServer getWebServer(ServletContextInitializer... initializers) {
//                return null;
//            }
//        }
//    }

    /**
     * Required to get access to httpRequest qnd session through spring, important to use the fhir client inside the servlets
     *
     * @return
     */
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }


    @Bean
    public Map<Integer, BulkImportStatus> resultCacheStore() {
        return new HashMap<>(30);
    }

//    @Bean
//    public ServletWebServerFactory servletWebServerFactory() {
//        return new TomcatServletWebServerFactory();
//    }

}
