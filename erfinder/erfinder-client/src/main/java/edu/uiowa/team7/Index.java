package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class Index {
    public static void Build() {



        // 1. Create the UI Widgets
        final Button sendButton = new Button("Check Server Status");
        final TextBox nameField = new TextBox();
        nameField.setText("Team 7 User");
        final Label errorLabel = new Label();

        sendButton.addStyleName("sendButton");

        // 2. Add widgets to the HTML page (matches the archetype's index.html)
        RootPanel.get("nameFieldContainer").add(nameField);
        RootPanel.get("sendButtonContainer").add(sendButton);
        RootPanel.get("errorLabelContainer").add(errorLabel);

        // Focus the cursor on the name field when the app loads
        nameField.setFocus(true);
        nameField.selectAll();

        // 3. Define what happens when the button is clicked
        sendButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendButton.setEnabled(false);
                errorLabel.setText("Contacting Spring Boot Server...");

                // 4. Build the REST Request to hit your Spring Controller
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/username");
                try {
                    builder.sendRequest(null, new RequestCallback() {

                        // If Spring successfully answers:
                        public void onResponseReceived(Request request, Response response) {
                            sendButton.setEnabled(true);
                            errorLabel.setText("");

                            // Pop up the JSON response from your MySQL-connected Spring server!
                            Window.alert("Success!\n\nSpring Server Says: " + response.getText());
                        }

                        // If the server is offline or fails:
                        public void onError(Request request, Throwable exception) {
                            sendButton.setEnabled(true);
                            errorLabel.setText("Failed to connect to backend.");
                            Window.alert("CRITICAL: Failed to connect to Spring Server.");
                        }
                    });
                } catch (RequestException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
