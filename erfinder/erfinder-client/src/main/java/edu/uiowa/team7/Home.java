package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

public class Home {
    private final VerticalPanel dynamicLayout;

    public Home() {
        dynamicLayout = new VerticalPanel();
        dynamicLayout.setSpacing(10);
        RootPanel.get("dynamicContent").add(dynamicLayout);

        loadDashboard();
    }


    private void loadDashboard() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        String[] data = response.getText().split(",", -1);
                        String role = data[0].trim().toUpperCase();
                        String firstName = data[1];

                        DOM.getElementById("roleBar").setInnerText("Role: " + role);

                        dynamicLayout.clear();
                        dynamicLayout.add(new HTML("<h2>Welcome, " + firstName + "</h2>"));

                        if (role.equals("PENDING")) {
                            buildPendingView();
                        } else if (role.equals("ADMIN")) {
                            buildAdminView();
                        } else if (role.equals("DOCTOR") || role.equals("NURSE") || role.equals("EMT")) {
                            buildMedicalView();
                        } else {
                            buildPatientView();
                        }

                    } else {
                        dynamicLayout.clear();
                        dynamicLayout.add(new HTML("<h2 style='color:red;'>Session expired. Please log in.</h2>"));
                    }
                }
                public void onError(Request request, Throwable exception) {
                    dynamicLayout.clear();
                    dynamicLayout.add(new HTML("<h2 style='color:red;'>Server Connection Error.</h2>"));
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }


    private void buildPendingView() {
        dynamicLayout.add(new HTML("<h3>Account Pending Approval</h3>"));
        dynamicLayout.add(new Label("Your account request is currently being reviewed by a system administrator. Please check back later."));
    }

    private void buildPatientView() {
        dynamicLayout.add(new Label("Search for the nearest ER matching your condition:"));
        TextBox searchBox = new TextBox();
        searchBox.addStyleName("form-input");
        dynamicLayout.add(searchBox);

        Button searchBtn = new Button("Search ERs");
        searchBtn.addStyleName("btn");
        dynamicLayout.add(searchBtn);
    }

    private void buildMedicalView() {
        dynamicLayout.add(new Label("Update current ER capacity and wait times:"));
        TextBox waitTimeBox = new TextBox();
        waitTimeBox.addStyleName("form-input");
        dynamicLayout.add(waitTimeBox);

        Button updateBtn = new Button("Broadcast Update");
        updateBtn.addStyleName("btn");
        dynamicLayout.add(updateBtn);
    }

    private void buildAdminView() {
        dynamicLayout.add(new HTML("<h3>System Administration</h3>"));
        dynamicLayout.add(new Label("To accept pending users in Sprint 1, update the database directly using:"));
        dynamicLayout.add(new HTML("<code>UPDATE users SET perm='PATIENT' WHERE userid='...';</code>"));
    }
}