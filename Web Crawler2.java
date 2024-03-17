import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebCrawler {
    private Set<String> visitedUrls;
    private String startingUrl;
    private int maxDepth;
    private int numThreads;
    private ExecutorService executor;

    public WebCrawler(String startingUrl, int maxDepth, int numThreads) {
        this.visitedUrls = new HashSet<>();
        this.startingUrl = startingUrl;
        this.maxDepth = maxDepth;
        this.numThreads = numThreads;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public void startCrawling() {
        crawl(startingUrl, 0);
        executor.shutdown();
    }

    private void crawl(String url, int depth) {
        if (depth > maxDepth || visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);

        Runnable task = () -> {
            try {
                // Fetch the HTML content of the page
                Document document = Jsoup.connect(url).get();
                processPage(document);

                // Find and crawl links on the page
                Elements links = document.select("a[href]");
                for (Element link : links) {
                    String nextUrl = link.absUrl("href");
                    crawl(nextUrl, depth + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        executor.submit(task);
    }

    private void processPage(Document document) {
        // Implement your logic to process the page here
        // For example, you can extract data, save to a database, etc.
    }
}
public class Main {
    public static void main(String[] args) {
        String startingUrl = "https://example.com";
        int maxDepth = 5;
        int numThreads = 4;

        WebCrawler crawler = new WebCrawler(startingUrl, maxDepth, numThreads);
        crawler.startCrawling();
    }
}
