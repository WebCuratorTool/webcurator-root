package org.webcurator.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.webcurator.core.harvester.coordinator.HarvestAgentListenerService;

@SpringBootApplication
@ComponentScan(basePackages = { "org.webcurator.webapp", "org.webcurator.ui", "org.webcurator.core.harvester",
        "org.webcurator.core.rest" }//,
        // Put any exclusions here.
        //excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ClassToExclude.class)
)
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
public class WebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }
}
