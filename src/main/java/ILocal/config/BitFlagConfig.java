package ILocal.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BitFlagConfig {

    @Bean
    public BitFlagConfig getBitFileConfig(){
        return new BitFlagConfig();
    }
}
