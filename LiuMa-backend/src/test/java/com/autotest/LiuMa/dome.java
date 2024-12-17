package com.autotest.LiuMa;

import org.junit.Test;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Set;

public class dome {
    /**
     * 打开某个网站，停留指定秒数之后自动关闭浏览器
     * @throws InterruptedException
     */
    @Test
    public void openUrl() throws InterruptedException {
        //如果将chromedriver.exe所在的当前路径添加到了path则不需要在此设置，如果无效可以重启idea再次尝试。
        //System.setProperty("webdriver.chrome.drive","C:\\Program Files\\Google\\Chrome\\Application\\chromedriver.exe");
        //开启会话，即打开浏览器
        String url = "https://www.tapd.cn/cloud_logins/login";

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless").addArguments("--disable-blink-features=AutomationControlled").addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36");
        ChromeDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.get(url);

        chromeDriver.findElementById("username").sendKeys("15675260320");
        chromeDriver.findElementById("password_input").sendKeys("Johnson123.");
        chromeDriver.findElementById("tcloud_login_button").click();
        //程序暂停5秒
        Thread.sleep(5000);
        WebDriver.Options manage = chromeDriver.manage();
        Set<Cookie> cookies = manage.getCookies();
        for (Cookie cookie : cookies) {
            System.out.println(cookie.getName()+":"+cookie.getValue());
            if("tapdsession".equals(cookie.getName())){
//                return cookie.getName()+"="+cookie.getValue();
                System.out.println(cookie.getName()+"="+cookie.getValue());
            }
        }

        //结束会话，关闭浏览器
        chromeDriver.quit();
    }
}
