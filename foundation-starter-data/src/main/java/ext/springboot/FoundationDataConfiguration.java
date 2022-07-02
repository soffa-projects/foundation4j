package ext.springboot;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({"dev.soffa.foundation.data.spring"})
@PropertySource("classpath:application-foundation-data.properties")
public class FoundationDataConfiguration {

}
