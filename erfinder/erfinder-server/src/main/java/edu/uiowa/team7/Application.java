package edu.uiowa.team7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

// Spring Boot Application Configuration
@SpringBootApplication
@ServletComponentScan
@EnableConfigurationProperties(Configuration.class)
public class Application extends SpringBootServletInitializer {

  // Main Entry Point
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  // Servlet Builder Configuration
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(Application.class);
  }

  // Embedded Server Customization
  @Component
  public static class EmbeddedServletContainerConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
      File launcherDirDirectory = new File(Objects.requireNonNull(getClass().getResource("/")).getFile(), "launcherDir");
      if (launcherDirDirectory.exists()) {
        // Sets document root
        factory.setDocumentRoot(launcherDirDirectory);
      }
    }
  }
}