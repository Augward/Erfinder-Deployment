package edu.uiowa.team7;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.i18n.client.DateTimeFormat;

public class Register {

    // Fields
    private final TextBox firstname = new TextBox();
    private final TextBox lastname = new TextBox();
    private final TextBox legalname = new TextBox();
    private final TextBox phoneNumber = new TextBox();
    private final TextBox email = new TextBox();
    private final TextBox username = new TextBox();
    private final PasswordTextBox password = new PasswordTextBox();
    private final TextBox address = new TextBox();
    private final TextBox zipcode = new TextBox();
    private final DateBox dob = new DateBox();
    private final ListBox gender = new ListBox();
    private final TextBox emergency = new TextBox();
    private final TextBox driversLicense = new TextBox();
    private final TextBox ssn = new TextBox();
    private final ListBox role = new ListBox();
    private final TextBox securityAns = new TextBox();
    private final ListBox securityQues = new ListBox();

    // Error Labels
    private final Label firstnameerror = new Label();
    private final Label lastnameerror = new Label();
    private final Label legalnameerror = new Label();
    private final Label phoneNumbererror = new Label();
    private final Label emailerror = new Label();
    private final Label usernameerror = new Label();
    private final Label passworderror = new Label();
    private final Label addresserror = new Label();
    private final Label zipcodeerror = new Label();
    private final Label doberror = new Label();
    private final Label gendererror = new Label();
    private final Label emergencyerror = new Label();
    private final Label driversLicenseerror = new Label();
    private final Label ssnerror = new Label();
    private final Label roleerror = new Label();
    private final Label securityAnserror = new Label();
    private final Label securityQueserror = new Label();

    // Submission Button
    private final Button submit = new Button("Submit");

    // Constructor
    public Register() {

        // Set error fields red
        firstnameerror.getElement().getStyle().setProperty("color", "red");
        lastnameerror.getElement().getStyle().setProperty("color", "red");
        legalnameerror.getElement().getStyle().setProperty("color", "red");
        phoneNumbererror.getElement().getStyle().setProperty("color", "red");
        emailerror.getElement().getStyle().setProperty("color", "red");
        usernameerror.getElement().getStyle().setProperty("color", "red");
        passworderror.getElement().getStyle().setProperty("color", "red");
        addresserror.getElement().getStyle().setProperty("color", "red");
        zipcodeerror.getElement().getStyle().setProperty("color", "red");
        doberror.getElement().getStyle().setProperty("color", "red");
        gendererror.getElement().getStyle().setProperty("color", "red");
        emergencyerror.getElement().getStyle().setProperty("color", "red");
        driversLicenseerror.getElement().getStyle().setProperty("color", "red");
        ssnerror.getElement().getStyle().setProperty("color", "red");
        roleerror.getElement().getStyle().setProperty("color", "red");
        securityAnserror.getElement().getStyle().setProperty("color", "red");
        securityQueserror.getElement().getStyle().setProperty("color", "red");

        VerticalPanel form = new VerticalPanel();
        form.setSpacing(30);

        // Header Section
        Label header = new Label("ERFinder Account Signup");
        header.getElement().getStyle().setProperty("fontSize","20px");
        header.getElement().getStyle().setProperty("fontWeight","bold");
        form.add(header);

        // Roles
        form.add(new Label("Role"));
        role.addItem("Select Role: ", " ");
        role.addItem("Patient", "PATIENT");
        role.addItem("Doctor","DOCTOR");
        role.addItem("Nurse", "NURSE");
        role.addItem("EMT", "EMT");
        role.addItem("Admin", "ADMIN");
        role.getElement().getStyle().setProperty("width", "200px");
        role.getElement().getStyle().setProperty("height", "30px");
        form.add(role);
        form.add(roleerror);

        // Personal Information Fields
        form.add(new Label("First Name"));
        form.add(firstname);
        form.add(firstnameerror);

        form.add(new Label("Last Name"));
        form.add(lastname);
        form.add(lastnameerror);

        form.add(new Label("Full Legal Name"));
        form.add(legalname);
        form.add(legalnameerror);

        form.add(new Label("Phone Number"));
        form.add(phoneNumber);
        form.add(phoneNumbererror);

        form.add(new Label("Email"));
        form.add(email);
        form.add(emailerror);

        // Account Details Fields
        form.add(new Label("Username"));
        form.add(username);
        form.add(usernameerror);

        form.add(new Label("Password"));
        form.add(password);
        form.add(passworderror);

        form.add(new Label("Address"));
        form.add(address);
        form.add(addresserror);

        form.add(new Label("ZipCode"));
        form.add(zipcode);
        form.add(zipcodeerror);

        form.add(new Label("Date of Birth (MM/DD/YYYY)"));
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy");
        dob.setFormat(new DateBox.DefaultFormat(format));
        form.add(dob);
        form.add(doberror);

        form.add(new Label("Gender"));
        gender.addItem("Select Gender: ", " ");
        gender.addItem("Female", "FEMALE");
        gender.addItem("Male", "MALE");
        gender.addItem("Other", "OTHER");
        gender.getElement().getStyle().setProperty("width", "200px");
        gender.getElement().getStyle().setProperty("height", "30px");
        form.add(gender);
        form.add(gendererror);

        form.add(new Label("Emergency Contact's Phone Number"));
        form.add(emergency);
        form.add(emergencyerror);

        form.add(new Label("Driver's License Number"));
        form.add(driversLicense);
        form.add(driversLicenseerror);

        form.add(new Label("Social Security Number"));
        form.add(ssn);
        form.add(ssnerror);

        // Security Configuration Fields
        form.add(new Label("Security Question"));
        securityQues.addItem("Select Security Question", " ");
        securityQues.addItem("What is your mother's maiden name?", "SQ1");
        securityQues.addItem("What is the name of your hometown?","SQ2");
        securityQues.addItem("What was your childhood nickname?", "SQ3");
        securityQues.addItem("What is your Dad's middle name?", "SQ4");
        securityQues.addItem("What is your favorite teacher's name?", "SQ5");
        securityQues.addItem("What is your favorite book?", "SQ6");
        securityQues.addItem("What is your favorite food?", "SQ7");
        securityQues.addItem("What was your dream job as a child?", "SQ8");
        securityQues.getElement().getStyle().setProperty("width", "200px");
        securityQues.getElement().getStyle().setProperty("height", "30px");
        form.add(securityQues);
        form.add(securityQueserror);

        form.add(new Label("Security Answer"));
        form.add(securityAns);
        form.add(securityAnserror);

        // Submit Button ClickHandler
        submit.addClickHandler(clickEvent -> {
            boolean valid = true;

            // Clear error fields
            roleerror.setText("");
            firstnameerror.setText("");
            lastnameerror.setText("");
            legalnameerror.setText("");
            phoneNumbererror.setText("");
            emailerror.setText("");
            addresserror.setText("");
            zipcodeerror.setText("");
            doberror.setText("");
            gendererror.setText("");
            emergencyerror.setText("");
            usernameerror.setText("");
            passworderror.setText("");
            driversLicenseerror.setText("");
            ssnerror.setText("");
            securityQueserror.setText("");
            securityAnserror.setText("");

                //validate role
                if(role.getSelectedIndex()==0){
                    valid = false;
                    roleerror.setText("Must Select a Role");
                }

                //validate firstname
                if(firstname.getText().trim().isEmpty()){
                    valid = false;
                    firstnameerror.setText("First Name Required");
                }

                //validate lastname
                if(lastname.getText().trim().isEmpty()){
                    valid = false;
                    lastnameerror.setText("Last Name Required");
                }

                //validate legalname
                if(legalname.getText().trim().isEmpty()){
                    valid = false;
                    legalnameerror.setText("Full Legal Name Required");
                }

                //validate phoneNumber
                if(phoneNumber.getText().trim().isEmpty()){
                    valid = false;
                    phoneNumbererror.setText("Phone Number Required");
                }
                else if(!phoneNumber.getText().trim().matches("\\d{10}")){
                    valid = false;
                    phoneNumbererror.setText("Phone Number Must be 10 Digits");
                }


                //validate email
                if(email.getText().trim().isEmpty()){
                    valid = false;
                    emailerror.setText("Email is Required");
                }
                else if(!email.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")){
                    valid = false;
                    emailerror.setText("Email Must Be Valid");
                }

                //validate username
                if(username.getText().trim().isEmpty()){
                    valid = false;
                    usernameerror.setText("Username Required");
                }

                //validate password
                if(password.getText().trim().isEmpty()){
                    valid = false;
                    passworderror.setText("Password is Required");
                }
                else if(!password.getText().trim().matches("^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$")){
                    valid = false;
                    passworderror.setText("Password Must Be At Least 8 Digits Long With A Letter and Number");
                }

                //validate address
                if(address.getText().trim().isEmpty()){
                    valid = false;
                    addresserror.setText("Address is Required");
                }

                //validate zipcode
                if(zipcode.getText().trim().isEmpty()){
                    valid = false;
                    zipcodeerror.setText("Zipcode is Required");
                }
                else if (!zipcode.getText().trim().matches("\\d{5}")){
                    valid = false;
                    zipcodeerror.setText("Zipcode Must Be 5 Digits");
                }

                //validate dob
                if(dob.getValue() == null){
                    valid = false;
                    doberror.setText("Date of Birth is Required");
                }

                //validate gender
                if(gender.getSelectedIndex() == 0){
                    valid = false;
                    gendererror.setText("Must Select a Gender");
                }

                //validate emergency
                if(emergency.getText().trim().isEmpty()){
                    valid = false;
                    emergencyerror.setText("Emergency Contact's Phone Number Required");
                }
                else if(!emergency.getText().trim().matches("\\d{10}")){
                    valid = false;
                    emergencyerror.setText("Emergency Contact's Phone Number Must be 10 Digits");
                }

                //validate driversLicense
                if(driversLicense.getText().trim().isEmpty()){
                    valid = false;
                    driversLicenseerror.setText("Driver's License Number Required");
                }
                else if(!driversLicense.getText().trim().matches("[A-Za-z0-9]{5,15}")){
                    valid = false;
                    driversLicenseerror.setText("Driver's License Number Must Be Valid");
                }

                //validate ssn
                if(ssn.getText().trim().isEmpty()){
                    valid = false;
                    ssnerror.setText("Social Security Number Required");
                }
                else if (!ssn.getText().trim().matches("\\d{9}")){
                    valid = false;
                    ssnerror.setText("Social Security Number Must Be 9 Digits");
                }
                else if(ssn.getText().trim().startsWith("000") || ssn.getText().trim().startsWith("00", 3) || ssn.getText().trim().substring(5).equals("0000")){
                    valid = false;
                    ssnerror.setText("Social Security Number Must Be Valid");
                }

                //validate security question
                if(securityQues.getSelectedIndex() == 0){
                    valid = false;
                    securityQueserror.setText("Must Select a Security Question");
                }

                //validate security answer
                if(securityAns.getText().trim().isEmpty()){
                    valid = false;
                    securityAnserror.setText("Security Answer is Required");
                }

            if(!valid){
                Window.alert("Not all Information is Valid. Can't Submit");
            } else {
                submit.setEnabled(false);
                checkExistingAccounts();
            }
        });

        form.add(submit);

        // Form setup and adding to page
        form.setWidth("300px");

        HorizontalPanel wrapper = new HorizontalPanel();
        wrapper.setWidth("100%");
        wrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        wrapper.add(form);
        RootPanel.get("registerContainer").add(wrapper);
    }

    // Validation & Submission Methods
    private void checkExistingAccounts(){
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/register/check");
        builder.setHeader("Content-Type", "application/json");

        String json = "{" + "\"username\":\"" + username.getText().trim() + "\","
                + "\"email\":\"" + email.getText().trim() + "\","
                + "\"ssn\":\"" + ssn.getText().trim() + "\","
                + "\"driversLicense\":\"" + driversLicense.getText().trim() + "\"" + "}";

        try{
            builder.sendRequest(json, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response res) {
                    if(res.getStatusCode() == 200){
                        String response = res.getText();
                        handleDuplicateResponse(response);
                    } else {
                        Window.alert("Server Error");
                    }
                }

                @Override
                public void onError(Request req, Throwable e) {
                    submit.setEnabled(true);
                    Window.alert("Unable to Connect to Server");
                }
            });
        } catch (RequestException e){
            Window.alert("Request Failed");
        }
    }

    private void handleDuplicateResponse(String json){
        boolean valid = true;
        if(json.contains("\"usernameExists\":true")){
            valid = false;
            usernameerror.setText("Username Already In Use");
        }

        if(json.contains("\"emailExists\":true")){
            valid = false;
            emailerror.setText("Email Already Has An Account");
        }

        if(json.contains("\"ssnExists\":true")){
            valid = false;
            ssnerror.setText("Social Security Number Already Has An Account");
        }

        if(json.contains("\"licenseExists\":true")){
            valid = false;
            driversLicenseerror.setText("Driver's License Already Has An Account");
        }

        if(valid){
            submitRegistration();
        } else {
            submit.setEnabled(true);
            Window.alert("Can't Submit Because Account Already Exists For Some Entered Fields");
        }
    }

    private void submitRegistration(){
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, "/register/create");
        builder.setHeader("Content-Type", "application/json");

        DateTimeFormat serverFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
        String dobFormatted = serverFormat.format(dob.getValue());

        String json = "{"
                + "\"username\":\"" + username.getText().trim() + "\","
                + "\"password\":\"" + password.getText().trim() + "\","
                + "\"securityQuestion\":\"" + securityQues.getValue(securityQues.getSelectedIndex()) + "\","
                + "\"securityAnswer\":\"" + securityAns.getText().trim() + "\","
                + "\"firstName\":\"" + firstname.getText().trim() + "\","
                + "\"lastName\":\"" + lastname.getText().trim() + "\","
                + "\"legalName\":\"" + legalname.getText().trim() + "\","
                + "\"phone\":\"" + phoneNumber.getText().trim() + "\","
                + "\"email\":\"" + email.getText().trim() + "\","
                + "\"address\":\"" + address.getText().trim() + "\","
                + "\"zipcode\":\"" + zipcode.getText().trim() + "\","
                + "\"dob\":\"" + dobFormatted + "\","
                + "\"gender\":\"" + gender.getValue(gender.getSelectedIndex()) + "\","
                + "\"emergency\":\"" + emergency.getText().trim() + "\","
                + "\"driversLicense\":\"" + driversLicense.getText().trim() + "\","
                + "\"ssn\":\"" + ssn.getText().trim() + "\","
                + "\"role\":\"" + role.getValue(role.getSelectedIndex()) + "\""
                + "}";

        try{
            builder.sendRequest(json, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    submit.setEnabled(true);
                    if(response.getStatusCode() == 200){
                        String respText = response.getText();
                        if(respText.contains("\"status\":\"success\"")){
                            Window.alert("Account Successfully Submitted!");
                        } else {
                            Window.alert("Submission Failed: " + respText);
                        }
                    } else {
                        Window.alert("Server Error");
                    }
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    submit.setEnabled(true);
                    Window.alert("Unable to Connect to Server");
                }
            });
        } catch(RequestException e) {
            submit.setEnabled(true);
            Window.alert("Request Failed");
        }
    }
}