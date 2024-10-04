package com.fancode.api;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.RestAssured.given;

public class AssignmentTest {

    @Test
    public void validateUserTodos() {
        RestAssured.baseURI = "http://jsonplaceholder.typicode.com";

        Response usersResponse = given()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> users = usersResponse.jsonPath().getList("");

        List<Map<String, Object>> filteredUsers = filterUsersByLocation(users);

        processUsersTodos(filteredUsers);
    }

    private List<Map<String, Object>> filterUsersByLocation(List<Map<String, Object>> users) {
        List<Map<String, Object>> filteredUsers = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Map<String, Object> address = (Map<String, Object>) user.get("address");
            Map<String, Object> geo = (Map<String, Object>) address.get("geo");
            double latitude = Double.parseDouble(geo.get("lat").toString());
            double longitude = Double.parseDouble(geo.get("lng").toString());
            if (latitude > -40 && latitude < 5 && longitude > 5 && longitude < 100) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }

    private void processUsersTodos(List<Map<String, Object>> users) {
        for (Map<String, Object> user : users) {
            int userId = (int) user.get("id");

            Response todosResponse = given()
                    .queryParam("userId", userId)
                    .when()
                    .get("/todos")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            List<Map<String, Object>> todos = todosResponse.jsonPath().getList("");

            // Count total and completed tasks
            long totalTasks = todos.size();
            long completedTasks = 0;
            for (Map<String, Object> todo : todos) {
                if ((boolean) todo.get("completed")) {
                    completedTasks++;
                }
            }

            double completionPercentage = (completedTasks * 100.0) / totalTasks;

            Assert.assertTrue(completionPercentage > 50,
                    "User " + userId + " has less than 50% tasks completed: " + completionPercentage + "%");
        }
    }
}