package Final;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MultiThreadedPersistentDFSWebCrawler {
    private static final int MAX_DEPTH = 5;
    private static final int NUM_THREADS = 5; // Number of threads to use
    private static final String VISITED_URLS_FILE = "visited_urls.txt";

    private static Set<String> visitedUrls = new HashSet<>(); // To store visited URLs
    private static ExecutorService executor; // Thread pool

    public static void main(String[] args) {
        // Ensure that the visited_urls.txt file exists
        createVisitedUrlsFile();

        // Load visited URLs from file
        loadVisitedUrls();

        // Initialize the thread pool
        executor = Executors.newFixedThreadPool(NUM_THREADS);

        // Start crawling from the initial URLs
        String[] startingUrls = {
            "https://jsoup.org/download",
            "https://www.yahoo.com",
            "https://www.google.com"
        };
        for (String url : startingUrls) {
            crawl(url, 1);
        }

        // Shutdown the thread pool once crawling is done
        executor.shutdown();
    }

    private static void createVisitedUrlsFile() {
        File file = new File(VISITED_URLS_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void crawl(String url, int depth) {
        if (depth > MAX_DEPTH) {
            return; // Stop crawling if reached max depth
        }

        // Check if the URL has already been visited
        if (visitedUrls.contains(url)) {
            return;
        }

        // Mark the URL as visited
        visitedUrls.add(url);

        // Submit crawling task to the thread pool
        executor.submit(() -> {
            try {
                // Send HTTP request and retrieve the document
                Connection con = Jsoup.connect(url);
                Document doc = con.get();

                // Print link and title of the document (with thread name)
                System.out.println(Thread.currentThread().getName() + ": Link: " + url);
                System.out.println(Thread.currentThread().getName() + ": Title: " + doc.title());

                // Extract links from the document and recursively crawl each link
                for (Element link : doc.select("a[href]")) {
                    String nextUrl = link.absUrl("href");
                    crawl(nextUrl, depth + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Save visited URLs to file
        saveVisitedUrls();
    }

    private static void loadVisitedUrls() {
        try (BufferedReader reader = new BufferedReader(new FileReader(VISITED_URLS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                visitedUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveVisitedUrls() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VISITED_URLS_FILE))) {
            for (String url : visitedUrls) {
                writer.write(url);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
