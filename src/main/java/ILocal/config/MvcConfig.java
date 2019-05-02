package ILocal.config;


import ILocal.service.PasswordEncoderMD5;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Value("${cross.origin.mapping}")
    private String origin;

    @Value("\"${jwt.name.access}\"")
    private String access;

    @Value("\"${jwt.name.refresh}\"")
    private String refresh;

    @Bean
    public PasswordEncoderMD5 passwordEncoder() {
        return new PasswordEncoderMD5();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .exposedHeaders(access, refresh)
                .allowedOrigins(origin)
                .allowedMethods("PUT", "DELETE", "GET", "POST")
                .allowCredentials(false).maxAge(3600);
    }

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                connector.setProperty("relaxedQueryChars", "|{}[]`~\\/");
            }
        });
        return factory;
    }
}
