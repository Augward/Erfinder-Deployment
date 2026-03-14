package edu.uiowa.team7;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

public class Home {
    private final VerticalPanel dynamicLayout;

    public Home() {
        dynamicLayout = new VerticalPanel();
        dynamicLayout.setSpacing(10);
        dynamicLayout.setWidth("100%");
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

                        DOM.getElementById("roleBar").setInnerText("Dashboard - Role: " + role);
                        dynamicLayout.clear();
                        dynamicLayout.add(new HTML("<h2>Welcome, " + firstName + "</h2>"));

                        switch (role) {
                            case "PENDING":
                                buildPendingView();
                                break;
                            case "ADMIN":
                                buildAdminView();
                                break;
                            case "DOCTOR":
                            case "NURSE":
                            case "EMT":
                                buildMedicalView();
                                break;
                            default:
                                buildPatientView();
                                break;
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
        } catch (RequestException e) { e.printStackTrace(); }
    }

    private void buildPendingView() {
        dynamicLayout.add(new HTML("<h3>Account Pending Approval</h3>"));
        dynamicLayout.add(new Label("Your account request is currently being reviewed. Please check back later."));
    }

    private void buildPatientView() {
        dynamicLayout.add(new HTML("<h3>Find Immediate Care</h3>"));

        ListBox injuryType = new ListBox();
        injuryType.addStyleName("form-input");
        injuryType.addItem("Select Injury/Condition Type...");
        injuryType.addItem("Cardiac / Chest Pain");
        injuryType.addItem("Trauma / Broken Bones");
        injuryType.addItem("Neurological / Stroke");
        injuryType.addItem("General Illness");
        dynamicLayout.add(new Label("Current Condition:"));
        dynamicLayout.add(injuryType);

        TextBox zipBox = new TextBox();
        zipBox.addStyleName("form-input");
        zipBox.getElement().setPropertyString("placeholder", "Enter current Zip Code...");
        dynamicLayout.add(new Label("Location:"));
        dynamicLayout.add(zipBox);

        Button searchBtn = new Button("Search Nearest ER");
        searchBtn.addStyleName("btn");
        dynamicLayout.add(searchBtn);
    }

    private void buildMedicalView() {
        dynamicLayout.add(new HTML("<h3>Facility Status Update</h3>"));

        ListBox statusBox = new ListBox();
        statusBox.addStyleName("form-input");
        statusBox.addItem("Accepting Patients (Normal Capacity)");
        statusBox.addItem("High Capacity (Expect Delays)");
        statusBox.addItem("Divert (Critical Status)");
        dynamicLayout.add(new Label("Set Current ER Status:"));
        dynamicLayout.add(statusBox);

        TextBox waitTimeBox = new TextBox();
        waitTimeBox.addStyleName("form-input");
        waitTimeBox.getElement().setPropertyString("placeholder", "Estimated wait time in minutes...");
        dynamicLayout.add(new Label("Update Wait Time:"));
        dynamicLayout.add(waitTimeBox);

        Button updateBtn = new Button("Broadcast Update");
        updateBtn.addStyleName("btn");
        dynamicLayout.add(updateBtn);
    }

    private void buildAdminView() {
        dynamicLayout.add(new HTML("<h3>Pending User Approvals</h3>"));
        dynamicLayout.add(new Label("The following accounts are awaiting verification:"));

        final FlexTable approvalTable = new FlexTable();
        approvalTable.setWidth("100%");
        approvalTable.addStyleName("form-input");
        approvalTable.setHTML(0, 0, "<b>User ID</b>");
        approvalTable.setHTML(0, 1, "<b>Role Requested</b>");
        approvalTable.setHTML(0, 2, "<b>Action</b>");

        dynamicLayout.add(approvalTable);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/pendingusers");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200 && !response.getText().isEmpty()) {
                        String[] users = response.getText().split(",");
                        int row = 1;
                        for (String u : users) {
                            String[] parts = u.split(":");
                            if (parts.length == 2) {
                                final String uid = parts[0];
                                String role = parts[1];

                                approvalTable.setText(row, 0, uid);
                                approvalTable.setText(row, 1, role);

                                Button approveBtn = new Button("Approve");
                                approveBtn.addStyleName("btn");
                                approveBtn.addClickHandler(event -> approveUser(uid));
                                approvalTable.setWidget(row, 2, approveBtn);
                                row++;
                            }
                        }
                    } else {
                        dynamicLayout.add(new HTML("<p style='color:green; font-weight:bold;'>No pending users at this time.</p>"));
                    }
                }
                public void onError(Request request, Throwable exception) {
                    dynamicLayout.add(new HTML("<p style='color:red;'>Failed to load users.</p>"));
                }
            });
        } catch (RequestException e) { e.printStackTrace(); }
    }

    private void approveUser(final String targetID) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/approveuser?target=" + App.B64Encode(targetID));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        com.google.gwt.user.client.Window.alert("User " + targetID + " approved successfully!");
                        dynamicLayout.clear();
                        loadDashboard();
                    } else {
                        com.google.gwt.user.client.Window.alert("Failed to approve user.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    com.google.gwt.user.client.Window.alert("Server connection error.");
                }
            });
        } catch (RequestException e) { e.printStackTrace(); }
    }
}