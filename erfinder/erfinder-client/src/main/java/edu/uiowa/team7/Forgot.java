package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Forgot {

    // Fields
    private final TextBox userIDBox;
    private final Button submitID;
    private final Label questionLabel;
    private final PasswordTextBox answerBox;
    private final Button submitAnswer;
    private final Label serverUpdates;

    // Constructor
    public Forgot() {
        userIDBox = new TextBox();
        submitID = new Button("Next");

        questionLabel = new Label();
        answerBox = new PasswordTextBox();
        submitAnswer = new Button("Submit");

        serverUpdates = new Label();

        answerBox.setEnabled(false);
        submitAnswer.setEnabled(false);

        // UI Injection
        RootPanel.get("userIDContainer").add(userIDBox);
        RootPanel.get("submitID").add(submitID);

        RootPanel.get("questionContainer").add(questionLabel);
        RootPanel.get("answerContainer").add(answerBox);
        RootPanel.get("submitAnswer").add(submitAnswer);

        RootPanel.get("responseContainer").add(serverUpdates);

        // Click Handlers
        submitID.addClickHandler(new SubmitUserID());
        submitAnswer.addClickHandler(new SubmitAnswer());
    }

    // API Handlers
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

    private class RetrieveUserSecq implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case 200:
                    // userid is response text
                    serverUpdates.setText("Found userID. Waiting for answer.");
                    questionLabel.setText(response.getText());
                    answerBox.setEnabled(true);
                    submitAnswer.setEnabled(true);
                    break;
                case 404:
                default:
                    // not valid userid
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

    private class SubmitAnswer implements ClickHandler {
        @Override
        public void onClick(ClickEvent clickEvent) {
            answerBox.setEnabled(false);
            submitAnswer.setEnabled(false);

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "api/userseca?userid=" + App.B64Encode(userIDBox.getText()) +
                            "&answer=" + App.B64Encode(answerBox.getText())
            );

            try {
                builder.sendRequest(null, new GetTokenResponse());
            } catch (Exception e) {
                serverUpdates.setText("Client Error. Try reloading page.");
            }
        }
    }

    private class GetTokenResponse implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case 200:
                    String token = response.getText();
                    // Attach the token
                    Window.Location.assign("reset.html?t=" + token + "&u=" + App.B64Encode(userIDBox.getText()));
                    break;
                case 404:
                default:
                    serverUpdates.setText("Incorrect");
                    submitAnswer.setEnabled(true);
                    answerBox.setEnabled(true);
                    break;
            }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
            serverUpdates.setText("Network error occurred.");
        }
    }
}