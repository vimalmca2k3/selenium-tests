package com.wikia.webdriver.PageObjectsFactory.PageObject.AdsBase.Mobile;

import com.wikia.webdriver.Common.Core.Assertion;
import com.wikia.webdriver.Common.Core.ImageUtilities.ImageComparison;
import com.wikia.webdriver.Common.Logging.PageObjectLogging;
import com.wikia.webdriver.PageObjectsFactory.PageObject.AdsBase.AdsBaseObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.AdsBase.Helpers.AdsComparison;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Bogna 'bognix' Knychala
 */
public class MobileAdsBaseObject extends AdsBaseObject {

	private final String smartBannerSelector = ".smartbanner.android";
	private AdsComparison adsComparison;
	private ImageComparison imageComparison;

	public MobileAdsBaseObject(WebDriver driver, String page) {
		super(driver, page);
		adsComparison = new AdsComparison();
		imageComparison = new ImageComparison();
		PageObjectLogging.log("", "Page screenshot", true, driver);
	}

	@Override
	protected void setWindowSize() {
		try {
			driver.manage().window().setSize(new Dimension(768, 1280));
		} catch (WebDriverException ex) {
			PageObjectLogging.log(
				"ResizeWindowForMobile",
				"Resize window method not available - possibly running on real device",
				true
			);
		}
	}

	public void verifyMobileTopLeaderboard() {
		removeSmartBanner();
		if (!checkIfSlotExpanded(presentLeaderboard)) {
			throw new NoSuchElementException(
				String.format("Slot is not expanded - ad is not there; CSS selector: %s", presentLeaderboardSelector)
			);
		}
		PageObjectLogging.log(
			"CompareScreenshot", "Page before hidding ads", true, driver
		);
		if (areAdsEmpty(presentLeaderboardSelector, presentLeaderboard)) {
			PageObjectLogging.log(
				"CompareScreenshot", "Screenshots look the same", false
			);
			throw new NoSuchElementException(
				"Screenshots of element on/off look the same."
				+ "Most probable ad is not present; CSS "
				+ presentLeaderboardSelector
			);
		} else {
			PageObjectLogging.log(
				"CompareScreenshot", "Screenshots look different", true
			);
		}
	}

	public void verifyNoAdInSlot(String slotName) {
		scrollToSlotOnMobile(slotName);
		WebElement slot = driver.findElement(By.id(slotName));
		if (!checkIfSlotExpanded(slot)) {
			if (!slot.isDisplayed()) {
				PageObjectLogging.log("AdInSlot", "Ad not found in slot as expected", true);
				return;
			} else {
				throw new NoSuchElementException("Slot is displayed, should be hidden");
			}
		}
		throw new NoSuchElementException("Slot expanded, should be collapsed");
	}

	public void verifySlotExpanded(String slotName) {
		scrollToSlotOnMobile(slotName);
		WebElement slot = driver.findElement(By.id(slotName));
		if (checkIfSlotExpanded(slot)) {
			PageObjectLogging.log("AdInSlot", "Slot expanded as expecting", true);
		} else {
			throw new NoSuchElementException("Slot is collapsed - should be expanded");
		}
	}

	public void verifyImgAdLoadedInSlot(String slotName, String expectedImg) {
		scrollToSlotOnMobile(slotName);
		WebElement slot = driver.findElement(By.id(slotName));
		if (checkIfSlotExpanded(slot)) {
			String foundImg = getSlotImageAd(slot);
			Assertion.assertEquals(foundImg, expectedImg);
		} else {
			throw new NoSuchElementException("Slot is collapsed - should be expanded");
		}
		PageObjectLogging.log("AdInSlot", "Ad found in slot", true);
	}

	private boolean areAdsEmpty(String slotSelector, WebElement slot) {
		File preSwitch = adsComparison.getMobileSlotScreenshot(slot, driver);
		adsComparison.hideSlot(slotSelector, driver);
		waitForElementNotVisibleByElement(slot);
		File postSwitch = adsComparison.getMobileSlotScreenshot(slot, driver);
		boolean imagesTheSame = imageComparison.areFilesTheSame(preSwitch, postSwitch);
		preSwitch.delete();
		postSwitch.delete();
		return imagesTheSame;
	}

	private void removeSmartBanner() {
		if (checkIfElementOnPage(smartBannerSelector)) {
			WebElement smartBanner = driver.findElement(By.cssSelector(smartBannerSelector));
			JavascriptExecutor js = (JavascriptExecutor)driver;
			js.executeScript("$(arguments[0]).css('display', 'none')", smartBanner);
			waitForElementNotVisibleByElement(smartBanner);
		}
	}

	private void scrollToSlotOnMobile(String slotName) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript(
			"var elementY = document.getElementById(arguments[0]).offsetTop;" +
			"window.scrollTo(0, elementY);",
			slotName
		);
	}

	public void waitUntilElementAppears(String selector) {
		driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeAsyncScript(
			"function setVerifySlotTimer(selector, cb) { " +
				"if (!document.querySelector(selector)){" +
					"setTimeout(function(){setVerifySlotTimer(selector, cb);}, 500);" +
				"} else { " +
					"cb(); " +
				"} " +
			"}" +
			"setVerifySlotTimer(arguments[0], arguments[arguments.length - 1]);",
			selector
		);
	}


	public void waitUntilIframeLoaded(String iframeId) {
		driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeAsyncScript(
			"var callback = arguments[arguments.length - 1]; " +
			"var iframe = document.getElementById(arguments[0]);" +
			"if (iframe.contentWindow.document.readyState === 'complete'){ return callback(); } else {" +
				"iframe.contentWindow.addEventListener('load', function () {return callback(); }) " +
			"}",
			iframeId
		);
	}


}
