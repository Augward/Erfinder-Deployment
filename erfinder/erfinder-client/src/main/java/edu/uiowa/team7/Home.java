package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;

public class Home {
    private final Label welcomeLabel;

    public Home() {
        welcomeLabel = new Label("Loading your dashboard...");
        RootPanel.get("welcomeContainer").add(welcomeLabel);

        // Fetch personalized data
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        String[] data = response.getText().split(",");
                        welcomeLabel.setText("Welcome back to ERFinder, " + data[0] + "!");
                    } else {
                        welcomeLabel.setText("Please log in.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    welcomeLabel.setText("Failed to load data.");
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}