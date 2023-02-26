package com.example.assesment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class QA_Assessment {
    private static final int MAX_CYCLES = 20;
    private static final String BASE_URL = "https://en.wikipedia.org";

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Wikipedia link: ");
        String url = scanner.nextLine();

        System.out.print("Enter number of cycles: ");
        int n = scanner.nextInt();

        if (!isValidWikiLink(url)) {
            System.err.println("Invalid Wikipedia link: " + url);
            System.exit(1);
        }

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();

        Set<String> visitedLinks = new HashSet<>();
        Queue<String> linksToVisit = new LinkedList<>();
        linksToVisit.offer(url);


        // Start the scraping process
        for (int i = 0; i < n && !linksToVisit.isEmpty(); i++) {
            int numLinksVisitedThisCycle = 0;

            // Process all the links in the current cycle
            while (!linksToVisit.isEmpty() && numLinksVisitedThisCycle < MAX_CYCLES) {
                String currentLink = linksToVisit.poll();

                // Check if we've already visited this link
                if (visitedLinks.contains(currentLink)) {
                    continue;
                }

                // Visit the link and get all the links on the page
                driver.get(currentLink);
                visitedLinks.add(currentLink);
                Set<String> linksOnPage = getLinksOnPage(driver);

                // Add the new links to the queue
                for (String link : linksOnPage) {
                    if (!visitedLinks.contains(link) && isValidWikiLink(link)) {
                        linksToVisit.offer(link);
                    }
                }

                numLinksVisitedThisCycle++;
            }
        }
        driver.quit();

        try {
            FileWriter writer = new FileWriter("results.csv");
            writer.write("Total Links Found,Unique Links Found\n");
            writer.write(visitedLinks.size() + "," + getUniqueLinks(visitedLinks).size() + "\n");
            writer.write("links\n");
            for (String links : visitedLinks) {
                writer.write(links + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to write results to file: " + e.getMessage());
        }
    }


    private static boolean isValidWikiLink(String url) {
        return url.startsWith(BASE_URL + "/wiki/");
    }


    private static Set<String> getLinksOnPage(WebDriver driver) {
        Set<String> linksOnPage = new HashSet<>();
        for (WebElement link : driver.findElements(By.tagName("a"))) {
            String href = link.getAttribute("href");
            if (href != null && isValidWikiLink(href)) {
                linksOnPage.add(href);
            }
        }
        return linksOnPage;

    }

    private static Set<String> getUniqueLinks(Set<String> links) {
        Set<String> uniqueLinks = new HashSet<>();
        for (String link : links) {
            String[] parts = link.split("#");
            uniqueLinks.add(parts[0]);
        }
        return uniqueLinks;
    }
}