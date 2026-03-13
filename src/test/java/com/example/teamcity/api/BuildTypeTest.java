package com.example.teamcity.api;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.Role;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.api.spec.ValidationResponseSpecifications;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static com.example.teamcity.api.enums.Endpoint.BUILD_TYPES;
import static com.example.teamcity.api.enums.Endpoint.PROJECTS;
import static com.example.teamcity.api.enums.Endpoint.USERS;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {

    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());

        softy.assertEquals(createdBuildType, testData.getBuildType(), "Build type is not correct");
    }

    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                .create(buildTypeWithSameId)
                .then().spec(ValidationResponseSpecifications
                        .checkBuildTypeWithIdAlreadyExist(testData.getBuildType().getId()));
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        step("Create project by super user");
        superUserCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        step("Set PROJECT_ADMIN role for user on this project and create user");
        testData.getUser().setRoles(Roles.builder()
                .role(List.of(Role.builder()
                        .roleId("PROJECT_ADMIN")
                        .scope("p:" + testData.getProject().getId())
                        .build()))
                .build());
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        step("Create buildType for project by user (PROJECT_ADMIN)");
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));
        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        step("Check buildType was created successfully");
        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read(testData.getBuildType().getId());
        softy.assertEquals(createdBuildType, testData.getBuildType(), "Build type is not correct");
    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
        step("Create project1 and project2 by super user");
        superUserCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        var testData2 = generate();
        superUserCheckRequests.<Project>getRequest(PROJECTS).create(testData2.getProject());

        step("Create user1 with PROJECT_ADMIN role in project1");
        testData.getUser().setRoles(Roles.builder()
                .role(List.of(Role.builder()
                        .roleId("PROJECT_ADMIN")
                        .scope("p:" + testData.getProject().getId())
                        .build()))
                .build());
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());

        step("Create user2 with PROJECT_ADMIN role in project2");
        testData2.getUser().setRoles(Roles.builder()
                .role(List.of(Role.builder()
                        .roleId("PROJECT_ADMIN")
                        .scope("p:" + testData2.getProject().getId())
                        .build()))
                .build());
        superUserCheckRequests.getRequest(USERS).create(testData2.getUser());

        step("Create buildType for project1 by user2 — should be forbidden");
        new UncheckedBase(Specifications.authSpec(testData2.getUser()), BUILD_TYPES)
                .create(testData.getBuildType())
                .then().spec(ValidationResponseSpecifications.checkForbidden());
    }
}
