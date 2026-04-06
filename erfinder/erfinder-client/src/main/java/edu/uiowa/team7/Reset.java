package edu.uiowa.team7;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Reset {

    private final PasswordTextBox newPassBox;
    private final PasswordTextBox confirmPassBox;
    private final Button submitBtn;
    private final Label responseLabel;

    public Reset() {
        newPassBox = new PasswordTextBox();
        newPassBox.addStyleName("form-input");

        confirmPassBox = new PasswordTextBox();
        confirmPassBox.addStyleName("form-input");

        submitBtn = new Button("Confirm New Password");
        submitBtn.addStyleName("btn");

        responseLabel = new Label();

        RootPanel.get("newPassContainer").add(newPassBox);
        RootPanel.get("confirmPassContainer").add(confirmPassBox);
        RootPanel.get("submitResetContainer").add(submitBtn);
        RootPanel.get("resetResponseContainer").add(responseLabel);

        submitBtn.addClickHandler(new SubmitNewPassword());
    }

    private class SubmitNewPassword implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            String pass1 = newPassBox.getText().trim();
            String pass2 = confirmPassBox.getText().trim();

            if (pass1.isEmpty() || pass2.isEmpty()) {
                responseLabel.setText("Please fill out both fields.");
                return;
            }
            if (!pass1.equals(pass2)) {
                responseLabel.setText("Passwords do not match!");
                return;
            }
            if (!pass1.matches("^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$")) {
                responseLabel.setText("Password must be 8+ chars with a letter and number.");
                return;
            }

            submitBtn.setEnabled(false);
            responseLabel.getElement().getStyle().setColor("black");
            responseLabel.setText("Updating password...");

            // Call existing update password API
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                    "/api/updatepassword?newpass=" + App.B64Encode(pass1));

            try {
                builder.sendRequest(null, new RequestCallback() {
                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() == 200) {
                            Window.alert("Password successfully updated! Please log in.");
                            Window.Location.assign("login.html");
                        } else {
                            submitBtn.setEnabled(true);
                            responseLabel.getElement().getStyle().setColor("red");
                            responseLabel.setText("Session expired or unauthorized.");
                        }
                    }
                    public void onError(Request request, Throwable exception) {
                        submitBtn.setEnabled(true);
                        responseLabel.getElement().getStyle().setColor("red");
                        responseLabel.setText("Network error occurred.");
                    }
                });
            } catch (RequestException e) {
                responseLabel.setText("Client exception occurred.");
            }
        }
    }
}