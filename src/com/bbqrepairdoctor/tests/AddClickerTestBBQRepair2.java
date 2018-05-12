package com.bbqrepairdoctor.tests;

import com.bbqrepairdoctor.base.AddClickerTestBase;


public class AddClickerTestBBQRepair2 extends AddClickerTestBase {
    protected String getDeviceName(){
        return "Nexus5X_1";
    }
    protected  String getKeywordsUrl(){
        return "https://bbqrepairdoctor.com/keywords.txt";
    }
    protected String getExceptionsUrl(){
        return "https://bbqrepairdoctor.com/exception_urls.txt";
    }
    protected String getTitle(){return "BBQ Repair";}
}


