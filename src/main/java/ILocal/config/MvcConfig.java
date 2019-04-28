package ILocal.config;

import ILocal.service.PasswordEncoderMD5;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter{
    @Value("${cross.origin.mapping}")
    private String origin;

    @Bean
    public PasswordEncoderMD5 passwordEncoder() {
        return new PasswordEncoderMD5();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origin)
                .allowedMethods("PUT", "DELETE", "GET", "POST")
                .allowCredentials(false).maxAge(3600);
    }
}
