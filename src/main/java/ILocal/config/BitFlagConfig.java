package ILocal.config;


import org.springframework.context.annotation.*;

@Configuration
public class BitFlagConfig {

    @Bean
    public BitFlagConfig getBitFileConfig(){
        return new BitFlagConfig();
    }
}
