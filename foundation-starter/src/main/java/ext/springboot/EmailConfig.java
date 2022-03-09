package ext.springboot;

import lombok.Data;

import java.util.Map;

@Data
public class EmailConfig {

    private String from;
    private Map<String, String> clients;

}
