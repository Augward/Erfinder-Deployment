package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;



public class FacilityUpdate extends VerticalPanel {
    private final Runnable onBack;

    private ListBox facilityDrop;
    private TextBox bedBox;
    private TextBox waitBox;
    private Button updatebtn;

    private final Label facilityerror = new Label();
    private final Label bederror = new Label();
    private final Label waiterror = new Label();


    public FacilityUpdate(Runnable onBack){
        this.onBack = onBack;

        setSpacing(10);
        setWidth("100%");

        Button backbtn = new Button(" <- Back to DashBoard");
        backbtn.addClickHandler(event -> onBack.run());
        add(backbtn);
        add(new HTML("<h3>Update ER Facility</h3>"));
        buildUI();
        loadFacilities();
    }

    private void buildUI(){
        facilityDrop = new ListBox();
        add(new Label("Select ER Facility"));
        add(facilityDrop);
        add(facilityerror);

        bedBox = new TextBox();
        bedBox.setWidth("150px");
        add(new Label ("Beds Available "));
        add(bedBox);
        add(bederror);

        waitBox = new TextBox();
        waitBox.setWidth("150px");
        add(new Label ("Wait Time In Minutes"));
        add(waitBox);
        add(waiterror);

        updatebtn = new Button("Update Facility");
        updatebtn.addStyleName("btn");
        add(updatebtn);

        updatebtn.addClickHandler(event -> submitUpdate());

    }

    private void loadFacilities(){
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, "/api/facilities");
        try{
            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if(response.getStatusCode() != 200){
                        Window.alert("Failed to Load Facilities");
                        return;
                    }

                    JSONValue parsed = JSONParser.parseStrict(response.getText());

                    JSONArray arr = parsed.isArray();
                    if(arr == null){
                        Window.alert("Invalid Facilities Response");
                        return;
                    }

                    facilityDrop.clear();
                    facilityDrop.addItem("Select Facility", "0");

                    for(int i = 0; i < arr.size(); i++) {
                        JSONObject obj = arr.get(i).isObject();
                        if (obj == null) {
                            continue;
                        }

                        int id = (int) obj.get("id").isNumber().doubleValue();
                        String name = obj.get("name").isString().stringValue();

                        facilityDrop.addItem(name, String.valueOf(id));
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    Window.alert("Server Error Loading Facilities");
                }
            });
        }
        catch(RequestException e){
            Window.alert("Request Failed");
        }
    }

    private void submitUpdate(){
        Boolean valid = true;
        //make error labels red
        facilityerror.getElement().getStyle().setProperty("color", "red");
        bederror.getElement().getStyle().setProperty("color", "red");
        waiterror.getElement().getStyle().setProperty("color", "red");

        //clear error labels
        facilityerror.setText("");
        bederror.setText("");
        waiterror.setText("");

        //validate facilitydrop
        if(facilityDrop.getSelectedIndex() == 0){
            valid = false;
            facilityerror.setText("Must Select a Facility");
        }

        //validate bedbox
        if(bedBox.getText().trim().isEmpty()){
            valid = false;
            bederror.setText("Beds Available are Required");
        }

        int beds;
        try{
            beds = Integer.parseInt(bedBox.getText().trim());
            if(beds < 0){throw new NumberFormatException();}
        }
        catch(NumberFormatException e){
            valid = false;
            bederror.setText("Must Enter Valid Number of Beds Available");
        }

        //validate waitbox
        if(waitBox.getText().trim().isEmpty()){
            valid = false;
            waiterror.setText("Wait Time is Required");
        }

        int waits;
        try{
            waits = Integer.parseInt(waitBox.getText().trim());
            if(waits < 0){throw new NumberFormatException();}
        }
        catch(NumberFormatException e){
            valid = false;
            waiterror.setText("Must Enter Valid Wait Time");
        }

        if(!valid){
            Window.alert("Not all Information is Valid. Can't Submit");
        }
        else{
            updatebtn.setEnabled(false);

            String payload =
                    "facilityId=" + URL.encodeQueryString(facilityDrop.getSelectedValue()) +
                            "&bedavail=" + URL.encodeQueryString(bedBox.getText().trim()) +
                            "&waitTime=" + URL.encodeQueryString(waitBox.getText().trim());

            RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "/api/updateFacility");

            rb.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try{
                rb.sendRequest(payload, new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        if(response.getStatusCode() == 200){
                            Window.alert("Facility Updated SuccessFully!");
                            onBack.run();
                        }
                        else{
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
            }
            catch(RequestException e){
                Window.alert("Failed to Send Update Request");
                updatebtn.setEnabled(true);
            }
        }
    }

}
