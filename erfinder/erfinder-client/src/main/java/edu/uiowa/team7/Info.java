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
        submitBtn = new Button("Update Info");
        statusLabel = new Label();

        RootPanel.get("emailContainer").add(emailBox);
        RootPanel.get("phoneContainer").add(phoneBox);
        RootPanel.get("submitBtnContainer").add(submitBtn);
        RootPanel.get("statusContainer").add(statusLabel);

        submitBtn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
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
                                statusLabel.setText("Info updated successfully!");
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
        });
    }
}