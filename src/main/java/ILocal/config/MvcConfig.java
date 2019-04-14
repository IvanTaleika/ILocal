package ILocal.config;

import ILocal.service.PasswordEncoderMD5;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MvcConfig {
    @Bean
    public PasswordEncoderMD5 passwordEncoder() {
        return new PasswordEncoderMD5();
    }
}
