package util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class HttpCheck implements Callable<Void> {

    private final URL url;

    public HttpCheck(URL url) {
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public Void call() throws Exception {

        while (true) {
            try {
                System.out.println("Check " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                System.out.println(url + " started." + conn.getResponseCode());
                return null;
            } catch (IOException ex) {
                System.out.println("Check failed " + url + " " + ex.getMessage());
                try {
                    TimeUnit.MILLISECONDS.sleep(500L);
                } catch (InterruptedException ex1) {
                }
            }
        }

    }

}
