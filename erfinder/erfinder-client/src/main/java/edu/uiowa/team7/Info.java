package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;

public class Info {
    private final TextBox emailBox;
    private final TextBox phoneBox;
    private final Button submitBtn;
    private final Label statusLabel;

    public Info() {
        emailBox = new TextBox();
        phoneBox = new TextBox();
        submitBtn = new Button("Save Changes");
        statusLabel = new Label("Loading your info...");

        RootPanel.get("emailContainer").add(emailBox);
        RootPanel.get("phoneContainer").add(phoneBox);
        RootPanel.get("submitBtnContainer").add(submitBtn);
        RootPanel.get("statusContainer").add(statusLabel);

        // Fetch existing data on load
        fetchCurrentInfo();

        submitBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                saveUpdatedInfo();
            }
        });
    }

    private void fetchCurrentInfo() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        // Assuming backend returns: Role,FirstName,Email,Phone
                        String[] data = response.getText().split(",");
                        if(data.length >= 4) {
                            emailBox.setText(data[2]);
                            phoneBox.setText(data[3]);
                            statusLabel.setText("");
                        }
                    } else {
                        statusLabel.setText("Could not load past info.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    statusLabel.setText("Server connection error.");
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void saveUpdatedInfo() {
        submitBtn.setEnabled(false);
        statusLabel.setText("Updating...");

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                "/api/updateinfo?email=" + App.B64Encode(emailBox.getText()) +
                        "&phone=" + App.B64Encode(phoneBox.getText()));

        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    submitBtn.setEnabled(true);
                    if (response.getStatusCode() == 200) {
                        statusLabel.setText("Profile updated successfully!");
                    } else {
                        statusLabel.setText("Failed to update.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    submitBtn.setEnabled(true);
                    statusLabel.setText("Server connection error.");
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }
}