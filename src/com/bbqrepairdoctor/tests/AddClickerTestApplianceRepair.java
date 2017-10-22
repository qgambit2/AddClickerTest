package com.bbqrepairdoctor.tests;

import com.bbqrepairdoctor.base.AddClickerTestBase;


public class AddClickerTestApplianceRepair extends AddClickerTestBase {
    protected String getDeviceName(){
        return "Nexus5X_2";
    }
    protected String getKeywordsUrl(){
        return "https://tophomeappliancerepair.com/keywords.txt";
    }
    protected String getExceptionsUrl(){
        return "https://tophomeappliancerepair.com/exception_urls.txt";
    }
    protected String getTitle(){return "Appliance Repair";}
}


