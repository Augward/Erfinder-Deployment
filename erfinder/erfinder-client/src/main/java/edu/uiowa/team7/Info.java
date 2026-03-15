package edu.uiowa.team7;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Info {

    private static final Logger logger = Logger.getLogger(Info.class.getName());

    // Fields - Form Inputs
    private final TextBox firstnBox = new TextBox();
    private final TextBox lastnBox = new TextBox();
    private final TextBox legalnBox = new TextBox();
    private final TextBox dobBox = new TextBox();
    private final TextBox emailBox = new TextBox();
    private final TextBox phoneBox = new TextBox();
    private final TextBox addrBox = new TextBox();
    private final TextBox zipBox = new TextBox();
    private final TextBox dlnBox = new TextBox();
    private final TextBox ssnBox = new TextBox();
    private final TextBox contactBox = new TextBox();
    private final TextBox genderBox = new TextBox();

    // Fields - Security & Actions
    private final PasswordTextBox passwordBox = new PasswordTextBox();
    private final PasswordTextBox deletePassBox = new PasswordTextBox();

    private final Button toggleEditBtn;
    private final Button updatePassBtn;
    private final Button deleteAccountBtn;
    private final Label statusLabel;

    // State Tracker
    private boolean isEditing = false;

    // Constructor
    public Info() {
        TextBox[] boxes = {firstnBox, lastnBox, legalnBox, dobBox, emailBox, phoneBox,
                addrBox, zipBox, dlnBox, ssnBox, contactBox, genderBox, passwordBox, deletePassBox};
        for (TextBox box : boxes) { box.addStyleName("form-input"); }
        setFieldsReadOnly(true);
        passwordBox.setReadOnly(false);
        deletePassBox.setReadOnly(false);

        toggleEditBtn = new Button("Edit Profile");
        toggleEditBtn.addStyleName("btn");

        updatePassBtn = new Button("Update Password");
        updatePassBtn.addStyleName("btn");

        deleteAccountBtn = new Button("Delete Account");
        deleteAccountBtn.addStyleName("btn");

        deleteAccountBtn.getElement().getStyle().setBackgroundColor("#fee2e2");
        deleteAccountBtn.getElement().getStyle().setColor("#dc2626");

        statusLabel = new Label("Loading...");

        // UI Injection
        RootPanel.get("firstnContainer").add(firstnBox);
        RootPanel.get("lastnContainer").add(lastnBox);
        RootPanel.get("legalnContainer").add(legalnBox);
        RootPanel.get("dobContainer").add(dobBox);
        RootPanel.get("emailContainer").add(emailBox);
        RootPanel.get("phoneContainer").add(phoneBox);
        RootPanel.get("addrContainer").add(addrBox);
        RootPanel.get("zipContainer").add(zipBox);
        RootPanel.get("dlnContainer").add(dlnBox);
        RootPanel.get("ssnContainer").add(ssnBox);
        RootPanel.get("contactContainer").add(contactBox);
        RootPanel.get("genderContainer").add(genderBox);

        RootPanel.get("passwordContainer").add(passwordBox);
        RootPanel.get("passwordUpdateBtnContainer").add(updatePassBtn);

        RootPanel.get("deletePassContainer").add(deletePassBox);
        RootPanel.get("deleteAccountBtnContainer").add(deleteAccountBtn);

        RootPanel.get("btnContainer").add(toggleEditBtn);
        RootPanel.get("statusContainer").add(statusLabel);

        // API Fetch
        fetchCurrentInfo();

        toggleEditBtn.addClickHandler(event -> {
            if (!isEditing) {
                isEditing = true;
                setFieldsReadOnly(false);
                toggleEditBtn.setText("Save Changes");
            } else { saveUpdatedInfo(); }
        });

        updatePassBtn.addClickHandler(event -> updatePassword());
        deleteAccountBtn.addClickHandler(event -> deleteAccount());
    }

    // UI Helper Methods
    private void setFieldsReadOnly(boolean readOnly) {
        TextBox[] boxes = {firstnBox, lastnBox, legalnBox, dobBox, emailBox, phoneBox, addrBox, zipBox, dlnBox, ssnBox, contactBox, genderBox};
        for (TextBox box : boxes) { box.setReadOnly(readOnly); }
    }

    // API Calls
    private void fetchCurrentInfo() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        String[] data = response.getText().split(",", -1);
                        if(data.length >= 13) {
                            firstnBox.setText(data[1]);
                            lastnBox.setText(data[2]);
                            legalnBox.setText(data[3]);
                            dlnBox.setText(data[4]);
                            ssnBox.setText(data[5]);
                            emailBox.setText(data[6]);
                            phoneBox.setText(data[7]);
                            addrBox.setText(data[8]);
                            zipBox.setText(data[9]);
                            dobBox.setText(data[10]);
                            genderBox.setText(data[11]);
                            contactBox.setText(data[12]);
                            statusLabel.setText("");
                        }
                    } else { statusLabel.setText("Session expired. Please log in."); }
                }
                public void onError(Request request, Throwable exception) { statusLabel.setText("Server connection error."); }
            });
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }

    private void saveUpdatedInfo() {
        toggleEditBtn.setEnabled(false);
        statusLabel.setText("Updating database...");
        statusLabel.getElement().getStyle().setColor("black");

        String url = "/api/updateinfo?" +
                "firstn=" + App.B64Encode(firstnBox.getText()) + "&lastn=" + App.B64Encode(lastnBox.getText()) +
                "&legaln=" + App.B64Encode(legalnBox.getText()) + "&dln=" + App.B64Encode(dlnBox.getText()) +
                "&ssn=" + App.B64Encode(ssnBox.getText()) + "&email=" + App.B64Encode(emailBox.getText()) +
                "&phone=" + App.B64Encode(phoneBox.getText()) + "&addr=" + App.B64Encode(addrBox.getText()) +
                "&zip=" + App.B64Encode(zipBox.getText()) + "&dob=" + App.B64Encode(dobBox.getText()) +
                "&gender=" + App.B64Encode(genderBox.getText()) + "&contact=" + App.B64Encode(contactBox.getText());

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    toggleEditBtn.setEnabled(true);
                    if (response.getStatusCode() == 200) {
                        statusLabel.getElement().getStyle().setColor("green");
                        statusLabel.setText("Profile updated successfully!");
                        isEditing = false;
                        setFieldsReadOnly(true);
                        toggleEditBtn.setText("Edit Profile");
                    } else {
                        statusLabel.getElement().getStyle().setColor("red");
                        statusLabel.setText("Failed to update database. Ensure all * required fields are filled.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    toggleEditBtn.setEnabled(true);
                    statusLabel.getElement().getStyle().setColor("red");
                    statusLabel.setText("Server connection error.");
                }
            });
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }

    private void updatePassword() {
        String newPass = passwordBox.getText();
        if (newPass == null || newPass.trim().isEmpty()) {
            statusLabel.getElement().getStyle().setColor("red");
            statusLabel.setText("Please enter a new password first.");
            return;
        }
        updatePassBtn.setEnabled(false);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/updatepassword?newpass=" + App.B64Encode(newPass));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) { Window.Location.assign("index.html"); }
                    else { updatePassBtn.setEnabled(true); statusLabel.setText("Failed to update password."); }
                }
                public void onError(Request request, Throwable exception) { updatePassBtn.setEnabled(true); statusLabel.setText("Server error."); }
            });
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }

    private void deleteAccount() {
        String pass = deletePassBox.getText();
        if (pass == null || pass.trim().isEmpty()) {
            statusLabel.getElement().getStyle().setColor("red");
            statusLabel.setText("Enter current password to delete account.");
            return;
        }
        if (!Window.confirm("Are you ABSOLUTELY sure? This will wipe your account.")) return;

        deleteAccountBtn.setEnabled(false);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/deleteaccount?password=" + App.B64Encode(pass));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) { Window.Location.assign("index.html"); }
                    else {
                        deleteAccountBtn.setEnabled(true);
                        statusLabel.getElement().getStyle().setColor("red");
                        statusLabel.setText("Incorrect password. Account not deleted.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    deleteAccountBtn.setEnabled(true);
                    statusLabel.setText("Server error.");
                }
            });
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }
}