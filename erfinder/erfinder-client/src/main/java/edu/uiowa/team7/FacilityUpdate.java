package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class FacilityUpdate extends VerticalPanel {

    // State Tracking
    private final Runnable onBack;

    // UI Input Elements
    private ListBox facilityDrop;
    private TextBox bedBox;
    private TextBox waitBox;
    private Button updatebtn;

    // Form Error Labels
    private final Label facilityerror = new Label();
    private final Label bederror = new Label();
    private final Label waiterror = new Label();

    // Component Constructor
    public FacilityUpdate(Runnable onBack){
        this.onBack = onBack;
        setSpacing(10);
        setWidth("100%");

        Button backbtn = new Button(" <- Back to DashBoard");
        backbtn.addClickHandler(event -> onBack.run());
        add(backbtn);

        add(new HTML("<h3>Update ER Facility</h3>"));

        buildUI();
        loadFacilities(facilityDrop);
    }

    // Construct Form UI
    private void buildUI(){
        add(new Label("Select ER Facility"));
        facilityDrop = new ListBox();
        add(facilityDrop);
        add(facilityerror);

        add(new Label("Beds Available"));
        bedBox = new TextBox();
        bedBox.setWidth("150px");
        add(bedBox);
        add(bederror);

        add(new Label("Wait Time In Minutes"));
        waitBox = new TextBox();
        waitBox.setWidth("150px");
        add(waitBox);
        add(waiterror);

        updatebtn = new Button("Update Facility");
        updatebtn.addStyleName("btn");
        updatebtn.addClickHandler(event -> submitUpdate());
        add(updatebtn);
    }

    // Fetch Facility Data Array
    public static void loadFacilities(ListBox box){
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "/api/facilities");

        try {
            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() != 200){
                        Window.alert("Failed to Load Facilities");
                        return;
                    }

                    JSONValue parsed = JSONParser.parseStrict(response.getText());
                    JSONArray arr = parsed.isArray();

                    if (arr == null){
                        Window.alert("Invalid Facilities Response");
                        return;
                    }

                    box.clear();
                    box.addItem("Select Facility", "0");

                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject obj = arr.get(i).isObject();
                        if (obj == null) continue;

                        int id = (int) obj.get("id").isNumber().doubleValue();
                        String name = obj.get("name").isString().stringValue();
                        box.addItem(name, String.valueOf(id));
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Window.alert("Server Error Loading Facilities");
                }
            });
        } catch(RequestException e){
            Window.alert("Request Failed");
        }
    }

    // Validate and Submit Update
    private void submitUpdate(){
        Boolean valid = true;

        // Reset Error Colors
        facilityerror.getElement().getStyle().setProperty("color", "red");
        bederror.getElement().getStyle().setProperty("color", "red");
        waiterror.getElement().getStyle().setProperty("color", "red");

        // Clear Error Text
        facilityerror.setText("");
        bederror.setText("");
        waiterror.setText("");

        // Validate Inputs
        if (facilityDrop.getSelectedIndex() == 0){
            valid = false;
            facilityerror.setText("Must Select a Facility");
        }

        if (bedBox.getText().trim().isEmpty()){
            valid = false;
            bederror.setText("Beds Available are Required");
        }

        try {
            int beds = Integer.parseInt(bedBox.getText().trim());
            if (beds < 0) throw new NumberFormatException();
        } catch(NumberFormatException e){
            valid = false;
            bederror.setText("Must Enter Valid Number of Beds Available");
        }

        if (waitBox.getText().trim().isEmpty()){
            valid = false;
            waiterror.setText("Wait Time is Required");
        }

        try {
            int waits = Integer.parseInt(waitBox.getText().trim());
            if (waits < 0) throw new NumberFormatException();
        } catch(NumberFormatException e){
            valid = false;
            waiterror.setText("Must Enter Valid Wait Time");
        }

        // Final Validation Check
        if (!valid){
            Window.alert("Not all Information is Valid. Can't Submit");
        } else {
            updatebtn.setEnabled(false);
            sendUpdateApiRequest();
        }
    }

    // Send API Update Data
    private void sendUpdateApiRequest() {
        String payload = "facilityId=" + URL.encodeQueryString(facilityDrop.getSelectedValue()) +
                "&bedavail=" + URL.encodeQueryString(bedBox.getText().trim()) +
                "&waitTime=" + URL.encodeQueryString(waitBox.getText().trim());

        RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "/api/updateFacility");
        rb.setHeader("Content-Type", "application/x-www-form-urlencoded");

        try {
            rb.sendRequest(payload, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == 200){
                        Window.alert("Facility Updated SuccessFully!");
                        onBack.run();
                    } else {
                        Window.alert("Update Failed: " + response.getText());
                        updatebtn.setEnabled(true);
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Window.alert("Server Connection Error");
                    updatebtn.setEnabled(true);
                }
            });
        } catch(RequestException e){
            Window.alert("Failed to Send Update Request");
            updatebtn.setEnabled(true);
        }
    }
}