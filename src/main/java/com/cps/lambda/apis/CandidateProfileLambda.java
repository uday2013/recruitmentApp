package com.cps.lambda.apis;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;

import com.google.gson.Gson;
import com.cps.lambda.apis.model.Candidate;


public class CandidateProfileLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    //Local Set Up
    //static final BasicAWSCredentials credentials = new BasicAWSCredentials("AKIATT3J4I6PFJCASMFA", "qUNVfrF+vXerR0fX7zR05yqGyX13k6tuOU8fEvFY");
   /* private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
           .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://dynamo-local:8000", "us-east-1"))
           .build();*/


    private static final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client);

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        String httpMethod = input.getHttpMethod();
        String output;
        int statusCode;


        switch (httpMethod) {
            case "GET":
                System.out.println("Inside Get Method ");
                output = getCandidate();
                System.out.println("Output " + output);
                statusCode = 200;
                break;
            case "POST":
                output = createCandidate(input.getBody());
                statusCode = 201;
                break;
            case "PUT":
                output = updateCandidate(input.getBody());
                statusCode = 200;
                break;
            case "DELETE":
                output = deleteCandidate(input.getBody());
                statusCode = 200;
                break;
            default:
                output = "Invalid HTTP Method";
                statusCode = 400;
                break;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(output);

        return response;
    }

    private String getCandidate() {
        System.out.println("Inside getCandidate :: ");
        List<Candidate> candidates = dynamoDBMapper.scan(Candidate.class, new DynamoDBScanExpression());
        return new Gson().toJson(candidates);
    }

    private String createCandidate(String requestBody) {
        Candidate candidate = new Gson().fromJson(requestBody, Candidate.class);
        dynamoDBMapper.save(candidate);
        return "Candidate created: " + candidate.getCandidateId();
    }

    private String updateCandidate(String requestBody) {
        Candidate updateCandidate = new Gson().fromJson(requestBody, Candidate.class);
        Candidate existingCandidate = dynamoDBMapper.load(Candidate.class, updateCandidate.getCandidateId());
        if (existingCandidate != null) {
            existingCandidate.setCandidateId(updateCandidate.getCandidateId());
            existingCandidate.setFirstName(updateCandidate.getFirstName());
            existingCandidate.setLastName(updateCandidate.getLastName());
            existingCandidate.setEmail(updateCandidate.getEmail());
            existingCandidate.setYearsOfExp(updateCandidate.getYearsOfExp());
            existingCandidate.setCity(updateCandidate.getCity());
            existingCandidate.setState(updateCandidate.getState());
            existingCandidate.setVisaType(updateCandidate.getVisaType());
            dynamoDBMapper.save(existingCandidate);
            return "Candidate updated: " + existingCandidate.getCandidateId();
        } else {
            return "Candidate not found";
        }
    }

    private String deleteCandidate(String requestBody) {
        Candidate candidateToDelete = new Gson().fromJson(requestBody, Candidate.class);
        Candidate existingUser = dynamoDBMapper.load(Candidate.class, candidateToDelete.getCandidateId());
        if (existingUser != null) {
            dynamoDBMapper.delete(existingUser);
            return "Candidate deleted: " + existingUser.getCandidateId();
        } else {
            return "Candidate not found";
        }
    }
}

// import java.util.HashMap;
// import java.util.Map;

// import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
// import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
// import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
// import com.amazonaws.services.lambda.runtime.Context;
// import com.amazonaws.services.lambda.runtime.RequestHandler;
// import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
// import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

// public class App1 implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//     public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {

//         AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
//         DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client);

//         Map<String, String> headers = new HashMap<>();
//         headers.put("Content-Type", "application/json");
//         headers.put("X-Custom-Header", "application/json");
//         APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent().withHeaders(headers);
//             String output = String.format("{ \"message\": \"hello world\", \"location\": \"%s\" }", "pageContents");

//             User user = new User();
//             user.setEmpId("EMP001");
//             user.setName("John Doe");
//             user.setEmail("johndoe@example.com");

//             dynamoDBMapper.save(user);
//             context.getLogger().log("Successfully added new Employee");

//             return response.withStatusCode(200).withBody(output);
//     }
// }
