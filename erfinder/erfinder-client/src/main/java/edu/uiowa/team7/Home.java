package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.*;

public class Home {
    private final VerticalPanel mainLayout;

    public Home() {
        mainLayout = new VerticalPanel();
        mainLayout.setWidth("100%");
        RootPanel.get("dashboardContainer").add(mainLayout);

        loadDashboard();
    }

    private void loadDashboard() {
        mainLayout.add(new Label("Loading tailored dashboard..."));

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/myinfo");
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    mainLayout.clear();
                    if (response.getStatusCode() == 200) {
                        // Assuming backend returns: Role,FirstName,Email,Phone
                        String[] data = response.getText().split(",");
                        String role = data[0].trim().toLowerCase();
                        String firstName = data[1];

                        buildTopNav(firstName);

                        if (role.equals("admin")) {
                            buildAdminView();
                        } else if (role.equals("medical") || role.equals("er")) {
                            buildMedicalView();
                        } else {
                            buildClientView();
                        }
                    } else {
                        mainLayout.add(new Label("Session expired. Please log in."));
                    }
                }
                public void onError(Request request, Throwable exception) {
                    mainLayout.clear();
                    mainLayout.add(new Label("Failed to connect to server."));
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    private void buildTopNav(String name) {
        HorizontalPanel nav = new HorizontalPanel();
        nav.setWidth("100%");
        nav.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        nav.getElement().getStyle().setBackgroundColor("#e2e8f0");
        nav.getElement().getStyle().setProperty("padding", "15px");

        HTML profileLink = new HTML("<a href='info.html'>Profile (" + name + ")</a>");
        HTML spacer = new HTML("&nbsp;&nbsp;|&nbsp;&nbsp;");
        HTML logoutLink = new HTML("<a href='/api/logout'>Logout</a>");

        nav.add(profileLink);
        nav.add(spacer);
        nav.add(logoutLink);

        mainLayout.add(nav);
    }

    private void buildClientView() {
        mainLayout.add(new HTML("<h2>Patient Dashboard</h2>"));
        mainLayout.add(new Label("Map and ER search tools will go here."));
    }

    private void buildMedicalView() {
        mainLayout.add(new HTML("<h2>Medical Staff Dashboard</h2>"));
        mainLayout.add(new Label("Update ER wait times and capacity:"));

        HorizontalPanel updatePanel = new HorizontalPanel();
        updatePanel.setSpacing(10);
        updatePanel.add(new TextBox()); // Placeholder for wait time
        updatePanel.add(new Button("Update Status"));
        mainLayout.add(updatePanel);
    }

    private void buildAdminView() {
        mainLayout.add(new HTML("<h2>Administrator Dashboard</h2>"));
        mainLayout.add(new HTML("<h3>Pending Account Requests</h3>"));
        // Placeholder for the approval list mentioned in your progress doc
        mainLayout.add(new Label("- No new sign-up requests pending approval."));
    }
}