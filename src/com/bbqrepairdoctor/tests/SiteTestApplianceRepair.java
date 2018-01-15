package com.bbqrepairdoctor.tests;

import com.bbqrepairdoctor.base.AddClickerTestBase;
import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;

import java.util.List;

public class SiteTestApplianceRepair extends AddClickerTestBase {
    private static final String MY_HOSTNAME = "tophomeappliancerepair.com";
    private static final int MAX_COUNT = 30;

    @Override
    @Before
    public void setUp() throws Exception{
        keywordList.add("Top Home Appliance Repair");
        keywordList.add("Top home appliance repair");
        keywordList.add("top home appliance repair");
    }

    @Override
    protected boolean performUrlClick(AndroidDriver driver, String keyWord) throws Exception{
        return performUrlClick(driver, 0);
    }
    private boolean performUrlClick(AndroidDriver driver, int count) throws Exception{
        if (count == MAX_COUNT){
            return false;
        }
        List<MobileElement> elements = driver.findElementsByXPath("//*");
        for (MobileElement element: elements){
            try {
                String name = element.getAttribute("name");
                if (name.contains(MY_HOSTNAME)){
                    if (isClickable(element)){
                        try {
                            (new TouchAction(driver)).press(880 + random.nextInt(10),
                                    1689+ random.nextInt(10)).moveTo(
                                    element).release().perform();
                            element.click();
                        }
                        catch(Exception e){
                            MobileElement website = (MobileElement)driver.findElementByAccessibilityId("WEBSITE");
                            website.click();
                        }
                        sleep(3);
                        return true;
                    }
                    else{
                        return false;
                    }
                }
            }
            catch(Exception e){

            }
        }
        MobileElement nextButtonElement = findNextButtonElement(driver);
        if (nextButtonElement == null){
            System.out.println("next page button not found");
            return false;
        }
        for (int i=0;i<8;i++) {
            (new TouchAction(driver)).press(880 + random.nextInt(10),
                    1689+ random.nextInt(10)).moveTo(
                    -71-random.nextInt(10),
                    -1546-random.nextInt(10)).release().perform();
        }
        nextButtonElement.click();
        sleep(6);
        return performUrlClick(driver, count++);
    }

    private boolean isClickable(MobileElement element){
        String id = element.getId();
        for (String addId : CLICK_IDS_X1){
            if (id.endsWith(addId)){
                return false;
            }
        }
        return true;
    }

    public MobileElement findNextButtonElement(AndroidDriver driver){
        MobileElement nextPage = null;
        try {
            nextPage = (MobileElement) driver.findElementByAccessibilityId("Next page");
        }catch (Exception e){}
        if (nextPage !=null)
            return nextPage;

        try {
            nextPage = (MobileElement)driver.findElementById("pnnext");
        }catch (Exception e){}

        return nextPage;
    }

    public String getDeviceName(){
        return "Nexus5X_3";
    }
    public  String getKeywordsUrl(){
        return "https://tophomeappliancerepair.com/keywords.txt";
    }
    public String getExceptionsUrl(){
        return "https://tophomeappliancerepair.com/exception_urls.txt";
    }
    protected String getTitle(){return "Appliance Repair Site";}
}


