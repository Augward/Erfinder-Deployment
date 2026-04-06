package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class Pending {

    private final Label statusLabel;
    private final Timer checkTimer;

    public Pending() {
        statusLabel = new Label("Checking live status...");
        RootPanel.get("pendingStatusContainer").add(statusLabel);

        // Auto-poll the server every 5 seconds
        checkTimer = new Timer() {
            @Override
            public void run() {
                checkApprovalStatus();
            }
        };
        // Initial check, then start loop
        checkApprovalStatus();
        checkTimer.scheduleRepeating(5000);
    }

    private void checkApprovalStatus() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        String[] data = response.getText().split(",", -1);
                        String role = data[0].trim().toUpperCase();

                        // If the role is no longer PENDING, they have been approved!
                        if (!role.equals("PENDING")) {
                            checkTimer.cancel(); // Stop polling
                            statusLabel.getElement().getStyle().setColor("green");
                            statusLabel.setText("Account Approved! Redirecting...");
                            Window.Location.assign("home.html");
                        } else {
                            // Simple visual indicator that polling is active
                            String text = statusLabel.getText();
                            statusLabel.setText(text.endsWith("...") ? "Checking live status." : text + ".");
                        }
                    } else if (response.getStatusCode() == 401) {
                        checkTimer.cancel();
                        Window.Location.assign("login.html");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    statusLabel.setText("Awaiting server connection...");
                }
            });
        } catch (RequestException e) {
            statusLabel.setText("Network error.");
        }
    }
}