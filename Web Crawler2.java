import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Webcrawler {
    private Set<String> visitedUrls;
    private String startingUrl;
    private int maxDepth;
    private ExecutorService executor;

    public Webcrawler(String startingUrl, int maxDepth, int numThreads) {
        this.visitedUrls = new HashSet<>();
        this.startingUrl = startingUrl;
        this.maxDepth = maxDepth;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public void startCrawling() {
        crawl(startingUrl, 0);
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all tasks to finish
        }
        // After all threads are finished, print visited URLs for example
        System.out.println("Visited URLs:");
        visitedUrls.forEach(System.out::println);
    }

    private void crawl(String url, int depth) {
        if (depth > maxDepth || visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);

        Runnable task = () -> {
            try {
                Document document = Jsoup.connect(url).get();
                processPage(document);
                
                Elements links = document.select("a[href]");
                for (Element link : links) {
                    String nextUrl = link.absUrl("href");
                    if (!nextUrl.isEmpty()) {
                        crawl(nextUrl, depth + 1);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error fetching URL: " + url);
            }
        };

        executor.submit(task);
    }

    private void processPage(Document document) {
        // Here you can implement logic to process the page
        // For example, printing the title of the page
        System.out.println("Page Title: " + document.title());
    }

    public static void main(String[] args) {
        String[] startingUrls = {
            "http://naver.com",
            "http://google.com",
            "http://youtube.com",
            "https://www.op.gg/"
        };
        int maxDepth = 5;
        int numThreads = 4;
    
        for (String url : startingUrls) {
            Webcrawler crawler = new Webcrawler(url, maxDepth, numThreads);
            crawler.startCrawling();
        }
    }
}
