package dev.soffa.foundation.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectStorageConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;

}
