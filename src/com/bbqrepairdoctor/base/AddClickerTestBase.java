package com.bbqrepairdoctor.base;

import com.bbqrepairdoctor.helper.EmailSender;
import com.bbqrepairdoctor.model.ClickInfo;
import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.remote.*;
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

public abstract class AddClickerTestBase {
    private static final String PLATFORM_VERSION = "6.0.1";
    private static final String serverUrl = "http://127.0.0.1:4723/wd/hub";
    protected static final Random random = new SecureRandom();
    protected static final String[] CLICK_IDS_1 = {"vs0p1c0","vs0p2c0","vs0p3c0","vs0p4c0","vs0p5c0","vs0p6c0"};
    protected static final String[] CLICK_IDS_2 = {"vs3p1c0","vs3p2c0","vs3p3c0"};
    private static final String ADD_PRESENT_KEYWORD = " Ad";
    private static final String NAME_ATTR = "name";

    protected List<String> keywordList = new ArrayList<>();
    protected Set<String> exceptionUrls = new HashSet<>();

    int CRITICAL_ANALYTICS_MASS = 300;

    @Before
    public void setUp() throws Exception{
        String line;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getKeywordsUrl()).openStream()))) {
            while ((line = reader.readLine()) != null) {
                String[] keywordAndFrequency = line.split(",");
                if (keywordAndFrequency.length > 0 && (line = keywordAndFrequency[0].trim()).length()>0) {
                    int frequency = (keywordAndFrequency.length > 1)?Integer.parseInt(keywordAndFrequency[1].trim()):1;
                    for (int i = 0; i < frequency; i++)
                        keywordList.add(line);
                }
            }
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getExceptionsUrl()).openStream()))) {
            while ((line = reader.readLine()) != null) {
                if (!(line = line.trim()).equals(""))
                    exceptionUrls.add(line);
            }
        }
        addAdditionalExceptionUrls();
    }
    private void addAdditionalExceptionUrls() {
        exceptionUrls.add("tophomeappliancerepair.com");
        exceptionUrls.add("bbqrepairdoctor.com");
    }

    @Test
    public void test(){
        while (true) {
            AndroidDriver driver = null;
            try {
                driver = createDriver();
                performBusinessLogic(driver);
                driver.quit();
            }
            catch(Throwable t){
                t.printStackTrace();
                try {
                    driver.quit();
                } catch (Throwable e) {}
            }
        }
    }

    protected void performBusinessLogic(AndroidDriver driver) throws Exception{
        changeIPAddress(driver);
        WebElement el1 = driver.findElementByAccessibilityId("More options");
        el1.click();
        try {
            el1.click();
        }
        catch(Exception e){}

        driver.findElementByAccessibilityId("New incognito tab").click();
        String keyWord = getKeyword();
        driver.findElementById("com.android.chrome:id/url_bar").sendKeys(keyWord);
        sleep(1);

        new TouchAction(driver).tap(991, 1688).perform();   //click on OK
        sleep(7);
        new TouchAction(driver).tap(921, 1695).perform();   //click on location.
        sleep(6);

        if (performUrlClick(driver, keyWord)) sleep(3);
    }

    protected void sleep(int number) throws InterruptedException{
        Thread.sleep(1000*number + random.nextInt(1000*number));
    }

    private AndroidDriver createDriver() throws Exception{
        DesiredCapabilities capabilities = DesiredCapabilities.android();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, getDeviceName());
        capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, MobileBrowserType.CHROME);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, Platform.ANDROID);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, PLATFORM_VERSION);
        capabilities.setCapability(MobileCapabilityType.ORIENTATION, ScreenOrientation.PORTRAIT);
        AndroidDriver driver = new AndroidDriver(new URL(serverUrl), capabilities);
        driver.context("NATIVE_APP");
        return driver;
    }

    protected void changeIPAddress(AndroidDriver driver) {
        driver.setConnection(Connection.AIRPLANE);
        driver.setConnection(Connection.DATA);
    }

    protected boolean performUrlClick(AndroidDriver driver, String keyWord) throws Exception{
        if (tryClickById(driver, keyWord, CLICK_IDS_1)) return true;
        if (tryClickByAd(driver, keyWord)) return true;
        if (tryClickById(driver, keyWord, CLICK_IDS_2)) return true;

        return false;
    }

    private boolean tryClickByAd(AndroidDriver driver, String keyWord) {
        if (performClick(driver, keyWord, findAdElements(driver))) return true;
        return false;
    }

    protected boolean tryClickById(AndroidDriver driver, String keyWord, String... ids) {
        for (String clickID : ids)
            if (performClick(driver, keyWord, driver.findElementsById(clickID))) return true;
        return false;
    }

    private boolean performClick(AndroidDriver driver, String keyWord, List<MobileElement> elements) {
        for (MobileElement element:elements)
            if (clickAllowed(element)) {
                (new TouchAction(driver)).press(885,1695).moveTo(element).release().perform();
                element.click();
                addAnalytics(element.getAttribute(NAME_ATTR), keyWord);
                return true;
            }
        return false;
    }

    protected boolean clickAllowed(MobileElement el){
        String attr = el.getAttribute(NAME_ATTR);
        for (String s: exceptionUrls)
            if (attr.contains(s)) return false;
        return true;
    }

    List<ClickInfo> clicks = new ArrayList<>();
    private void addAnalytics(String clickDetails, String keyWord){
        int lastIndexAdd = clickDetails.lastIndexOf(ADD_PRESENT_KEYWORD);
        if (lastIndexAdd !=-1)
            clickDetails = clickDetails.substring(lastIndexAdd+3);
        clicks.add(new ClickInfo(keyWord, clickDetails));
        if (clicks.size() == CRITICAL_ANALYTICS_MASS)
            try {
                EmailSender.sendEmail(clicks, getTitle());
                clicks.clear();
            }
            catch(Exception e){
                e.printStackTrace();
                if (clicks.size()>CRITICAL_ANALYTICS_MASS*3)
                    clicks.clear();
            }
    }

    private List<MobileElement> findAdElements(AndroidDriver driver){
        List<MobileElement> elements = driver.findElementsByXPath("//*");
        List<MobileElement> adElements = new ArrayList<>();
        for (MobileElement element: elements)
            if (element.getAttribute(NAME_ATTR).contains(ADD_PRESENT_KEYWORD))
                adElements.add(element);
        return adElements;
    }

    protected String getKeyword(){
        return keywordList.get(random.nextInt(keywordList.size()));
    }

    protected abstract String getDeviceName();
    protected abstract String getKeywordsUrl();
    protected abstract String getExceptionsUrl();
    protected abstract String getTitle();
}