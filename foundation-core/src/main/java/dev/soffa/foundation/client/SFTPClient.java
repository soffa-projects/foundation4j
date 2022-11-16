package dev.soffa.foundation.client;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import dev.soffa.foundation.commons.TextUtil;
import dev.soffa.foundation.commons.UrlInfo;
import dev.soffa.foundation.error.TechnicalException;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SFTPClient implements RemoteFileClient {

    private final String sshKey;
    private final String passphrase;
    private final String username;
    private final String hostname;
    private final int port;

    private final String workdir;

    private Session session;
    private Channel channel;
    private ChannelSftp sftp;

    private SFTPClient(String hostname, int port, String username, String sshKey, String passphrase, String workdir) {
        this.sshKey = sshKey;
        this.passphrase = passphrase;
        this.username = username;
        this.hostname = hostname;
        this.port = port;
        this.workdir = workdir;
    }

    public static SFTPClient connect(String url) {
        return connect(UrlInfo.parse(url));
    }
    public static SFTPClient connect(UrlInfo info) {
        SFTPClient client = new SFTPClient(
            info.getHostname(),
            info.getPort(),
            info.getUsername(),
            info.param("sshkey", ""),
            info.param("passphrase", ""),
            info.getPath()
        );
        client.connect();
        return client;
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public List<RemoteFile> listFiles(String filter) {
        List<RemoteFile> files = new ArrayList<>();
        Iterator<ChannelSftp.LsEntry> itr = sftp.ls(filter).iterator();
        while (itr.hasNext()) {
            ChannelSftp.LsEntry entry = itr.next();
            files.add(new RemoteFile(entry.getFilename(), entry.getLongname()));
        }
        return files;
    }

    @SneakyThrows
    @Override
    public void download(String filename, OutputStream outputStream) {
        sftp.get(filename, outputStream);
    }

    @SneakyThrows
    private void connect() {
        JSch.setConfig("StrictHostKeyChecking", "no");
        JSch client = new JSch();
        if (TextUtil.isNotEmpty(sshKey)) {
            File keyFile = new File(sshKey);
            if (!keyFile.exists()) {
                throw new TechnicalException("%s file does not exists", sshKey);
            }
            if (TextUtil.isNotEmpty(passphrase)) {
                client.addIdentity(keyFile.getAbsolutePath(), passphrase);
            } else {
                client.addIdentity(keyFile.getAbsolutePath());
            }
        }
        session = client.getSession(username, hostname, port);
        session.connect();
        channel = session.openChannel("sftp");
        sftp = (ChannelSftp) channel;
        sftp.connect();
        if (TextUtil.isNotEmpty(workdir)) {
            sftp.cd(workdir);
        }
    }

    @Override
    public void close() throws IOException {
        if (sftp!=null) {
            sftp.disconnect();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

}
