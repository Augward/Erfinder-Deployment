package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {

	public enum Page {
		Index,
		Login,
		Forgot,
		Register,
		Home,
		Info,
	}

	// Page Routing
	public void setPage() {
		Element label = RootPanel.get("page").getElement();
		Page inner;
		try {
			inner = Page.valueOf(label.getInnerHTML());
		} catch (IllegalArgumentException e) {
			inner = Page.Index;
		}
		label.removeFromParent();

		switch (inner) {
			case Login:
				new Login();
				break;
			case Forgot:
				new Forgot();
				break;
			case Register:
				new Register();
				break;
			case Home:
				new Home();
				break;
			case Info:
				new Info();
				break;
			case Index:
			default: // Added default
				Index.Build();
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