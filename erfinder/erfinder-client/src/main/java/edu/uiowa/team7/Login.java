package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.eclipse.jetty.http.HttpStatus;

public class Login {

    // Fields
    private final Button sendButton;
    private final TextBox userIDBox;
    private final PasswordTextBox passwordTextBox;
    private final Label response;

    // Constructor
    public Login() {
        sendButton = new Button("Log in");
        sendButton.addStyleName("sendButton");
        userIDBox = new TextBox();
        userIDBox.setMaxLength(32);
        passwordTextBox = new PasswordTextBox();
        passwordTextBox.setMaxLength(32);
        response = new Label();

        // UI Injection
        RootPanel.get("userIDContainer").add(userIDBox);
        RootPanel.get("passwordContainer").add(passwordTextBox);
        RootPanel.get("sendButtonContainer").add(sendButton);
        RootPanel.get("responseContainer").add(response);

        userIDBox.setFocus(true);
        userIDBox.selectAll();

        // Click Handlers
        sendButton.addClickHandler(new SendCredentials());
    }

    // API Handlers
    private class SendCredentials implements ClickHandler {
        public void onClick(ClickEvent event) {
            sendButton.setEnabled(false);
            passwordTextBox.setEnabled(false);
            userIDBox.setEnabled(false);
            response.setText("Signing in...");

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "/api/gettoken?userID="
                            + App.B64Encode(userIDBox.getText())
                            + "&password="
                            + App.B64Encode(passwordTextBox.getText())
            );
            try {
                builder.sendRequest(null, new GetTokenResponse());
            } catch (RequestException e) {
                response.setText("An unexpected error occured!");
                // Compliance
            }
        }
    }

    private class GetTokenResponse implements RequestCallback {
        public void onResponseReceived(Request request, Response received) {
            switch (received.getStatusCode()) {
                case HttpStatus.OK_200:
                    Window.Location.assign("home.html");
                    break;
                case HttpStatus.NOT_FOUND_404:
                    sendButton.setEnabled(true);
                    passwordTextBox.setEnabled(true);
                    userIDBox.setEnabled(true);
                    response.setText("Invalid credentials.");
                    break;
                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    sendButton.setEnabled(true);
                    passwordTextBox.setEnabled(true);
                    userIDBox.setEnabled(true);
                    response.setText("Internal Server Error.");
                    break;
                default:
                    sendButton.setEnabled(true);
                    passwordTextBox.setEnabled(true);
                    userIDBox.setEnabled(true);
                    response.setText("Unexpected error occurred.");
                    break;
            }
        }

        public void onError(Request request, Throwable exception) {
            sendButton.setEnabled(true);
            passwordTextBox.setEnabled(true);
            userIDBox.setEnabled(true);
            response.setText("Unable to connect to server!");
        }
    }
}