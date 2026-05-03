package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;

public class Forgot {

    // View Components
    private final TextBox userIDBox;
    private final Button submitID;
    private final Label questionLabel;
    private final PasswordTextBox answerBox;
    private final Button submitAnswer;
    private final Label serverUpdates;

    // Initialize Forgot Page
    public Forgot() {
        userIDBox = new TextBox();
        submitID = new Button("Next");

        questionLabel = new Label();
        answerBox = new PasswordTextBox();
        submitAnswer = new Button("Submit");

        serverUpdates = new Label();

        answerBox.setEnabled(false);
        submitAnswer.setEnabled(false);

        // Inject DOM Elements
        RootPanel.get("userIDContainer").add(userIDBox);
        RootPanel.get("submitID").add(submitID);
        RootPanel.get("questionContainer").add(questionLabel);
        RootPanel.get("answerContainer").add(answerBox);
        RootPanel.get("submitAnswer").add(submitAnswer);
        RootPanel.get("responseContainer").add(serverUpdates);

        // Setup Event Listeners
        submitID.addClickHandler(new SubmitUserID());
        submitAnswer.addClickHandler(new SubmitAnswer());
    }

    // Step 1: Submit Username
    private class SubmitUserID implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            userIDBox.setEnabled(false);
            submitID.setEnabled(false);
            serverUpdates.setText("Fetching question...");

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "/api/usersecq?userid=" + App.B64Encode(userIDBox.getText()));
            try {
                builder.sendRequest(null, new RetrieveUserSecq());
            } catch (Exception e) {
                serverUpdates.setText("Client Error. Try reloading page.");
            }
        }
    }

    // Step 1: Handle Server Response
    private class RetrieveUserSecq implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case 200:
                    serverUpdates.setText("Found userID. Waiting for answer.");
                    questionLabel.setText(response.getText());
                    answerBox.setEnabled(true);
                    submitAnswer.setEnabled(true);
                    break;
                case 404:
                default:
                    serverUpdates.setText("Failed to find user with that UserID.");
                    userIDBox.setEnabled(true);
                    submitID.setEnabled(true);
                    break;
            }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
            serverUpdates.setText("Server Error. Try again later.");
        }
    }

    // Step 2: Submit Secure Answer
    private class SubmitAnswer implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            answerBox.setEnabled(false);
            submitAnswer.setEnabled(false);

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "api/userseca?userid=" + App.B64Encode(userIDBox.getText()) +
                            "&answer=" + App.B64Encode(answerBox.getText()));

            try {
                builder.sendRequest(null, new GetTokenResponse());
            } catch (Exception e) {
                serverUpdates.setText("Client Error. Try reloading page.");
            }
        }
    }

    // Step 2: Handle Verification Response
    private class GetTokenResponse implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case 200:
                    serverUpdates.getElement().getStyle().setColor("green");
                    serverUpdates.setText("Success! A secure reset link has been sent to your email.");
                    break;
                case 404:
                default:
                    serverUpdates.getElement().getStyle().setColor("red");
                    serverUpdates.setText("Incorrect credentials.");
                    submitAnswer.setEnabled(true);
                    answerBox.setEnabled(true);
                    break;
            }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
            serverUpdates.getElement().getStyle().setColor("red");
            serverUpdates.setText("Network error occurred.");
            submitAnswer.setEnabled(true);
            answerBox.setEnabled(true);
        }
    }
}