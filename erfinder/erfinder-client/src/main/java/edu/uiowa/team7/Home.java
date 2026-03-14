package edu.uiowa.team7;

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

                        DOM.getElementById("roleBar").setInnerText("Role: " + role);
                        dynamicLayout.clear();
                        dynamicLayout.add(new HTML("<h2>Welcome, " + firstName + "</h2>"));

                        if (role.equals("PENDING")) { buildPendingView(); }
                        else if (role.equals("ADMIN")) { buildAdminView(); }
                        else if (role.equals("DOCTOR") || role.equals("NURSE") || role.equals("EMT")) { buildMedicalView(); }
                        else { buildPatientView(); }
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

        FlexTable approvalTable = new FlexTable();
        approvalTable.setWidth("100%");
        approvalTable.addStyleName("form-input"); // Borrowing border style
        approvalTable.setHTML(0, 0, "<b>User ID</b>");
        approvalTable.setHTML(0, 1, "<b>Role Requested</b>");
        approvalTable.setHTML(0, 2, "<b>Action</b>");

        approvalTable.setText(1, 0, "JohnDoe");
        approvalTable.setText(1, 1, "Patient");
        approvalTable.setWidget(1, 2, new Button("Approve"));

        dynamicLayout.add(approvalTable);
    }
}