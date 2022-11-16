package dev.soffa.foundation.client;

import java.io.Closeable;
import java.io.OutputStream;
import java.util.List;

public interface RemoteFileClient extends Closeable {

    default List<RemoteFile> listFiles() {
        return listFiles("*");
    }

    List<RemoteFile> listFiles(String filter);

    void download(String filename, OutputStream outputStream);
}
