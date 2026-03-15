package edu.uiowa.team7;

import com.google.gwt.user.client.ui.RootPanel;

public class Index {

    public Index(boolean logged_in) {
        // the navbar should respond to log-in status
        if (logged_in) {
            RootPanel.get("navbar-yes-auth").getElement().removeAttribute("hidden");
        }
        else {
            RootPanel.get("navbar-no-auth").getElement().removeAttribute("hidden");
        }
    }
}