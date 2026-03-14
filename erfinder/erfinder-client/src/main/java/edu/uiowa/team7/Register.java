package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.i18n.client.DateTimeFormat;

public class Register {

    //fields
    private final TextBox firstname = new TextBox();
    private final TextBox lastname = new TextBox();
    private final TextBox flegalname = new TextBox();
    private final TextBox phoneNumber = new TextBox();
    private final TextBox email = new TextBox();
    private final TextBox username = new TextBox();
    private final PasswordTextBox password = new PasswordTextBox();
    private final TextBox address = new TextBox();
    private final TextBox zipcode = new TextBox();
    private final DateBox dob = new DateBox();
    private final TextBox gender = new TextBox();
    private final TextBox emcontact = new TextBox();
    private final TextBox driversLicense = new TextBox();
    private final TextBox ssn = new TextBox();
    private final ListBox role = new ListBox();

    public Register() {


        VerticalPanel form = new VerticalPanel();
        form.setSpacing(50);


        //Header
        Label header = new Label("ERFinder Account Signup");
        header.getElement().getStyle().setProperty("fontSize","20px");
        header.getElement().getStyle().setProperty("fontWeight","bold");
        form.add(header);


        //Labels and Fields

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

        form.add(new Label("First Name"));
        form.add(firstname);


        form.add(new Label("Last Name"));
        form.add(lastname);


        form.add(new Label("Full Legal Name"));
        form.add(flegalname);


        form.add(new Label("Phone Number"));
        form.add(phoneNumber);


        form.add(new Label("Email"));
        form.add(email);


        form.add(new Label("Username"));
        form.add(username);


        form.add(new Label("Password"));
        form.add(password);


        form.add(new Label("Address"));
        form.add(address);


        form.add(new Label("ZipCode"));
        form.add(zipcode);


        form.add(new Label("Date of Birth (MM/DD/YYYY)"));
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy");
        dob.setFormat(new DateBox.DefaultFormat(format));
        form.add(dob);


        form.add(new Label("Gender"));
        form.add(gender);


        form.add(new Label("Emergency Contact's Phone Number"));
        form.add(emcontact);


        form.add(new Label("Driver's License Number"));
        form.add(driversLicense);


        form.add(new Label("Social Security Number"));
        form.add(ssn);


        //Button and ClickHandler
        Button submit = new Button("Submit");
        //submit.addClickHandler();
        form.add(submit);

        //form setup and adding to page
        form.setWidth("300px");

        HorizontalPanel wrapper = new HorizontalPanel();
        wrapper.setWidth("100%");
        wrapper.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        wrapper.add(form);

        RootPanel.get("registerContainer").add(wrapper);
    }

    //when user hits submit button the code then checks the entries of stuffs:
    //if everything valid then page changes and says account pending. The information is put together and added
    //to the pending users table in MYSQL for admin to approve or deny
    //if something information is invalid then there is a message by the textbox in red explaining what was wrong

}
