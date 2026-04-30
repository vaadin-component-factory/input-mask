/*
 * Copyright 2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.componentfactory.demo;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for Playwright-based browser tests against the demo application.
 *
 * <p>Tests assume the demo server is reachable. By default they target
 * {@code http://localhost:8080} - start the server with {@code mvn jetty:run}
 * from the {@code vcf-input-mask-demo} module before running, or override the
 * URL with the {@code -Ddemo.baseUrl=...} system property.
 *
 * <p>If the server is not reachable when the test class is loaded, all tests
 * in the class are skipped via {@link Assumptions} so {@code mvn test} stays
 * green in environments where the server is intentionally not running (for
 * example CI builds that do not start Jetty).
 *
 * <p>Playwright downloads browser binaries on first use (around 150 MB). To
 * pre-install Chromium without running a test, use the Playwright CLI.
 */
public abstract class AbstractPlaywrightTest {

  private static final String BASE_URL_PROPERTY = "demo.baseUrl";
  private static final String DEFAULT_BASE_URL = "http://localhost:8080";
  private static final int SERVER_CHECK_TIMEOUT_MS = 2000;
  // Vaadin dev mode lazy-compiles the frontend bundle on the first request, so
  // the very first navigation can take a long time. Override per-test if needed.
  private static final double DEFAULT_NAVIGATION_TIMEOUT_MS = 120_000;

  private static Playwright playwright;
  private static Browser browser;

  protected BrowserContext context;
  protected Page page;

  @BeforeAll
  public static void launchBrowser() {
    Assumptions.assumeTrue(
        isServerRunning(),
        "Demo server is not reachable at " + baseUrl()
            + " - start it with `mvn jetty:run` from vcf-input-mask-demo, or override "
            + "with -Ddemo.baseUrl=...");
    playwright = Playwright.create();
    browser = playwright.chromium()
        .launch(new BrowserType.LaunchOptions().setHeadless(false));
  }

  @AfterAll
  public static void closeBrowser() {
    if (browser != null) {
      browser.close();
      browser = null;
    }
    if (playwright != null) {
      playwright.close();
      playwright = null;
    }
  }

  @BeforeEach
  public void createPage() {
    context = browser.newContext();
    page = context.newPage();
    page.setDefaultNavigationTimeout(DEFAULT_NAVIGATION_TIMEOUT_MS);
    page.setDefaultTimeout(DEFAULT_NAVIGATION_TIMEOUT_MS);
  }

  @AfterEach
  public void closePage() {
    if (context != null) {
      context.close();
      context = null;
      page = null;
    }
  }

  /**
   * @return base URL of the demo server, configurable via the
   *         {@code demo.baseUrl} system property.
   */
  protected static String baseUrl() {
    return System.getProperty(BASE_URL_PROPERTY, DEFAULT_BASE_URL);
  }

  private static boolean isServerRunning() {
    URI uri = URI.create(baseUrl());
    int port = uri.getPort();
    if (port == -1) {
      port = "https".equals(uri.getScheme()) ? 443 : 80;
    }
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress(uri.getHost(), port), SERVER_CHECK_TIMEOUT_MS);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
