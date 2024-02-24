

/**
 * 
 * NOTE : THIS CLASS REPRESENTS A MAXIMO USER WITH FUNCTIONALITIES RELATED TO MAXIMO SYSTEM
 * THE USER OBJECT IS USED TO MIMIC REAL USER'S BEHAVIOUR ON THE SYSTEM
 * 
 */


package maximo_automation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class User {

    //USER INFO
    private  String password ;
    private  String username ;
    private  String url ;
    
    //CREATE PUBLIC WEBDRIVER
    private WebDriver driver;
    private boolean loggedIn = false;
    private WebDriverWait wait;
    private WebDriverWait shortWait;
    //CHROME VARS
    private String chromeDriverPath =  "drivers\\chromedriver.exe";
    
    // GET CURRENT DIRECTORY AT RUNTIME
    String currentDir = System.getProperty("user.dir");

    // RELATIVE DIRECTORY
    String relativePath = "downloads"; // DOWNLOADS FOLDER

    // CONSTRUCT DYNAMIC ABSOLUTE PATH
    private String downloadPath = Paths.get(currentDir, relativePath).toString();
    
    //CREATE A SCHEDULED EXECUTER WITH ONE THREAD
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> ScheduledSystemErrorCheck ; 
    
    //PROPERTIES TO FETCH CONFIG PROP
    Properties properties = new Properties();
    

    //USER CONSTRUCTOR
    public User() {
        
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return; // Exit if properties file cannot be loaded
        }
        
        this.url = properties.getProperty("url");
        this.username = properties.getProperty("username");
        this.password = properties.getProperty("password");
        
        // SCHEDULE TASK
        ScheduledSystemErrorCheck = executorService.scheduleAtFixedRate(() -> {
            // CHECK FOR SYSTEM ERRORS
            checkForSystemError(driver);
        }, 0, 60, TimeUnit.SECONDS); //TASK WILL RUN EVERY 60s
    }
    
    //LOG IN METHOD
    public void login(){
        try{
        //MODIFYING CHROME OPTIONS
        ChromeOptions options = new ChromeOptions();
        
         Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("profile.default_content_settings.popups", 0);
            prefs.put("download.prompt_for_download", false);
            //This will set the path of the download folder
            prefs.put("download.default_directory", downloadPath); //RESET DEFAULT DOWNLOAD PATH
            prefs.put("savefile.default_directory", downloadPath); //RESET DEFAULT DOWNLOAD PATH
            prefs.put("profile.default_content_setting_values.notifications", 1);
            prefs.put("profile.default_content_settings.cookies", 1);
            //options.addArguments("--headless"); //THIS OPTION PREVENTS OPENING CHROME GUI
            //options.addArguments("--disable-gpu"); // DISABLE GPU TO AVOID ERRORS CAUSED BY RENDERING

            options.setExperimentalOption("prefs", prefs);

        
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        driver=new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get(this.url);
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(300)); //WAIT UP TO 5min TO FIND ELEMENTS
        shortWait = new WebDriverWait(driver, Duration.ofSeconds(120)); //WAIT UP TO 2min TO FIND ELEMENTS
        WebElement username=wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement password=wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement login=wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginbutton")));
        username.sendKeys(this.username);
        password.sendKeys(this.password);
        login.click();
        
        try{
            //LOOK FOR THE WELCOME MESSAGE AS A SIGN OF SUCCESFUL LOGIN
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titlebar-tb_appname")));
            loggedIn = true;
            System.out.println("Logged in Successfully");
        }catch(Exception e){
            String error=driver.findElement(By.className("errorText")).getText();
            System.out.println(error);
            System.out.println("Logged in Successfully");
        }
        
        }catch(Exception e){
            System.out.println("Login Error : " + e);
        }
    } //END OF LOG IN METHOD
    
    //LOG OUT METHOD
    public void logout() throws InterruptedException{
        try{
            if(loggedIn){

                WebElement logout = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titlebar_hyperlink_8-lbsignout_image")));
                logout.click();
                
                //DELAYING 3s TILL LOGGING OUT IS COMPLETE
                Thread.sleep(3000);

                try{
                //LOOK FOR THE WELCOME MESSAGE AS A SIGN OF SUCCESFUL LOGIN
                driver.findElement(By.id("titlebar-tb_appname"));
                System.out.println("Log out Failed");
                }catch(Exception e){
                    loggedIn = false;
                    driver.quit(); 
                    System.out.println("Logged out Successfully");
                    //CLOSE SIDE TASK CHECKING FOR SYSTEM ERRORS
                    ScheduledSystemErrorCheck.cancel(true);
                    executorService.shutdown();
                } 
                
            }
        }catch(Exception e){
            System.out.print("Error at Log out : " + e);
        } 
    } //END OF LOG OUT METHOD
    
    //NAVIGATE TO APPS BY MENU
    public void menuNavigate(String appName) { // INVALID METHOD
        //WebElement appLink = driver.findElement(By.xpath("//a[contains(text(), '" + appName + "')]"));
        //appLink.click();
    }  //END NAVIGATE TO APPS BY MENU
        
    //NAVIGATE TO APPS BY SEARCH
    public void searchNavigate(String appName) throws InterruptedException {
        try{  
            WebElement searchField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nav_search_fld")));
            searchField.sendKeys(appName);
            // Press Enter key to perform the search
            searchField.sendKeys(Keys.ENTER);
            try{
                WebElement firstSearchResult = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#nav_search_fld_menu li:nth-child(2) a")));
                firstSearchResult.click();
                wait.until(ExpectedConditions.titleContains(appName));//WAIT TILL APP IS LOADED
                String actualAppName = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titlebar-tb_appname"))).getText();
                System.out.println("Navigated to : "+actualAppName);
            } catch (NoSuchElementException e) {
                System.out.println("No search results found for: " + appName);
            }
        }catch(Exception e){
            System.out.println("Error at Search Navigate : " + e);
        }    
    } //END NAVIGATE TO APPS BY SEARCH

    //QUERY RECORDS
    public void queryRecords(String query) throws InterruptedException {
        try{    
            
            //EXECUTE QUERY 
            WebElement dropDownBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m68d8715f-tbb_dropdown_menuarrow")));
            dropDownBtn.click();

            WebElement whereBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("menu0_SEARCHWHER_OPTION_a")));
            whereBtn.click();

            WebElement whereTxt = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m8366b731-ta")));
            whereTxt.clear();
            whereTxt.sendKeys(query);

            WebElement queryBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m81200968-pb")));
            queryBtn.click();
            
            //CHECKING FOR SYSTEM ERRORS ELSE PRINT RESULT
            try{
                driver.findElement(By.id("msgbox-dialog_inner"));
                String sysMsg = driver.findElement(By.id("mb_msg")).getText();
                System.out.println("No search results found for: " + sysMsg);
                driver.findElement(By.id("m15f1c9f0-pb")).click(); //CLOSE MESSAGE BOX
                
            } catch (NoSuchElementException e) {

                //WAIT FOR RESULT CHANGE
                String oldResult = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m6a7dfd2f-lb3"))).getText();
                waitForTextChange(driver, "#m6a7dfd2f-lb3", oldResult);
    
                String resultLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m6a7dfd2f-lb3"))).getText();
                String queryResult = resultLabel.substring(resultLabel.indexOf("of") + 3).trim();
                System.out.println("Query Result : " + queryResult);
            
            }
        }catch(Exception e){
            System.out.print("Error at Qeuery Records : " + e);
        }    
    } //END QUERY RECORDS
    
    
    //QUERY RECORDS
    public void downloadRecords() throws InterruptedException {
        try{    
           
                //DOWNLOAD RESULT
                WebElement downloadBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("m6a7dfd2f-lb4_image"))); 
                long initialSize = getDirectorySize(downloadPath); //DOWNLOADS SIZE BEFORE DOWNLOADING
                downloadBtn.click();
                waitForDownload(initialSize); //WAIT TILL DOWNLOAD COMPLETE

                //FETCH DOWNLOADED FILE NAME
                File dir = new File(downloadPath);
                File[] files = dir.listFiles();

                // Filter the list to include only regular files (not directories)
                if (files != null) {
                    // Sort the files based on their last modified timestamp in descending order
                    Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                    // Get the newest file (the first file after sorting)
                    File newestFile = files.length > 0 ? files[0] : null;

                    if (newestFile != null) {
                        // Get the filename of the newest file
                        String filename = newestFile.getName();
                        System.out.println("Query Result Downloaded : " + filename);
                    } 
                } else {
                    System.out.println("Failed to retrieve files from the download directory.");
                }
                
            
        }catch(Exception e){
            System.out.print("Error at Download Records : " + e);
        }    
    } //END QUERY RECORDS
    
    //CREATE NEW RECORD
    public void createRecord(String desc) throws InterruptedException{
        try{
            //CREATE RECORD
            WebElement newRecBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("toolactions_INSERT-tbb_image")));
            newRecBtn.click();
            //ENTER DESCRIPTION
            WebElement RecDesc = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mad3161b5-tb2")));
            RecDesc.sendKeys(desc);
            //GET NEW RECORD NUMBER 
            String newRecCNo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mad3161b5-tb"))).getText();
            //SAVE NEW WO
            driver.findElement(By.id("toolactions_SAVE-tbb_image")).click();
            //WAIT TILL CONFIRMATION MESSAGE APPEARS
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titlebar_error")));
            System.out.println("Record Number " + newRecCNo + " Was Created");
            //MAVIGATE BACK TO APP
            WebElement backBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("m397b0593-tabs_middle")));
            backBtn.click();       
            waitForInvisibility(driver, "#m397b0593-tabs_middle"); //WAIT TILL BACK BUTTON I INVISIBLE
        }catch(Exception e){
            System.out.println("Error at Create Record : " + e);
        }
    } //END CREATE NEW RECORD
 
    //CHECH FOR SYSTEM ERRORS
    public static void checkForSystemError(WebDriver driver) {
        try{
        WebElement systemErrorDiv = driver.findElement(By.id("systemdialog"));
        String systemMessage = driver.findElement(By.id("ascerrmsg")).getText(); 
        WebElement closeBtn = driver.findElement(By.id("su_button1")); 
        closeBtn.click();
        System.out.println("System Error Message was Handled : " + systemMessage);
        }catch(Exception e){
            // IF THERE IS NO EXCEPTION THEN THERE WAS NO SYSTEM ERROR DIV WEB ELEMENT
        }
    } //END CHECK FOR ERRORS
    
    
//////PRIVATE METHODS    
    
    //PRIVATE METHOD TO WAIT TILL IT A CHANGE IN A SPECIFIC ELEMENTS CONTENT IS DETECTED
    private static void waitForContentChange(WebDriver driver, String elementId) { //INVALID METHOD
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300)); //WAIT UP TO 5min TO FIND ELEMENTS
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                WebElement element = driver.findElement(By.cssSelector(elementId));
                return !element.findElements(By.cssSelector("*")).isEmpty(); 
            }
        });
    } // END WAIT FOR CONTENT CHANGE
    
    //PRIVATE METHOD TO WAIT TILL IT A CHANGE IN A SPECIFIC ELEMENTS TEXT IS DETECTED
    private static void waitForTextChange(WebDriver driver, String elementId, String oldText) {    
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300)); //WAIT UP TO 5min TO FIND ELEMENTS
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                WebElement element = driver.findElement(By.cssSelector(elementId));
                String actualText = element.getText();
                return !actualText.equals(oldText); //WAIT TILL TEXT CHANGES
            }
        });
    } //END WAIT FOR TEXT CHANGE
    
    //PRIVATE METHOD TO WAIT TILL IT A GIVEN ELEMENT IS INVISIBLE
    private static void waitForInvisibility(WebDriver driver, String elementId) { 
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300)); //WAIT UP TO 5min TO FIND ELEMENTS
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    // Check if the element is present and invisible
                    WebElement element = driver.findElement(By.cssSelector(elementId));
                    return !element.isDisplayed();
                } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
                    // Handle exceptions when the element is not found or stale
                    return true; // Element is not present, consider it invisible
                }
            }
        });
    } //END   
    
    //PRIVATE METHOD TO WAIT TILL A GIVEN ELEMENT CONTAINS A GIVEN TEXT
    private static void waitTillTextContains(WebDriver driver, String elementId, String text) { 
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300)); //WAIT UP TO 5min TO FIND ELEMENTS
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                WebElement header = driver.findElement(By.id(elementId)); // Replace "yourHeaderId" with the actual ID of your header element
                String headerText = header.getText();
                return headerText.contains(text);
            }
        });
    } //END   
        
    //PRIVATE METHOD WAIT FOR DOWNLOAD TO COMPLETE 
    private  void waitForDownload(long initialSize) { 
        
        long currentSize = getDirectorySize(downloadPath);
        // Monitor the directory for changes
        while (currentSize == initialSize ) {
            // Wait for a certain period
            try {
                Thread.sleep(2000); // 2 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentSize = getDirectorySize(downloadPath);
        }
    } //END WAIT FOR DOWNLOAD
    
    
    //A METHOD TO GET FILE SIZE
    private static long getDirectorySize(String directoryPath) {
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) { 
            File[] files = directory.listFiles();
            int fileCount = 0;
            // COUNT FILES
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileCount++;
                    }
                }
            }
            return fileCount;
        } else {
            return -1; // RETURN -1 IF PATH IS NOT A DIRECTORY OR IF PATH DONT EXIST
        }
    } //END GET FILE SIZE
    
    
}
    
