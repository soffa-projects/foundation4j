package ext.springboot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-vault-defaults.properties")
public class FoundationConfigAutoConfig {

}
