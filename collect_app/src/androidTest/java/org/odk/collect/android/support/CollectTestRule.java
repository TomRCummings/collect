package org.odk.collect.android.support;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.odk.collect.android.activities.SplashScreenActivity;
import org.odk.collect.android.external.AndroidShortcutsActivity;
import org.odk.collect.android.support.pages.FirstLaunchPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.Page;
import org.odk.collect.android.support.pages.ShortcutsPage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollectTestRule implements TestRule {

    public static boolean projectCreated;

    private final boolean useDemoProject;
    private String formFilename;
    private boolean copyToDatabase;
    private List<String> mediaFilePaths;

    public CollectTestRule() {
        this(true);
    }

    public CollectTestRule(String formFilename, boolean copyToDatabase) {
        this(true);
        this.formFilename = formFilename;
        this.copyToDatabase = copyToDatabase;
        this.mediaFilePaths = new ArrayList<>();
    }

    public CollectTestRule(String fileName, List<String> mediaFilePaths, boolean copyToDatabase) {
        this(fileName, copyToDatabase);
        this.mediaFilePaths = mediaFilePaths;
    }

    public CollectTestRule(boolean skipLaunchScreen) {
        this.useDemoProject = skipLaunchScreen;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ActivityScenario.launch(SplashScreenActivity.class);

                if (projectCreated) {
                    new MainMenuPage().assertOnPage();
                } else {
                    FirstLaunchPage firstLaunchPage = new FirstLaunchPage().assertOnPage();

                    if (useDemoProject) {
                        firstLaunchPage.clickTryCollect();
                    }
                }

                if (formFilename != null) {
                    new MainMenuPage()
                            .copyForm(formFilename, mediaFilePaths, copyToDatabase);
                }

                base.evaluate();
            }
        };
    }

    public MainMenuPage startAtMainMenu() {
        return new MainMenuPage();
    }

    public FirstLaunchPage startAtFirstLaunch() {
        return new FirstLaunchPage();
    }

    public MainMenuPage withProject(String serverUrl) {
        return startAtFirstLaunch()
                .clickManuallyEnterProjectDetails()
                .inputUrl(serverUrl)
                .addProject();
    }

    public ShortcutsPage launchShortcuts() {
        ActivityScenario<AndroidShortcutsActivity> scenario = ActivityScenario.launch(AndroidShortcutsActivity.class);
        return new ShortcutsPage(scenario).assertOnPage();
    }

    public <T extends Page<T>> T launch(Intent intent, T destination) {
        ActivityScenario.launch(intent);
        return destination.assertOnPage();
    }

    public <T extends Page<T>> Instrumentation.ActivityResult launchForResult(Intent intent, T destination, Consumer<T> actions) {
        ActivityScenario<Activity> scenario = ActivityScenario.launch(intent);
        destination.assertOnPage();
        actions.accept(destination);
        return scenario.getResult();
    }
}
