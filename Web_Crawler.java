package Final;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Web_Crawler {
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();
    private final int numThreads;

    public Web_Crawler(int numThreads) {
        this.numThreads = numThreads;
    }

    public void crawl(String seedUrl, int maxPages) {
        queue.offer(seedUrl);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.execute(new CrawlWorker(maxPages));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private class CrawlWorker implements Runnable {
        private final int maxPages;

        public CrawlWorker(int maxPages) {
            this.maxPages = maxPages;
        }

        @Override
        public void run() {
            while (!queue.isEmpty() && visitedUrls.size() < maxPages) {
                String url = queue.poll();
                if (url != null && !visitedUrls.contains(url)) {
                    crawlUrl(url);
                }
            }
        }

        private void crawlUrl(String url) {
            try {
                System.out.println("Crawling: " + url);
                visitedUrls.add(url);

                URLConnection connection = new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Process the content of the page as needed
                    // For simplicity, we're just printing it here
                    System.out.println(line);
                }

                reader.close();

                // Extract links and add them to the queue for further crawling
                // You may need to use a proper HTML parser for this
                // For simplicity, we're just adding the URL itself here
                queue.offer(url);

            } catch (IOException e) {
                System.err.println("Failed to crawl URL: " + url);
            }
        }
    }

    public static void main(String[] args) {
        int numThreads = 4; // Number of threads
        int maxPages = 20; // Maximum number of pages to crawl
        String seedUrl = "https://docs.google.com/document/d/1OXSEKZG7z62xB5baNEERmyFzHQH5MQoM88_l8IeaG7M/edit"; // Seed URL

        Web_Crawler crawler = new Web_Crawler(numThreads);
        crawler.crawl(seedUrl, maxPages);
    }
}
