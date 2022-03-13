package ext.springboot;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableScheduling
@Configuration
@ComponentScan({"dev.soffa.foundation.spring"})
@PropertySource("classpath:application-foundation.properties")
public class FoundationAutoConfiguration {

}
