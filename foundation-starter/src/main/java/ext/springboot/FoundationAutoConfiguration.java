package ext.springboot;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@Configuration
@ComponentScan({"dev.soffa.foundation.spring"})
//@PropertySource("classpath:application-foundation.properties")
//@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class) // Note the file name with the extension unlike a property file. Also, it's not the `application.yml` file.
public class FoundationAutoConfiguration {

}
