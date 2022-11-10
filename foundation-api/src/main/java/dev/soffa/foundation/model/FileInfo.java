package dev.soffa.foundation.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileInfo {

    private String file;
    private String extension;

    private String name;
    private long length;

    public FileInfo(String file) {
        this.file = file;
    }

    public FileInfo(String file, long length) {
        this.file = file;
        this.length = length;
    }
}
