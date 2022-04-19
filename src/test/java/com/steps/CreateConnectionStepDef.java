package com.steps;

import com.Pojos.Country;
import com.Pojos.GetCountriesTestContext;
import com.Pojos.SumTestContext;
import com.apirequests.GetRequest;
import com.configutility.ConfigFileReader;
import com.google.inject.Inject;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;


public class CreateConnectionStepDef {
    @Inject
    private SumTestContext sumtestContext;
    @Inject
    private GetCountriesTestContext getCountriesTestContext;

    private static final String HOST = "https://restcountries-v1.p.rapidapi.com";
    private static final String PATH = "/all";
    private Optional<Country> optionalCountry;
    public static String token;
    public ConfigFileReader configFileReader;


    public CreateConnectionStepDef() {
        configFileReader = new ConfigFileReader();
    }


    // Demo Example
    @Given("I have API headers")
    public void iHaveAPIHeaders(DataTable dataTable) {
        Map<String, String> headers = dataTable.asMap(String.class, String.class);
        getCountriesTestContext.setHeaders(headers);

    }

    @When("I get countries")
    public void iGetCountries() {
        Response respose = GetRequest.getCountries(HOST, PATH, getCountriesTestContext.getHeaders());
        System.out.println("Response --> " + respose.prettyPrint());
        getCountriesTestContext.setHttpResponse(respose);
    }

    @Then("I verify (.*) exists in the response$")
    public void iVerifyVietnamExistsInTheResponse(String countryName) {
        List<Country> countries = Arrays.asList(getCountriesTestContext.getCountries());
        optionalCountry = countries.stream().filter(country -> country.getName().equals(countryName)).findFirst();
        assertTrue("country with name " + countryName + " doesnt exist in the list of countries returned by application", optionalCountry.isPresent());
    }

    //  Sum Operation
    @Given("I have {int} and {int}")
    public void iHaveAnd(int a, int b) {
        sumtestContext.setA(a);
        sumtestContext.setB(b);

    }

    @When("I add number")
    public void iAddNumber() {
        int sum = sumtestContext.getA() + sumtestContext.getB();
        sumtestContext.setSum(sum);
    }

    @Then("I get result")
    public void iGetResult() {
        System.out.println(sumtestContext.getSum());
        Assert.assertEquals(sumtestContext.getSum(), 19);
    }

    // Connection RDBMS
    @Given("User login into DIL")
    public void userLoginIntoDIL() {
        CommonStepDef commonStepDef = new CommonStepDef();
        token = commonStepDef.authenticateUserAndFetchBearerToken();
        System.out.println("Token --> " + token);
    }

    @When("User create {string} connection")
    public void userCreateConnection(String connectionType) {
        if (connectionType.equalsIgnoreCase("SFTP")) {
            RestAssured.baseURI = configFileReader.getProperties().getProperty("BASE_URL");
            System.out.println("Base URI -->" +RestAssured.baseURI);
            RequestSpecification request = RestAssured.given();
            request.header("Content-Type", "application/json");
            request.header("Authorization", "Bearer " + token);
            JSONObject jsonObject_parent = new JSONObject();
            jsonObject_parent.put("name", "Auto_SFTP_Conn_" + CommonStepDef.getTodayDateWithSeconds());
            jsonObject_parent.put("description", "SFTP Connection");
            jsonObject_parent.put("connectionType", "SFTP");
            jsonObject_parent.put("databaseConnectionDetail", JSONObject.NULL);
            jsonObject_parent.put("kerberosConnectionDetail", JSONObject.NULL);
            jsonObject_parent.put("schemaConnectionDetailDTO", JSONObject.NULL);
            jsonObject_parent.put("sslConnectionDetailsDTO", JSONObject.NULL);
            jsonObject_parent.put("kafkaConnectionDetailDTO", JSONObject.NULL);
            jsonObject_parent.put("configurationProperties", JSONObject.NULL);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("host", "10.0.1.226");
            jsonObject.put("port", "22");
            jsonObject.put("user", "sftp");
            jsonObject.put("password", "sftp123");
            jsonObject_parent.put("sftpConnectionDetailDTO", jsonObject);

            System.out.println("j_obj --> " + jsonObject_parent);
            request.body(jsonObject_parent);
            Response response = request.post("/connections/");
            int statusCode = response.statusCode();
            System.out.println(statusCode);
            System.out.println(response.getBody().asString());
        }

    }

    @Then("Connection is created successfully")
    public void connectionIsCreatedSuccessfully() {
    }


}
