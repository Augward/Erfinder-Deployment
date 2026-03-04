package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Login {
    public static void Build() {

        // 1. Create the UI Widgets
        final Button sendButton = new Button("Log in");

        final TextBox userIDBox = new TextBox();
        userIDBox.setMaxLength(32);
        final PasswordTextBox passwordTextBox = new PasswordTextBox();
        passwordTextBox.setMaxLength(32);
        final Label response = new Label();

        sendButton.addStyleName("sendButton");

        // 2. Add widgets to the HTML page (matches the archetype's index.html)
        RootPanel.get("userIDContainer").add(userIDBox);
        RootPanel.get("passwordContainer").add(passwordTextBox);
        RootPanel.get("sendButtonContainer").add(sendButton);
        RootPanel.get("responseContainer").add(response);

        // Focus the cursor on the name field when the app loads
        userIDBox.setFocus(true);
        userIDBox.selectAll();

        // 3. Define what happens when the button is clicked
        sendButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendButton.setEnabled(false);
                passwordTextBox.setEnabled(false);
                userIDBox.setEnabled(false);
                response.setText("Signing in...");

                // 4. Build the REST Request to hit your Spring Controller
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                        "/api/validate?userID="+userIDBox.getText()
                                +"&password="+passwordTextBox.getText());
                try {
                    builder.sendRequest(null, new RequestCallback() {

                        // If Spring successfully answers:
                        public void onResponseReceived(Request request, Response received) {
                            sendButton.setEnabled(true);
                            passwordTextBox.setEnabled(true);
                            userIDBox.setEnabled(true);
                            response.setText(received.getText());
                        }

                        // If the server is offline or fails:
                        public void onError(Request request, Throwable exception) {
                            sendButton.setEnabled(true);
                            passwordTextBox.setEnabled(true);
                            userIDBox.setEnabled(true);
                            response.setText("You're Offline!");
                        }
                    });
                } catch (RequestException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
