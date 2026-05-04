package edu.uiowa.team7;

import com.google.gwt.user.client.ui.RootPanel;

public class Landing {

    // Configure Navbar Authentication View
    public Landing(boolean logged_in) {
        if (logged_in) {
            RootPanel.get("navbar-yes-auth").getElement().removeAttribute("hidden");
        } else {
            RootPanel.get("navbar-no-auth").getElement().removeAttribute("hidden");
        }
    }

}