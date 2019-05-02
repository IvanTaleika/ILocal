package ILocal.config;


import ILocal.service.Translator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslatorConfig {

    @Bean
    public Translator getTranslator(){
        return new Translator();
    }
}
