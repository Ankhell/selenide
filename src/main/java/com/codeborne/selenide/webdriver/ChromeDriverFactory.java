package com.codeborne.selenide.webdriver;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.List;
import java.util.Map;

import static org.openqa.selenium.remote.CapabilityType.*;
import static org.openqa.selenium.remote.CapabilityType.SUPPORTS_ALERTS;

@ParametersAreNonnullByDefault
public class ChromeDriverFactory extends AbstractChromiumDriverFactory {
  private static final Logger log = LoggerFactory.getLogger(ChromeDriverFactory.class);

  @Override
  public void setupWebdriverBinary() {
    if (isSystemPropertyNotSet("webdriver.chrome.driver")) {
      WebDriverManager.chromedriver().setup();
    }
  }

  @Override
  @CheckReturnValue
  @Nonnull
  @SuppressWarnings("deprecation")
  public WebDriver create(Config config, Browser browser, @Nullable Proxy proxy, @Nullable File browserDownloadsFolder) {
    MutableCapabilities chromeOptions = createCapabilities(config, browser, proxy, browserDownloadsFolder);
    log.debug("Chrome options: {}", chromeOptions);
    return new ChromeDriver(buildService(config), chromeOptions);
  }

  @CheckReturnValue
  @Nonnull
  protected ChromeDriverService buildService(Config config) {
    return withLog(config, new ChromeDriverService.Builder());
  }

  @Override
  @CheckReturnValue
  @Nonnull
  public MutableCapabilities createCapabilities(Config config, Browser browser,
                                                @Nullable Proxy proxy, @Nullable File browserDownloadsFolder) {
    ChromeOptions chromeOptions = createCommonCapabilities(config, browser, proxy);

    chromeOptions.setHeadless(config.headless());
    if (!config.browserBinary().isEmpty()) {
      log.info("Using browser binary: {}", config.browserBinary());
      chromeOptions.setBinary(config.browserBinary());
    }
    chromeOptions.addArguments(createChromeArguments(config, browser));
    chromeOptions.setExperimentalOption("excludeSwitches", excludeSwitches(chromeOptions));
    chromeOptions.setExperimentalOption("prefs", prefs(chromeOptions, browserDownloadsFolder, System.getProperty("chromeoptions.prefs", "")));
    setMobileEmulation(chromeOptions);

    return chromeOptions;
  }

  @CheckReturnValue
  @Nonnull
  protected List<String> createChromeArguments(Config config, Browser browser) {
    return createChromiumArguments(config, System.getProperty("chromeoptions.args"));
  }

  @CheckReturnValue
  @Nonnull
  protected String[] excludeSwitches(Capabilities capabilities) {
    return hasExtensions(capabilities) ?
      new String[]{"enable-automation"} :
      new String[]{"enable-automation", "load-extension"};
  }

  private boolean hasExtensions(Capabilities capabilities) {
    Map<?, ?> chromeOptions = (Map<?, ?>) capabilities.getCapability("goog:chromeOptions");
    if (chromeOptions == null) return false;

    List<?> extensions = (List<?>) chromeOptions.get("extensions");
    return extensions != null && !extensions.isEmpty();
  }

  private void setMobileEmulation(ChromeOptions chromeOptions) {
    Map<String, Object> mobileEmulation = mobileEmulation();
    if (!mobileEmulation.isEmpty()) {
      chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
    }
  }

  @CheckReturnValue
  @Nonnull
  protected Map<String, Object> mobileEmulation() {
    String mobileEmulation = System.getProperty("chromeoptions.mobileEmulation", "");
    return parsePreferencesFromString(mobileEmulation);
  }

  @Nonnull
  @Override
  protected ChromeOptions createCommonCapabilities(
    Config config, Browser browser, @Nullable Proxy proxy
  ) {
    ChromeOptions chromeOptions;
    if (config.browserCapabilities() instanceof ChromeOptions) {
      chromeOptions = (ChromeOptions)config.browserCapabilities();
    } else {
      chromeOptions = new ChromeOptions();
    }
    if (proxy != null) {
      chromeOptions.setCapability(PROXY, proxy);
    }
    if (config.browserVersion() != null && !config.browserVersion().isEmpty()) {
      chromeOptions.setCapability(BROWSER_VERSION, config.browserVersion());
    }
    chromeOptions.setCapability(PAGE_LOAD_STRATEGY, config.pageLoadStrategy());
    chromeOptions.setCapability(ACCEPT_SSL_CERTS, true);

    if (browser.supportsInsecureCerts()) {
      chromeOptions.setCapability(ACCEPT_INSECURE_CERTS, true);
    }
    chromeOptions.setCapability(SUPPORTS_JAVASCRIPT, true);
    chromeOptions.setCapability(TAKES_SCREENSHOT, true);
    chromeOptions.setCapability(SUPPORTS_ALERTS, true);

    transferCapabilitiesFromSystemProperties(chromeOptions);

    if (config.browserCapabilities() instanceof ChromeOptions) {
      return chromeOptions;
    } else {
      return chromeOptions.merge(config.browserCapabilities());
    }
  }

  protected void transferCapabilitiesFromSystemProperties(ChromeOptions currentBrowserCapabilities) {
    String prefix = "capabilities.";
    for (String key : System.getProperties().stringPropertyNames()) {
      if (key.startsWith(prefix)) {
        String capability = key.substring(prefix.length());
        String value = System.getProperties().getProperty(key);
        log.debug("Use {}={}", key, value);
        currentBrowserCapabilities.setCapability(capability, convertStringToNearestObjectType(value));
      }
    }
  }
}
