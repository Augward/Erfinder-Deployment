package edu.uiowa.team7;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Home {

    // Component Logger
    private static final Logger logger = Logger.getLogger(Home.class.getName());

    // Main Content Containers
    private final VerticalPanel dynamicLayout;
    private final CheckBox[] ESIBoxes = new CheckBox[4];

    // Build Home Page
    public Home() {
        dynamicLayout = new VerticalPanel();
        dynamicLayout.setSpacing(10);
        dynamicLayout.setWidth("100%");

        RootPanel.get("dynamicContent").add(dynamicLayout);
        loadDashboard();
    }

    // Fetch Authorized User Data
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
                            case "PENDING": buildPendingView(); break;
                            case "ADMIN": buildAdminView(); break;
                            case "DOCTOR":
                            case "NURSE":
                            case "EMT": buildMedicalView(); break;
                            default: buildPatientView(); break;
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
            logger.log(Level.SEVERE, "An error occurred during the request", e);
        }
    }

    // Render Unapproved UI
    private void buildPendingView() {
        dynamicLayout.add(new HTML("<h3>Account Pending Approval</h3>"));
        dynamicLayout.add(new Label("Your account request is currently being reviewed. Please check back later."));
    }

    // Render Standard Patient Form
    private void buildPatientView() {
        dynamicLayout.add(new HTML("<h3>Find Immediate Care</h3>"));
        dynamicLayout.add(new HTML("<h4>Check all that apply:</h3>"));

        HTMLPanel tablePanel = new HTMLPanel("table", "");
        String[] criteriaLines = {
                "Require life-saving intervention now.",
                "In a dangerous situation.",
                "In severe pain.",
                "Feel strange, disoriented, and/or lethargic."
        };

        HTMLPanel[] rows = new HTMLPanel[4];
        for (int i = 0; i < 4; i++) {
            rows[i] = new HTMLPanel("tr", "");
            rows[i].add(wrapWidget(new Label(criteriaLines[i])));
            rows[i].add(wrapWidget(ESIBoxes[i] = new CheckBox()));
            ESIBoxes[i].addClickHandler(event -> ESIBoxEvent());
            tablePanel.add(rows[i]);
        }

        dynamicLayout.add(tablePanel);

        addPatientConditionInputs();
    }

    // Construct Patient Injury Selection
    private void addPatientConditionInputs() {
        ListBox injuryType = new ListBox();
        injuryType.addStyleName("form-input");
        injuryType.addItem("Select Injury/Condition Type...");
        injuryType.addItem("Cardiac / Chest Pain");
        injuryType.addItem("Trauma / Broken Bones");
        injuryType.addItem("Neurological / Stroke");
        injuryType.addItem("General Illness");

        dynamicLayout.add(new Label("Current Condition:"));
        dynamicLayout.add(injuryType);

        Button searchBtn = new Button("Search ERs");
        searchBtn.addStyleName("btn");
        searchBtn.addClickHandler(event -> {
            dynamicLayout.clear();
            dynamicLayout.add(new SearchUI(() -> {
                dynamicLayout.clear();
                loadDashboard();
            }));
        });
        dynamicLayout.add(searchBtn);
    }

    // Wrap Component in Table Data Tags
    private HTMLPanel wrapWidget(Widget w) {
        HTMLPanel panel = new HTMLPanel("td", "");
        panel.add(w);
        return panel;
    }

    // Calculate Triage Level
    private int EvalESI() {
        if (ESIBoxes[0].getValue()) return 1;
        for (int i = 1; i < 4; i++) {
            if (ESIBoxes[i].getValue()) return 2;
        }
        return 3;
    }

    // Placeholder ESI Action
    private void ESIBoxEvent() {
        // Event processing here
    }

    // Render Medical Provider Interface
    private void buildMedicalView() {
        dynamicLayout.add(new HTML("<h3>Facility Status Update</h3>"));

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
        updateBtn.addClickHandler(event -> {
            if (erselect.getSelectedIndex() == 0){
                Window.alert("Please Select an ER");
                return;
            }

            String waitTime = waitTimeBox.getText().trim();
            if (!waitTime.matches("\\d+")){
                Window.alert("Must Enter Valid Wait Time");
                return;
            }

            int facilityid = Integer.parseInt(erselect.getValue(erselect.getSelectedIndex()));
            int waitMinutes = Integer.parseInt(waitTime);
            sendWaitTimeUpdate(facilityid, waitMinutes);
        });

        dynamicLayout.add(updateBtn);
        FacilityUpdate.loadFacilities(erselect);
    }

    // Render System Admin Dashboard
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
        fetchPendingUsers(approvalTable);

        Button facilityReg = new Button("Register ER Facility");
        facilityReg.addStyleName("btn");
        facilityReg.addClickHandler(event -> registerFacility());
        dynamicLayout.add(facilityReg);

        Button upFac = new Button("Update ER Facility");
        upFac.addStyleName("btn");
        upFac.addClickHandler(event -> updateFacility());
        dynamicLayout.add(upFac);
    }

    // Pull Pending Accounts Array
    private void fetchPendingUsers(FlexTable approvalTable) {
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
                                rejectBtn.getElement().getStyle().setProperty("backgroundColor", "#dc3545");
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
        } catch (RequestException e) {
            logger.log(Level.SEVERE, "An error occurred during the request", e);
        }
    }

    // Approve Targeted User API
    private void approveUser(final String targetID) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/approveuser?target=" + App.B64Encode(targetID));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Window.alert("User " + targetID + " approved successfully!");
                        dynamicLayout.clear();
                        loadDashboard();
                    } else {
                        Window.alert("Failed to approve user.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    Window.alert("Server connection error.");
                }
            });
        } catch (RequestException e) {
            logger.log(Level.SEVERE, "An error occurred during the request", e);
        }
    }

    // Reject Targeted User API
    private void rejectUser(final String targetID) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/rejectuser?target=" + App.B64Encode(targetID));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Window.alert("User " + targetID + " has been rejected and removed.");
                        dynamicLayout.clear();
                        loadDashboard();
                    } else {
                        Window.alert("Failed to reject user.");
                    }
                }
                public void onError(Request request, Throwable exception) {
                    Window.alert("Server connection error.");
                }
            });
        } catch (RequestException e) {
            logger.log(Level.SEVERE, "An error occurred", e);
        }
    }

    // Swap to Registration UI
    private void registerFacility() {
        dynamicLayout.clear();
        dynamicLayout.add(new Facility_Register(() -> {
            dynamicLayout.clear();
            loadDashboard();
        }));
    }

    // Swap to Update UI
    private void updateFacility() {
        dynamicLayout.clear();
        dynamicLayout.add(new FacilityUpdate(() -> {
            dynamicLayout.clear();
            loadDashboard();
        }));
    }

    // Post Facility Wait Time Changes
    private void sendWaitTimeUpdate(int facilityId, int waitMinutes) {
        String payload = "{"
                + "\"facilityId\":" + facilityId + ","
                + "\"waitTime\":" + waitMinutes
                + "}";

        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "/api/updateWaitTime");
        rb.setHeader("Content-Type", "application/json");

        try {
            rb.sendRequest(payload, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200) {
                        Window.alert("Wait Time Updated SuccessFully!!");
                    } else {
                        Window.alert("Update Failed (status " + response.getStatusCode() + ")");
                    }
                }
                @Override
                public void onError(Request request, Throwable throwable) {
                    Window.alert("Server Error While Updating Wait Time");
                }
            });
        } catch(RequestException e) {
            Window.alert("Request Didn't Send");
        }
    }
}