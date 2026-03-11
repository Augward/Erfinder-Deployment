package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.eclipse.jetty.http.HttpStatus;

public class Login {
    private final Button sendButton;
    private final TextBox userIDBox;
    private final PasswordTextBox passwordTextBox;
    private final Label response;

    public Login() {

        // 1. Create the UI Widgets
        sendButton = new Button("Log in");
        sendButton.addStyleName("sendButton");
        userIDBox = new TextBox();
        userIDBox.setMaxLength(32);
        passwordTextBox = new PasswordTextBox();
        passwordTextBox.setMaxLength(32);
        response = new Label();

        // 2. Add widgets to the HTML page (matches the archetype's index.html)
        RootPanel.get("userIDContainer").add(userIDBox);
        RootPanel.get("passwordContainer").add(passwordTextBox);
        RootPanel.get("sendButtonContainer").add(sendButton);
        RootPanel.get("responseContainer").add(response);

        // Focus the cursor on the name field when the app loads
        userIDBox.setFocus(true);
        userIDBox.selectAll();

        // 3. Define what happens when the button is clicked
        sendButton.addClickHandler(new SendCredentials());
    }

    private class SendCredentials implements ClickHandler {
        public void onClick(ClickEvent event) {
            sendButton.setEnabled(false);
            passwordTextBox.setEnabled(false);
            userIDBox.setEnabled(false);
            response.setText("Signing in...");

            // 4. Build the REST Request to hit your Spring Controller
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "/api/gettoken?userID="
                            + App.B64Encode(userIDBox.getText())
                            + "&password="
                            + App.B64Encode(passwordTextBox.getText())
            );
            try {
                builder.sendRequest(null, new GetTokenResponse());
            } catch (RequestException e) {
                e.printStackTrace();
            }
        }
    }

    private class GetTokenResponse implements RequestCallback {
        public void onResponseReceived(Request request, Response received) {
            switch (received.getStatusCode()) {
                case HttpStatus.OK_200:
                    // redirect to home page
                    Window.Location.assign("home.html");
                    break;
                case HttpStatus.NOT_FOUND_404:
                    // show error message.
                    sendButton.setEnabled(true);
                    passwordTextBox.setEnabled(true);
                    userIDBox.setEnabled(true);
                    response.setText("Invalid credentials");
                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    sendButton.setEnabled(true);
                    passwordTextBox.setEnabled(true);
                    userIDBox.setEnabled(true);
                    response.setText("Internal Server Error");

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
