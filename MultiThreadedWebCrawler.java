package Final;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MultiThreadedWebCrawler {
    private static final int MAX_DEPTH = 5;
    private static final int NUM_THREADS = 5; // Number of threads to use

    private static Set<String> visitedUrls = new HashSet<>(); // To store visited URLs
    private static ExecutorService executor; // Thread pool

    public static void main(String[] args) {
        String url = "https://jsoup.org/download";

        // Initialize the thread pool
        executor = Executors.newFixedThreadPool(NUM_THREADS);

        // Start crawling from the initial URL
        crawl(1, url);

        // Shutdown the thread pool once crawling is done
        executor.shutdown();
    }

    private static synchronized void crawl(int depth, String url) {
        if (depth > MAX_DEPTH) {
            return; // Stop crawling if reached max depth
        }

        // Check if the URL has already been visited
        if (visitedUrls.contains(url)) {
            return;
        }

        // Mark the URL as visited
        visitedUrls.add(url);

        // Log the crawling activity
        System.out.println(Thread.currentThread().getName() + " is crawling " + url);

        // Submit crawling task to the thread pool
        executor.submit(() -> {
            try {
                // Send HTTP request and retrieve the document
                Connection con = Jsoup.connect(url);
                Document doc = con.get();

                // Print link and title of the document
                System.out.println(Thread.currentThread().getName() + " found link: " + url);
                System.out.println("Title: " + doc.title());

                // Extract links from the document and recursively crawl each link
                for (Element link : doc.select("a[href]")) {
                    String nextUrl = link.absUrl("href");
                    crawl(depth + 1, nextUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
