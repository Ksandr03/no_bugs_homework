package com.example.teamcity.api;

import com.example.teamcity.api.models.Build;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Properties;
import com.example.teamcity.api.models.Property;
import com.example.teamcity.api.models.Step;
import com.example.teamcity.api.models.Steps;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.common.WireMock;
import io.qameta.allure.Feature;
import org.apache.http.HttpStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.teamcity.api.enums.Endpoint.BUILD_QUEUE;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

@Feature("Start build")
public class StartBuildTest extends BaseApiTest {

    private Build expectedBuild;

    @BeforeMethod
    public void setupWireMockServer() {
        expectedBuild = Build.builder()
                .state("finished")
                .status("SUCCESS")
                .buildType(testData.getBuildType())
                .build();

        WireMock.setupServer(post(BUILD_QUEUE.getUrl()), HttpStatus.SC_OK, expectedBuild);
    }

    @Test(description = "User should be able to start build with mocked response (Advanced)", groups = {"Regression"})
    public void userStartsBuildWithWireMockTest() {
        var buildTypeWithStep = BuildType.builder()
                .id(testData.getBuildType().getId())
                .name(testData.getBuildType().getName())
                .project(testData.getProject())
                .steps(Steps.builder()
                        .step(List.of(Step.builder()
                                .name("echo step")
                                .type("simpleRunner")
                                .properties(Properties.builder()
                                        .property(List.of(
                                                Property.builder()
                                                        .name("script.content")
                                                        .value("echo \"Hello, world!\"")
                                                        .build(),
                                                Property.builder()
                                                        .name("use.custom.script")
                                                        .value("true")
                                                        .build()))
                                        .build())
                                .build()))
                        .build())
                .build();

        var checkedBuildQueueRequest = new CheckedBase<Build>(Specifications.mockSpec(), BUILD_QUEUE);

        var build = checkedBuildQueueRequest.create(Build.builder()
                .buildType(buildTypeWithStep)
                .build());

        softy.assertEquals(build, expectedBuild, "Build response is not correct");
    }

    @AfterMethod(alwaysRun = true)
    public void stopWireMockServer() {
        WireMock.stopServer();
    }
}
