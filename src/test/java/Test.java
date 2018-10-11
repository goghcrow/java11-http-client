import java11.net.http.HttpClient;
import java11.net.http.HttpRequest;
import java11.net.http.HttpResponse;
import java11.util.concurrent.CompletableFuture;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;

public class Test {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        HttpClient client = HttpClient
                .newBuilder()
                .proxy(ProxySelector.getDefault())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("http://www.baidu.com"))
                .uri(new URI("https://www.baidu.com"))
//                .uri(new URI("https://www.eporner.com/"))
//                .version(HttpClient.Version.HTTP_1_1)
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();

        while (true) {
            System.out.println(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get().body());
//            System.out.println(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get().body());
        }

//        HttpResponse<String> response = HttpClient
//                .newBuilder()
//                .proxy(ProxySelector.getDefault())
//                .build()
//                .send(request, HttpResponse.BodyHandlers.ofString());
//
//        System.out.println(response.body());

//        ArrayBlockingQueue<CompletableFuture<HttpResponse<String>>> q = new ArrayBlockingQueue<CompletableFuture<HttpResponse<String>>>(500);
//
//        HttpClient cli = HttpClient
//                .newBuilder()
//                .proxy(ProxySelector.getDefault())
//                .build();
//
//        Thread t1 = new Thread(() -> {
//            while (true) {
//                CompletableFuture<HttpResponse<String>> cf =
//                        cli.sendAsync(request, HttpResponse.BodyHandlers.ofString());
//                try {
//                    q.put(cf);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//
//        Thread t2 = new Thread(() -> {
//            try {
//                int i = 0;
//                CompletableFuture<HttpResponse<String>> cf;
//                while ((cf = q.take()) != null) {
//                    try {
//                        System.out.println("[" + i + "]" + cf.get().body());
//                        i++;
//                    } catch (ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//                System.err.println("~~~~~~~!!!!!!!!!!!!!!!!");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//
//        t1.start();
//        t2.start();
//
//        t1.join();
//        t2.join();
    }
}
