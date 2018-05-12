package com.bbqrepairdoctor.base;

import com.bbqrepairdoctor.helper.EmailSender;
import com.bbqrepairdoctor.model.ClickInfo;
import io.appium.java_client.*;
import io.appium.java_client.android.*;
import io.appium.java_client.remote.*;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.AndroidServerFlag;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.appium.java_client.service.local.flags.ServerArgument;
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;

public abstract class AddClickerTestBase {
    private static final String PLATFORM_VERSION = "6.0.1";

    protected static final Random random = new SecureRandom();

    protected static final String[] CLICK_IDS_X1 = {"0p1c0", "0p2c0", "0p3c0", "0p4c0"};
    protected static final String[] CLICK_IDS_X2 = {"3p1c0", "3p2c0", "3p3c0", "3p4c0"};

    private static final String ADD_PRESENT_KEYWORD = " Ad";
    private static final String NAME_ATTR = "name";

    protected List<String> keywordList = new ArrayList<>();
    protected Set<String> exceptionUrls = new HashSet<>();

    int CRITICAL_ANALYTICS_MASS = 300;

    private static final String TOPHOME_HOSTNAME = "tophomeappliancerepair.com";
    private static final String BBQREPAIR_HOSTNAME = "bbqrepairdoctor.com";

    @Before
    public void setUp() throws Exception{
        refreshKeywordsAndUrls();
        resetAppium();
    }
    private void addAdditionalExceptionUrls(Collection urls) {
        urls.add("tophomeappliancerepair.com");
        urls.add("bbqrepairdoctor.com");
    }

    @Test
    public void test(){
        long adbTime = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        while (true) {
            AndroidDriver driver = null;
            try {
                long now = System.currentTimeMillis();
                if (now - adbTime > 3600000*8 ){
                    if ("true".equals(System.getenv("RESET.ADB"))){
                        resetAdb();
                        adbTime = now;
                    }
                }

                if (now - time > 3600000 ){
                    resetAppium();
                    try{
                        refreshKeywordsAndUrls();
                    }catch (Exception e){};
                    time = System.currentTimeMillis();
                }
                driver = createDriver();
                performBusinessLogic(driver);
                driver.quit();
            }
            catch(Throwable t){
                t.printStackTrace();
                try {
                    driver.quit();
                } catch (Throwable e) {}
                if (t.getCause()!=null && t.getCause() instanceof ConnectException){
                    resetAppium();
                }
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

        int rand = random.nextInt(3);
        if (rand == 0){
            sleep(1);
            el1 = driver.findElementByAccessibilityId("More options");
            el1.click();
            try {
                el1.click();
            }
            catch(Exception e){}

            sleep(1);

            MobileElement el8 = (MobileElement) driver.findElementById("com.android.chrome:id/checkbox");
            el8.click();
            sleep(1);
        }

        String keyWord = getKeyword();
        driver.findElementById("com.android.chrome:id/url_bar").sendKeys(keyWord);
        sleep(1);

        new TouchAction(driver).tap(991, 1688).perform();   //click on OK
        sleep(10);
        (new TouchAction(driver)).tap(902, 1040).perform();

        sleep(8);

        if (performUrlClick(driver, keyWord) || performSiteClick(driver, keyWord))
            sleep(6);
    }

    protected void sleep(int number) throws InterruptedException{
        Thread.sleep(1000*number + random.nextInt(1000*number));
    }

    private AndroidDriver createDriver() throws Exception{
        String systemPlatformVersion = System.getenv("PLATFORM_VERSION");
        String systemUDID = System.getenv("UDID");
        String systemDeviceName = System.getenv("DEVICE_NAME");

        DesiredCapabilities capabilities = DesiredCapabilities.android();
        if (systemDeviceName!=null) {
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, systemDeviceName);
        }
        else{
            capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, getDeviceName());
        }
        capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, MobileBrowserType.CHROME);
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, Platform.ANDROID);
        if (systemPlatformVersion!=null){
            capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, systemPlatformVersion);
        }
        else {
            capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, PLATFORM_VERSION);
        }
        if (systemUDID!=null){
            capabilities.setCapability(MobileCapabilityType.UDID, systemUDID);
        }

        String appiumPort = System.getenv("appium.port");
        if (appiumPort == null){
            appiumPort = "4723";
        }
        String serverUrl = "http://127.0.0.1:"+appiumPort+"/wd/hub";

        capabilities.setCapability(MobileCapabilityType.ORIENTATION, ScreenOrientation.PORTRAIT);
        AndroidDriver driver = new AndroidDriver(new URL(serverUrl), capabilities);
        driver.context("NATIVE_APP");
        return driver;
    }

    protected void changeIPAddress(AndroidDriver driver) throws InterruptedException{
        driver.setConnection(Connection.WIFI);
        sleep(1);
        driver.setConnection(Connection.DATA);
        sleep(1);
    }

    protected boolean performUrlClick(AndroidDriver driver, String keyWord) throws Exception{
        if (tryClickById(driver, keyWord, CLICK_IDS_X1)) return true;
        if (tryClickByAd(driver, keyWord)) return true;
        if (tryClickById(driver, keyWord, CLICK_IDS_X2)) return true;

        return false;
    }

    private boolean tryClickByAd(AndroidDriver driver, String keyWord) throws InterruptedException{
        if (performClick(driver, keyWord, findAdElements(driver))) return true;
        return false;
    }

    protected boolean tryClickById(AndroidDriver driver, String keyWord, String... ids) throws InterruptedException{
        for (String id : ids) {
            List<MobileElement> elements =
                    driver.findElementsByXPath(
                            "//*[substring(@resource-id,string-length(@resource-id)-string-length('"+id+"') +1) = '"+id+"']");
            if (performClick(driver, keyWord,elements)) return true;
        }
        return false;
    }

    private boolean performClick(AndroidDriver driver, String keyWord, List<MobileElement> elements)
                    throws InterruptedException{
        for (MobileElement element:elements)
            if (clickAllowed(element)) {
                performClickOnElement(driver, keyWord, element);
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
        if (clicks.size() >= CRITICAL_ANALYTICS_MASS)
            try {
                EmailSender.sendEmail(clicks, getTitle());
                clicks.clear();
            }
            catch(Exception e){
                e.printStackTrace();
                if (clicks.size()>CRITICAL_ANALYTICS_MASS*30)
                    clicks.clear();
            }
    }

    private List<MobileElement> findAdElements(AndroidDriver driver){
        List<MobileElement> adElements = new ArrayList<>();
        for (MobileElement element:  (List<MobileElement>)driver.findElementsByXPath("//*"))
            if (element.getAttribute(NAME_ATTR).contains(ADD_PRESENT_KEYWORD))
                adElements.add(element);
        return adElements;
    }

    protected String getKeyword(){
        return keywordList.get(random.nextInt(keywordList.size()));
    }

    protected boolean performSiteClick(AndroidDriver driver, String keyWord) throws Exception{
        List<MobileElement> elements = driver.findElementsByXPath("//*");
        for (MobileElement element: elements){
            String name = element.getAttribute(NAME_ATTR);
            if (name.contains(TOPHOME_HOSTNAME) || name.contains(BBQREPAIR_HOSTNAME) && !isAnAd(element)){
                performClickOnElement(driver, keyWord, element);
                return true;
            }
        }
        return false;
    }

    private void performClickOnElement(AndroidDriver driver, String keyWord, MobileElement element) throws InterruptedException {
        if (!element.isDisplayed()) {
            new TouchAction(driver).press(885,1695).moveTo(element).release().perform();
            sleep(1);
        }
        element.click();
        addAnalytics(element.getAttribute(NAME_ATTR), keyWord);
    }

    private boolean isAnAd(MobileElement element){
        String id = element.getId();
        List<String> ids = new ArrayList<>();
        ids.addAll(Arrays.asList(CLICK_IDS_X1));
        ids.addAll(Arrays.asList(CLICK_IDS_X2));
        for (String addId : ids){
            if (id.endsWith(addId)){
                return true;
            }
        }
        return false;
    }

    private void resetAppium(){
        if("false".equals(System.getenv("reset.appium"))){
            return;
        }
        try{
            String os = System.getProperty("os.name");
            try{
                if (service !=null) service.stop();
            }catch (Exception e){}
            if (os!=null && os.toLowerCase().contains("windows")){
                stopAppiumWindows();
                startAppiumWindows();
            }
            else {
                stopAppiumLinux();
                startAppiumLinux();
            }

        }
        catch (Throwable t){
            t.printStackTrace();
        }
    }

    void startAppiumWindows() throws Exception{
        Process p = Runtime.getRuntime().exec(new String[]{
                "where", "appium"
        });

        List<String> locations = new ArrayList<>();

        String line = null;
        BufferedReader input = new BufferedReader
                (new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            locations.add(line);
        }
        input.close();

        Exception ex = new RuntimeException("Appium not found");
        boolean success = false;
        for (String location:locations){
            try{
                p = Runtime.getRuntime().exec(new String[]{
                        location
                });
                success = true;
                final BufferedReader input2 = new BufferedReader
                                (new InputStreamReader(p.getInputStream()));
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String line = null;
                        try {
                            while ((line = input2.readLine()) != null) {
                                System.out.println(line);
                            }
                        }
                        catch (Exception e){}

                    }
                });
                t.start();
                break;
            }
            catch (Exception e){
                ex = e;
            }
        }
        if (!success){
            throw ex;
        }
        Thread.sleep(30000l);
    }

    void startAppiumLinux() throws Exception{
        String appiumPort = System.getenv("appium.port");
        String udid = System.getenv("UDID");
        String tmpDir = System.getenv("TMP_DIR");
        if (appiumPort == null){
            appiumPort = "4723";
        }
        String bPort = String.valueOf(Integer.parseInt(appiumPort)+2);
        String cPort = String.valueOf(Integer.parseInt(appiumPort)+4);
        String selendroidPort = String.valueOf(Integer.parseInt(appiumPort)+6);
        String systemPort = String.valueOf(Integer.parseInt(appiumPort)+8);

        String command = "appium -p "+appiumPort+" -bp "+bPort+" -U "+udid+" --chromedriver-port "+cPort
                +" --session-override --tmp "+tmpDir +" --selendroid-port " + selendroidPort;

        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.withIPAddress("127.0.0.1");
        builder.withLogFile(new File(tmpDir+"/"+appiumPort+".log"));
        builder.usingPort(Integer.parseInt(appiumPort));
        builder.withArgument(GeneralServerFlag.TEMP_DIRECTORY, tmpDir);
        builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
        builder.withArgument(AndroidServerFlag.BOOTSTRAP_PORT_NUMBER, bPort);
        builder.withArgument(AndroidServerFlag.CHROME_DRIVER_PORT, cPort);
        builder.withArgument(AndroidServerFlag.SUPPRESS_ADB_KILL_SERVER);//, "true");

        DesiredCapabilities caps = DesiredCapabilities.android();
        caps.setCapability("noReset", "false");
        caps.setCapability(MobileCapabilityType.UDID, udid);
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Appium");
        caps.setCapability(MobileCapabilityType.BROWSER_NAME, "chrome");
        caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, System.getenv("PLATFORM_VERSION"));
        caps.setCapability("version", System.getenv("PLATFORM_VERSION"));
        caps.setCapability("platform", System.getenv("ANDROID"));
        caps.setCapability("systemPort", systemPort);

        builder.withCapabilities(caps);
        service = AppiumDriverLocalService.buildService(builder);
        service.start();
        //Runtime.getRuntime().exec(command);
        Thread.sleep(10000l);
    }
    AppiumDriverLocalService service = null;

    private void stopAppiumWindows() throws IOException{
        Process p = Runtime.getRuntime().exec(new String[]{
               "taskkill", "/IM", "node.exe", "/F"
        });

        String line = null;
        BufferedReader input = new BufferedReader
                (new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
           //
        }
        input.close();

    }

    private void stopAppiumLinux() throws IOException {
        String line;
        List<String> pids = new ArrayList<>();

        String appiumPort = System.getenv("appium.port");
        if (appiumPort == null){
            appiumPort = "4723";
        }
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c",
                "ps -ef | grep 'appium -p "+appiumPort+"' | awk '{print $2}'"});
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            pids.add(line);
        }
        input.close();

        for (int i=0;i<pids.size();i++){
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "kill -9 "+pids.get(i)});
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                pids.add(line);
            }
            input.close();
        }
    }

    private void resetAdb() throws IOException{
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c",
                "adb kill-server"});
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((input.readLine()) != null) {
        }
        input.close();

        p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c",
                "adb start-server"});
        input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((input.readLine()) != null) {
        }
        input.close();
    }

    protected abstract String getDeviceName();
    protected abstract String getKeywordsUrl();
    protected abstract String getExceptionsUrl();
    protected abstract String getTitle();


    public void refreshKeywordsAndUrls() throws Exception{
        String line;
        List<String> localKeywordList = new ArrayList<>();
        Set<String> localExceptionUrls = new HashSet<>();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getKeywordsUrl()).openStream()))) {
            while ((line = reader.readLine()) != null) {
                String[] keywordAndFrequency = line.split(",");
                if (keywordAndFrequency.length > 0 && (line = keywordAndFrequency[0].trim()).length()>0) {
                    int frequency = (keywordAndFrequency.length > 1)?Integer.parseInt(keywordAndFrequency[1].trim()):1;
                    for (int i = 0; i < frequency; i++)
                        localKeywordList.add(line);
                }
            }
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(getExceptionsUrl()).openStream()))) {
            while ((line = reader.readLine()) != null) {
                if (!(line = line.trim()).equals(""))
                    localExceptionUrls.add(line);
            }
        }
        addAdditionalExceptionUrls(localExceptionUrls);
        this.keywordList = localKeywordList;
        this.exceptionUrls = localExceptionUrls;
    }
}
