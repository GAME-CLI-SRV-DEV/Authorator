package io.nip.18.8.76.144.viaproxy.1.21.1.25565.authenticator

import net.raphimc.viaproxy.api.plugin.ViaProxyPlugin;
import net.raphimc.viaproxy.api.plugin.ViaProxyPluginInfo;
import net.raphimc.viaproxy.api.server.ViaProxyServer;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.yaml.snakeyaml.Yaml;
import sun.security.x509.*;

    private HttpsServer httpsServer;
    private int port;

public class main extends ViaProxyPlugin {
    @Override
    public void onEnable() {
        getLogger().info("AuthPlugin enabled!");
        getLogger().info("Warning! This Plugin Will Renew Key each time");
        getLogger().info("If You Don");
        try {
            // Load the configuration
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(new FileInputStream("config.yml"));
            port = (int) ((Map<String, Object>) config.get("server")).get("port");

            // Initialize the HTTPS server
            initializeHttpsServer();

            // Schedule certificate renewal
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::renewCertificate, 0, 365, TimeUnit.DAYS);

            getLogger().info("HTTPS server started on port " + port);
        } catch (Exception e) {
            getLogger().severe("Failed to start HTTPS server: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AuthPlugin disabled!");
        if (httpsServer != null) {
            httpsServer.stop(0);
            getLogger().info("HTTPS server stopped");
        }
    }

    private void initializeHttpsServer() throws Exception {
        // Load the keystore
        char[] password = "mypassword".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("mykey.keystore"), password);

        // Set up the key manager factory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, password);

        // Set up the trust manager factory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        // Set up the HTTPS context and parameters
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        // Create the HTTPS server
        httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        httpsServer.createContext("/", new MyHandler());
        httpsServer.setExecutor(null); // creates a default executor
        httpsServer.start();
    }

    private void renewCertificate() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            X509CertInfo certInfo = new X509CertInfo();
            Date from = new Date();
            Date to = new Date(from.getTime() + 365L * 24 * 60 * 60 * 1000); // 1 year validity
            CertificateValidity interval = new CertificateValidity(from, to);
            X500Name owner = new X500Name("CN=ViaProxyWebAuth, OU=ApproximasterPluginNet, O=ApproximasterAnarchy2004, L=Unknown, ST=Unknown, C=Unknown");

            certInfo.set(X509CertInfo.VALIDITY, interval);
            certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber((int) (from.getTime() / 1000)));
            certInfo.set(X509CertInfo.SUBJECT, owner);
            certInfo.set(X509CertInfo.ISSUER, owner);
            certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
            certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get("SHA256WithRSA")));

            X509CertImpl cert = new X509CertImpl(certInfo);
            cert.sign(keyPair.getPrivate(), "SHA256WithRSA");

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setKeyEntry("alias", keyPair.getPrivate(), "mypassword".toCharArray(), new Certificate[]{cert});
            try (FileOutputStream fos = new FileOutputStream("mykey.keystore")) {
                keyStore.store(fos, "mypassword".toCharArray());
            }

            getLogger().info("Certificate renewed successfully!");

        } catch (Exception e) {
            getLogger().severe("Failed to renew certificate: " + e.getMessage() + ": Please Use VIAaaS as a Alternative or reboot this plugin.");
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Hello, HTTPS!";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
