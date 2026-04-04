package edu.uiowa.team7;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Facility_Register extends VerticalPanel {
    private Runnable onBack;
    public Facility_Register(Runnable onBack){
        this.onBack = onBack;
        setSpacing(10);
        setWidth("100%");

        Button backbtn = new Button(" <- ");
        backbtn.addClickHandler(event -> onBack.run());
        add(backbtn);
        add(new HTML("<h3>Register ER Facility</h3>"));
        buildForm();
    }

    private void buildForm(){
        //form stuff here
    }
}
