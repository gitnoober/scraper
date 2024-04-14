import FileWriter.FileWriter;
import Model.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CermatiScraper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CermatiScraper.class);

    public static void main(String[] args) {
        String url = "https://www.cermati.com/karir";
        WebDriver driver = setupWebDriver();
        LOGGER.info("Accessing URL: {}", url);
        driver.get(url);
        Map<String, List<Job>> departmentMap = new HashMap<>();
        try {
            WebElement link = driver.findElement(By.cssSelector("div.search-bar-wrapper > a"));
            String href = link.getAttribute("href");
            LOGGER.info("Href attribute value: {}", href);
            driver.get(href);
            while (true) {
                List<WebElement> jobListWrappers = driver.findElements(By.className("page-job-list-wrapper"));
                Map<String, String> jobDetailMap = new HashMap<>();
                for (WebElement jobListWrapper : jobListWrappers) {
                    WebElement jobLink = jobListWrapper.findElement(By.tagName("a"));
                    String jobHref = jobLink.getAttribute("href");
                    LOGGER.info("Href attribute of the job link: {}", jobHref);
                    List<WebElement> jobDetailElements = jobListWrapper.findElements(By.cssSelector("p.job-detail"));
                    int cnt = 0;
                    String deptName = "";
                    for (WebElement jobDetailElement : jobDetailElements) {
                        String jobDetailText = jobDetailElement.getText();
                        LOGGER.info("Job Detail: {}", jobDetailText);
                        if (cnt == 0) {
                            deptName = jobDetailText;
                        }
                        cnt++;
                    }
                    jobDetailMap.put(jobHref, deptName);
                    break;
                }

                // Process jobHrefs in parallel
                processJobHrefsInParallel(jobDetailMap, departmentMap);

                // Check if there's a next page and navigate to it
                try {
                    WebElement nextPageButton = driver.findElement(By.cssSelector("div.career-pagination > button.arrow-icon:not([disabled]) > i.fa.fa-angle-right"));
                    nextPageButton.click();
                    LOGGER.info("Clicked next page button");
                } catch (NoSuchElementException e) {
                    LOGGER.info("No next page button found. Exiting pagination loop.");
                    break;
                }
            }
            // Write departmentMap to JSON file
            FileWriter.writeDepartmentMapToJsonFile(departmentMap, "department_data_final.json");
        } catch (Exception err) {
            LOGGER.error("Error occurred while connecting to URL: {}", url, err);
        } finally {
            driver.quit();
        }

    }

    public static void setDriverProperty() {
        System.setProperty("webdriver.firefox.bin", "/opt/homebrew/bin/firefox");
        System.setProperty("webdriver.gecko.driver", "/Users/priyanshusingh/Downloads/geckodriver");
    }

    public static void processJobHrefsInParallel(Map<String, String> jobDetailMap, Map<String, List<Job>> departmentMap) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Number of available threads: {}", numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (Map.Entry<String, String> entry : jobDetailMap.entrySet()) {
            tasks.add(() -> {
                String href = entry.getKey();
                String departmentName = entry.getValue();
                LOGGER.info("Processing jobHref: {}, department : {}", href, departmentName);
                WebDriver driver = new FirefoxDriver();
                driver.get(href);
                WebElement jobTitleElement = driver.findElement(By.cssSelector("div.column.jobad-container > main.jobad-main.job > h1.job-title"));
                String jobTitle = jobTitleElement.getText();
                LOGGER.info("Job title: {}", jobTitle);

                WebElement jobLocationElement = driver.findElement(By.cssSelector("div.column.jobad-container > main.jobad-main.job > ul.job-details"));
                List<WebElement> liTags = jobLocationElement.findElements(By.tagName("li"));
                int liTagCnt = 0;
                String jobLocation = "";
                String jobType = "";
                for (WebElement liTag : liTags) {
                    String text = liTag.getText();
                    LOGGER.info("Job Location: {}", text);
                    if (liTagCnt == 0) {
                        jobLocation = text;
                    } else {
                        jobType = text;
                    }
                    liTagCnt++;
                }

                WebElement descriptionSection = driver.findElement(By.cssSelector("div.column.jobad-container > main.jobad-main.job > div.job-sections > div[itemprop='description'] > section#st-jobDescription"));
                List<WebElement> pElements = descriptionSection.findElements(By.tagName("p"));
                List<String> jobDescription = new ArrayList<>();
                for (WebElement pTag : pElements) {
                    String text = pTag.getText().trim();
                    if (text.isEmpty()) {
                        continue;
                    }
                    LOGGER.info("Job Description: {}", text);
                    jobDescription.add(text);
                }

                WebElement jobQualificationElement = driver.findElement(By.cssSelector("div.column.jobad-container > main.jobad-main.job > div.job-sections > div[itemprop='description'] > section#st-qualifications"));
                List<WebElement> jobQualificationPTags = jobQualificationElement.findElements(By.tagName("p"));
                List<String> jobQualification = new ArrayList<>();
                for (WebElement pTag : jobQualificationPTags) {
                    String text = pTag.getText().trim();
                    if (text.isEmpty()) {
                        continue;
                    }
                    LOGGER.info("Job Qualification: {}", text);
                    jobQualification.add(text);
                }

                Job job = new Job(jobTitle, jobLocation, jobDescription, jobQualification, jobType, null);
                // Check if Department already exists
                List<Job> jobs = departmentMap.computeIfAbsent(departmentName, k -> new ArrayList<>());
                // Add the job to the department
                jobs.add(job);

                driver.quit();
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get(); // Wait for each task to complete
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error occurred during parallel processing", e);
        } finally {
            executor.shutdown();
        }
    }

    private static WebDriver setupWebDriver() {
        System.setProperty("webdriver.firefox.bin", "/opt/homebrew/bin/firefox");
        System.setProperty("webdriver.gecko.driver", "/Users/priyanshusingh/Downloads/geckodriver");
        FirefoxOptions options = new FirefoxOptions();
        return new FirefoxDriver(options);
    }
}
