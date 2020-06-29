import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSpider {

    protected static boolean isQuestion = false;
    private final Set<URL> links;
    private final long startTime;

    private WebSpider(URL startURL) {
        this.links = new HashSet<>();
        this.startTime = System.currentTimeMillis();
        crawl(initURLS(startURL));

    }

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                                             urlMatcher.end(0)));
        }

        return containedUrls;
    }
    private void crawl(Set<URL> urls) {
        boolean found = false;
        String possibleURL = null;
        urls.removeAll(this.links);
        if (!urls.isEmpty()) {
            final Set<URL> newURLS = new HashSet<>();
            try {
                this.links.addAll(urls);
                for (final URL url : urls) {
                    System.out.println("Time (ms) = " + (System.currentTimeMillis() - this.startTime) + " --------- Connected to: " + url);
                    final Document document = Jsoup.connect(url.toString()).get();
                    final Elements linksOnPage = document.select("a[href]");
                    final Elements paragraphs = document.select("p");
                    if (isQuestion = true) {

                        String text = document.body().text();
                        String trimmedText = text.substring(text.indexOf("Search Results") + 15);
                        List<String> extractedUrls = extractUrls(trimmedText);
                        for (String url1 : extractedUrls)
                        {
                            System.out.println(url1);
                            possibleURL = url1;
                            found = true;

                        }
                        if (!found){
                            System.out.println("URL is valid");
                            trimmedText = trimmedText.substring(trimmedText.indexOf(possibleURL));
                            trimmedText = trimmedText.substring(trimmedText.indexOf("Similar"));
                            trimmedText = trimmedText.substring(0, trimmedText.indexOf("."));
                        } else {
                            System.out.println("URL is invalid");
                            System.out.println(possibleURL);
                            trimmedText = trimmedText.substring(0, trimmedText.indexOf("."));
                            trimmedText = trimmedText.substring(0, trimmedText.indexOf("People"));
                        }
                        System.out.println(trimmedText.replace("/", ": ").replace("[tldr_position]", ""));
                        break;
                    }
                    for (Element p : paragraphs)
                        System.out.println(p.text());
                    for (final Element element : linksOnPage) {
                        final String urlText = element.attr("abs:href");
                        final URL discoveredURL = new URL(urlText);
                        newURLS.add(discoveredURL);
                    }
                }
            } catch (final Exception ex) {
                Logger.getLogger(WebSpider.class.getName()).log(Level.SEVERE, null, ex);
            }
            crawl(newURLS);
        }
    }

    private Set<URL> initURLS(URL startURL) {
        return Collections.singleton(startURL);
    }

    public static void main(String[] args) throws IOException {
        System.out.print("Enter search: ");
        Scanner sc = new Scanner(System.in);
        String keyword = sc.nextLine();
        String multiWord = keyword.replace(" ", "+");
        String[] questionPhrases = { "how", "why", "who", "what", "where" };
        for (int i = 0; i <= questionPhrases.length - 1; i++) {

            if (multiWord.contains(questionPhrases[i])) {
                isQuestion = true;
            }
            final WebSpider spider = new WebSpider(new URL("https://www.google.com/search?sxsrf=&q=" + multiWord));

        }

    }

}
