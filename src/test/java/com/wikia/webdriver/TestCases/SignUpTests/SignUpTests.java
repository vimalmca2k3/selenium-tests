/**
 *
 */
package com.wikia.webdriver.TestCases.SignUpTests;

import java.io.File;
import java.util.Calendar;

import org.testng.annotations.Test;

import com.wikia.webdriver.Common.ContentPatterns.PageContent;
import com.wikia.webdriver.Common.Properties.Credentials;
import com.wikia.webdriver.Common.Templates.NewTestTemplate;
import com.wikia.webdriver.PageObjectsFactory.ComponentObject.Toolbars.CustomizedToolbarComponentObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.SignUp.AlmostTherePageObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.SignUp.ConfirmationPageObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.SignUp.SignUpPageObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.SignUp.UserProfilePageObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.Special.Preferences.PreferencesPageObject;
import com.wikia.webdriver.PageObjectsFactory.PageObject.Special.Preferences.PreferencesPageObject.tabNames;

/**
 * @author Karol 'kkarolk' Kujawiak
 *
 * 1. Sign up without/wrong blurry word,
 * 2. Sign up of too young user,
 * 3. Sign up with existing user name,
 * 4. Sign up with users from data provider.
 * 5. Sign up drop-down from from article,
 */
public class SignUpTests extends NewTestTemplate {

	Credentials credentials = config.getCredentials();
	File captchaFile = config.getCaptchaFile();

	@Test(groups = {"SignUp_001", "SignUp"})
	public void Signup_001_wrongBlurryWord() {
		SignUpPageObject signUp = new SignUpPageObject(driver, wikiURL);
		signUp.typeUserName(signUp.getTimeStamp());
		signUp.typeEmail(credentials.email);
		signUp.typePassword(signUp.getTimeStamp());
		signUp.enterBirthDate(
				PageContent.wikiSignUpBirthMonth,
				PageContent.wikiSignUpBirthDay,
				PageContent.wikiSignUpBirthYear
		);
		signUp.typeCaptcha(signUp.getTimeStamp());
		signUp.submit();
		signUp.verifyCaptchaInvalidMessage();
		signUp.verifySubmitButtonDisabled();
	}

	@Test(groups = {"SignUp_002", "SignUp"})
	public void Signup_002_tooYoungUser() {
		SignUpPageObject signUp = new SignUpPageObject(driver, wikiURL);
		signUp.typeUserName(signUp.getTimeStamp());
		signUp.typeEmail(credentials.email);
		signUp.typePassword(signUp.getTimeStamp());
		Calendar currentDate = Calendar.getInstance();
		signUp.enterBirthDate(
				// +1 because months are numerated from 0
				Integer.toString(currentDate.get(Calendar.MONTH) + 1),
				Integer.toString(currentDate.get(Calendar.DAY_OF_MONTH)),
				Integer.toString(currentDate.get(Calendar.YEAR)- PageContent.MIN_AGE)
		);
		signUp.verifyTooYoungMessage();
		signUp.verifySubmitButtonDisabled();
	}

	@Test(groups = {"SignUp_003", "SignUp"})
	public void Signup_003_existingUserName() {
		SignUpPageObject signUp = new SignUpPageObject(driver, wikiURL);
		signUp.typeUserName(credentials.userName);
		signUp.verifyUserExistsMessage();
		signUp.verifySubmitButtonDisabled();
	}

	@Test(groups = {"SignUp_account_creation_TC_001", "SignUp", "Smoke4"})
	public void Signup_004_signup() {
		SignUpPageObject signUp = new SignUpPageObject(driver, wikiURL);

		String userName = "user" + signUp.getTimeStamp();
		String password = "pass" + signUp.getTimeStamp();
		String email = credentials.emailQaart2;
		String emailPassword = credentials.emailPasswordQaart2;

		signUp.typeEmail(email);
		signUp.typeUserName(userName);
		signUp.typePassword(password);
		signUp.enterBirthDate(
			PageContent.wikiSignUpBirthMonth,
			PageContent.wikiSignUpBirthDay,
			PageContent.wikiSignUpBirthYear
		);
		signUp.typeCaptcha(captchaFile);
		AlmostTherePageObject almostTherePage = signUp.submit(email, emailPassword);
		almostTherePage.verifyAlmostTherePage();
		ConfirmationPageObject confirmPageAlmostThere = almostTherePage.enterActivationLink(email, emailPassword);
		confirmPageAlmostThere.typeInUserName(userName);
		confirmPageAlmostThere.typeInPassword(password);
		UserProfilePageObject userProfile = confirmPageAlmostThere.clickSubmitButton(email, emailPassword);
		userProfile.verifyUserLoggedIn(userName);
		CustomizedToolbarComponentObject toolbar = new CustomizedToolbarComponentObject(driver);
		toolbar.verifyUserToolBar();
		userProfile.verifyWelcomeEmail(userName, email, emailPassword);
		PreferencesPageObject preferences = userProfile.openSpecialPreferencesPage(wikiURL);
		preferences.selectTab(tabNames.Email);
		preferences.verifyEmailMeSection();
	}
}
