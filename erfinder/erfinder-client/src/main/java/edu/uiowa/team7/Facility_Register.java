package edu.uiowa.team7;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class Facility_Register extends VerticalPanel {
    private Runnable onBack;

    //form fields
    private TextBox ername;
    private TextBox phonenum;
    private TextBox address;
    private TextBox zip;
    private ListBox traumalevel;
    private TextArea specialties;
    private TextBox bedavail;
    private TextBox waitTime;

    private final Label ernameerror = new Label();
    private final Label phonenumerror = new Label();
    private final Label addresserror = new Label();
    private final Label ziperror = new Label();
    private final Label traumaerror = new Label();
    private final Label specialerror = new Label();
    private final Label bedavailerror = new Label();
    private final Label waiterror = new Label();

    private Button submitbtn;

    public Facility_Register(Runnable onBack){
        this.onBack = onBack;
        setSpacing(10);
        setWidth("100%");

        Button backbtn = new Button(" <- Back to DashBoard");
        backbtn.addClickHandler(event -> onBack.run());
        add(backbtn);
        add(new HTML("<h3>Register ER Facility</h3>"));
        buildForm();
    }

    private void buildForm(){
        Label ernamelab = new Label("Enter ER Name: ");
        ername = new TextBox();
        ername.setWidth("300px");
        add(ernamelab);
        add(ername);
        add(ernameerror);

        Label phonenumlab = new Label("Enter ER Phone Number: ");
        phonenum = new TextBox();
        phonenum.setWidth("300px");
        add(phonenumlab);
        add(phonenum);
        add(phonenumerror);

        Label addresslab = new Label("Enter ER Address: ");
        address = new TextBox();
        address.setWidth("300px");
        add(addresslab);
        add(address);
        add(addresserror);

        Label ziplab = new Label("Enter ER zipcode: ");
        zip = new TextBox();
        zip.setWidth("300px");
        add(ziplab);
        add(zip);
        add(ziperror);

        Label traumalab = new Label("Enter Trauma Level: ");
        traumalevel = new ListBox();
        traumalevel.addItem("Select Trauma Level", "0");
        traumalevel.addItem("I", "1");
        traumalevel.addItem("II", "2");
        traumalevel.addItem("III", "3");
        traumalevel.addItem("IV", "4");
        traumalevel.addItem("V", "5");
        add(traumalab);
        add(traumalevel);
        add(traumaerror);

        Label speciallab = new Label("Enter ER Specialties: ");
        specialties = new TextArea();
        specialties.setWidth("300px");
        specialties.setVisibleLines(3);
        specialties.getElement().setPropertyString("placeholder","Example: Cardiac, Pediatric, Stroke, etc");
        add(speciallab);
        add(specialties);
        add(specialerror);

        Label bedavaillab = new Label("Enter ER Beds Available: ");
        bedavail = new TextBox();
        bedavail.setWidth("100px");
        add(bedavaillab);
        add(bedavail);
        add(bedavailerror);

        Label waitlab = new Label("Enter ER Wait Time In Minutes: ");
        waitTime = new TextBox();
        waitTime.setWidth("100px");
        add(waitlab);
        add(waitTime);
        add(waiterror);

        submitbtn = new Button("Submit ER Facility");
        submitbtn.addStyleName("btn");
        add(submitbtn);
        submitbtn.addClickHandler(e->handleSubmit());
    }

    private void handleSubmit(){
        boolean valid = true;

        //make labels red
        ernameerror.getElement().getStyle().setProperty("color", "red");
        phonenumerror.getElement().getStyle().setProperty("color", "red");
        addresserror.getElement().getStyle().setProperty("color", "red");
        ziperror.getElement().getStyle().setProperty("color", "red");
        traumaerror.getElement().getStyle().setProperty("color", "red");
        specialerror.getElement().getStyle().setProperty("color", "red");
        bedavailerror.getElement().getStyle().setProperty("color", "red");
        waiterror.getElement().getStyle().setProperty("color", "red");

        //clear error labels
        ernameerror.setText("");
        phonenumerror.setText("");
        addresserror.setText("");
        ziperror.setText("");
        traumaerror.setText("");
        specialerror.setText("");
        bedavailerror.setText("");
        waiterror.setText("");

        //validate name
        if(ername.getText().trim().isEmpty()){
            valid = false;
            ernameerror.setText("ER Name is Required");
        }

        //validate phone number
        if(phonenum.getText().trim().isEmpty()){
            valid = false;
            phonenumerror.setText("Phone Number Required");
        }
        else if(!phonenum.getText().trim().matches("\\d{10}")){
            valid = false;
            phonenumerror.setText("Phone Number Must be 10 Digits");
        }

        //validate address
        if(address.getText().trim().isEmpty()){
            valid = false;
            addresserror.setText("ER Address is Required");
        }

        //validate zip
        if(zip.getText().trim().isEmpty()){
            valid = false;
            ziperror.setText("Zipcode is Required");
        }
        else if (!zip.getText().trim().matches("\\d{5}")){
            valid = false;
            ziperror.setText("Zipcode Must Be 5 Digits");
        }

        //valdiate trauma
        if(traumalevel.getSelectedIndex() == 0){
            valid = false;
            traumaerror.setText("Must Select A Trauma Level");
        }

        //validate special
        if(specialties.getText().trim().isEmpty()){
            valid = false;
            specialerror.setText("Specialties Are Required");
        }

        //validate bedavail
        if(bedavail.getText().trim().isEmpty()){
            valid = false;
            bedavailerror.setText("Must Enter Amount of Beds Available");
        }

        int beds = -1;
        try{
            beds = Integer.parseInt(bedavail.getText().trim());
            if(beds < 0){
                throw new NumberFormatException();
            }
        }
        catch(NumberFormatException e){
            valid = false;
            bedavailerror.setText("Must Enter a Valid Number Of Beds Available");
        }

        //validate waiterror
        if(waitTime.getText().trim().isEmpty()){
            valid = false;
            waiterror.setText("Must Enter ER Wait Time");
        }

        int waits = -1;
        try{
            waits = Integer.parseInt(waitTime.getText().trim());
            if(waits < 0){
                throw new NumberFormatException();
            }
        }
        catch(NumberFormatException e){
            valid = false;
            waiterror.setText("Must Enter a Valid Wait Time");
        }

        if(!valid){
            Window.alert("Not all Information is Valid. Can't Submit");
        }
        else{
            submitbtn.setEnabled(false);
            String payload = "ername=" + URL.encodeQueryString(ername.getText().trim()) +
                            "&phonenum=" + URL.encodeQueryString(phonenum.getText().trim()) +
                            "&address=" + URL.encodeQueryString(address.getText().trim()) +
                            "&zip=" + URL.encodeQueryString(zip.getText().trim()) +
                            "&traumalevel=" + URL.encodeQueryString(traumalevel.getSelectedItemText()) +
                            "&specialties=" + URL.encodeQueryString(specialties.getText().trim()) +
                            "&bedavail=" + URL.encodeQueryString(bedavail.getText().trim()) +
                            "&waitTime=" + URL.encodeQueryString(waitTime.getText().trim());

            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/api/addfacility");
            builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try{
                builder.sendRequest(payload, new com.google.gwt.http.client.RequestCallback(){
                    @Override
                    public void onResponseReceived(
                            com.google.gwt.http.client.Request request,
                            com.google.gwt.http.client.Response response
                    ){
                        if(response.getStatusCode() == 200){
                            Window.alert("ER Facility Successfully Registered!");
                            onBack.run();
                        }
                        else{
                            Window.alert("Failed To Register Facility" + response.getText());
                            submitbtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(com.google.gwt.http.client.Request request, Throwable exception){
                        Window.alert("Server Connection Error");
                        submitbtn.setEnabled(true);
                    }
                });
            } catch(Exception e){
                Window.alert("Unexpected Error Submitting Form");
                submitbtn.setEnabled(true);
            }
        }
    }
}
