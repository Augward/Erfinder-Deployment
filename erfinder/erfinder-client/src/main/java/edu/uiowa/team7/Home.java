package edu.uiowa.team7;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Home {

    private static final Logger logger = Logger.getLogger(Home.class.getName());

    // Fields
    private final VerticalPanel dynamicLayout;

    // private final CheckBox[] ESIBoxes = new CheckBox[4];

    // Constructor
    public Home() {
        dynamicLayout = new VerticalPanel();
        dynamicLayout.setSpacing(10);
        dynamicLayout.setWidth("100%");
        RootPanel.get("dynamicContent").add(dynamicLayout);
        loadDashboard();
    }

    // API Data Loading
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
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }

    // Role-Specific Views
    private void buildPendingView() {
        dynamicLayout.add(new HTML("<h3>Account Pending Approval</h3>"));
        dynamicLayout.add(new Label("Your account request is currently being reviewed. Please check back later."));
    }

    private void buildPatientView() {
        dynamicLayout.add(new HTML("<h3>Find Immediate Care</h3>"));
        dynamicLayout.add(new HTML("<h4>Check all that apply:</h3>"));

        CheckBox[] boxes = new CheckBox[4];

        HTMLPanel t = new HTMLPanel("table","");
        String[] lines = {
                "Require life-saving intervention now.",
                "In a dangerous situation.",
                "In severe pain.",
                "Feel strange, disoriented, and/or lethargic."
        };
        HTMLPanel[] rows = new HTMLPanel[4];
        for (int i = 0; i < 4; i ++) {
            rows[i] = new HTMLPanel("tr", "");
            rows[i].add(cw(new Label(lines[i])));
            rows[i].add(cw(/*ESIBoxes[i] = */ boxes[i] = new CheckBox()));
            // ESIBoxes[i].addClickHandler(event -> ESIBoxEvent());
            t.add(rows[i]);
        }

        dynamicLayout.add(t);

        ListBox injuryType = new ListBox();
        injuryType.addStyleName("form-input");
        injuryType.addItem("Select Injury/Condition Type...");
        injuryType.addItem("Cardiac / Chest Pain");
        injuryType.addItem("Trauma / Broken Bones");
        injuryType.addItem("Neurological / Stroke");
        injuryType.addItem("General Illness");
        dynamicLayout.add(new Label("Current Condition:"));
        dynamicLayout.add(injuryType);

        ListBox priority = new ListBox();
        priority.addStyleName("form-input");
        priority.addItem("By Lowest Estimated Time-To-Care"); // DIST + WAIT
        priority.addItem("By Nearest"); // DIST ONLY
        priority.addItem("By Lowest Wait Time"); // WAIT ONLY
        dynamicLayout.add(new Label("Search Priority:"));
        dynamicLayout.add(priority);

        //TextBox zipBox = new TextBox();
        //zipBox.addStyleName("form-input");
        //zipBox.getElement().setPropertyString("placeholder", "Enter current Zip Code...");
        //dynamicLayout.add(new Label("Location:"));
        //dynamicLayout.add(zipBox);

        Button searchBtn = new Button("Search ERs");
        searchBtn.addStyleName("btn");
        searchBtn.addClickHandler(event ->{
            int esiScore = boxes[0].getValue() ? 1 :
                    (boxes[1].getValue() || boxes[2].getValue() || boxes[3].getValue() ? 2 : 3);

            int in = injuryType.getSelectedIndex();
            int pr = priority.getSelectedIndex();

            dynamicLayout.clear();

            dynamicLayout.add(new SearchUI(esiScore, in, pr, ()->{
                dynamicLayout.clear();
                loadDashboard();
            }));
        });
        dynamicLayout.add(searchBtn);
    }
    private HTMLPanel cw(Widget w) {
        HTMLPanel p = new HTMLPanel("td", "");
        p.add(w);
        return p;
    }

    private void buildMedicalView() {
        dynamicLayout.add(new HTML("<h3>Facility Status Update</h3>"));

        /*
        ListBox statusBox = new ListBox();
        statusBox.addStyleName("form-input");
        statusBox.addItem("Accepting Patients (Normal Capacity)");
        statusBox.addItem("High Capacity (Expect Delays)");
        statusBox.addItem("Divert (Critical Status)");
        dynamicLayout.add(new Label("Set Current ER Status:"));
        dynamicLayout.add(statusBox);
        */

        ListBox erselect = new ListBox();
        erselect.addStyleName("form-input");
        dynamicLayout.add(new Label("Select ER Facility:"));
        dynamicLayout.add(erselect);

        TextBox waitTimeBox = new TextBox();
        waitTimeBox.addStyleName("form-input");
        waitTimeBox.getElement().setPropertyString("placeholder", "Estimated wait time in minutes...");
        dynamicLayout.add(new Label("Update Wait Time:"));
        dynamicLayout.add(waitTimeBox);


        Button updateBtn = new Button("Broadcast Update");
        updateBtn.addStyleName("btn");
        updateBtn.addClickHandler(event->{
            if(erselect.getSelectedIndex() == 0){
                Window.alert("Please Select an ER");
                return;
            }

            String waitTime = waitTimeBox.getText().trim();
            if(!waitTime.matches("\\d+")){
                Window.alert("Must Enter Valid Wait Time");
                return;
            }

            String facilityId = erselect.getValue(erselect.getSelectedIndex());
            int facilityid = Integer.parseInt(facilityId);
            int waitMinutes = Integer.parseInt(waitTime);
            sendWaitTimeUpdate(facilityid, waitMinutes);


        });
        dynamicLayout.add(updateBtn);

        FacilityUpdate.loadFacilities(erselect);
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

                                HorizontalPanel actions = new HorizontalPanel();
                                actions.setSpacing(5);

                                Button approveBtn = new Button("Approve");
                                approveBtn.addStyleName("btn");
                                approveBtn.addClickHandler(event -> approveUser(uid));

                                Button rejectBtn = new Button("Reject");
                                rejectBtn.addStyleName("btn");
                                rejectBtn.getElement().getStyle().setProperty("backgroundColor", "#dc3545"); // Red button
                                rejectBtn.addClickHandler(event -> rejectUser(uid));

                                actions.add(approveBtn);
                                actions.add(rejectBtn);

                                approvalTable.setWidget(row, 2, actions);
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
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }

        //ER FACILITY REGISTER STUFF
        Button facilityReg = new Button("Register ER Facility");
        facilityReg.addStyleName("btn");
        facilityReg.addClickHandler(event -> registerFacility());
        dynamicLayout.add(facilityReg);

        //ER FACILITY UPDATE STUFF
        Button upFac = new Button("Update ER Facility");
        upFac.addStyleName("btn");
        upFac.addClickHandler(event -> updateFacility());
        dynamicLayout.add(upFac);

    }

    // Admin Helper Action
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
        } catch (RequestException e) { logger.log(Level.SEVERE, "An error occurred during the request", e); }
    }

    private void rejectUser(final String targetID) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/rejectuser?target=" + App.B64Encode(targetID));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Window.alert("User " + targetID + " has been rejected and removed.");
                        dynamicLayout.clear();
                        loadDashboard(); // Refresh the table
                    } else {
                        Window.alert("Failed to reject user.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    Window.alert("Server connection error.");
                }
            });
        } catch (RequestException e) { logger.log(java.util.logging.Level.SEVERE, "An error occurred", e); }
    }

    private void registerFacility(){
        dynamicLayout.clear();
        dynamicLayout.add(new Facility_Register(() -> {
            dynamicLayout.clear();
            loadDashboard();
        }));
    }

    private void updateFacility(){
        dynamicLayout.clear();
        dynamicLayout.add(new FacilityUpdate(() -> {
            dynamicLayout.clear();
            loadDashboard();
        }));
    }

    private void sendWaitTimeUpdate(int facilityId, int waitMinutes){
        String payload = "{"
                        + "\"facilityId\":" + facilityId + ","
                        + "\"waitTime\":" + waitMinutes
                        + "}";

        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "/api/updateWaitTime");
        rb.setHeader("Content-Type", "application/json");
        try{
            rb.sendRequest(payload, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if(response.getStatusCode() == 200){
                        Window.alert("Wait Time Updated SuccessFully!!");
                    }
                    else{
                        Window.alert("Update Failed (status " + response.getStatusCode() + ")");
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Window.alert("Server Error While Updating Wait Time");

                }
            });
        }
        catch(RequestException e){
            Window.alert("Request Didn't Send");
        }

    }
}