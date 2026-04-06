package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {

	public enum Page {
		Landing,
		Login,
		Forgot,
		Signup,
		Home,
		Profile,
		Pending,
		Reset
	}

	// Page Routing
	public void setPage() {
		Element label = RootPanel.get("page").getElement();
		Page inner;
		try {
			inner = Page.valueOf(label.getInnerHTML());
		} catch (IllegalArgumentException e) {
			inner = Page.Landing;
		}
		label.removeFromParent();

		switch (inner) {
			case Login:
				new Login();
				break;
			case Forgot:
				new Forgot();
				break;
			case Signup:
				new Signup();
				break;
			case Home:
				new Home();
				break;
			case Profile:
				new Profile();
				break;
			case Pending:
				new Pending();
				break;
			case Reset:
				new Reset();
				break;
			case Landing:
			default: // Added default
				Landing.Build();
				break;
		}
	}

	// Initialization
	public void onModuleLoad() {
		setPage();
	}

	// Native JS Methods
	public static native String B64Encode(String original) /*-{
		return $wnd.btoa(original);
	}-*/;

	public static native void Redirect(String url) /*-{
		$wnd.location = url;
	}-*/;
}