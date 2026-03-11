package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {
	public enum Page {
		Index,
		Login,
		Forgot,
		Home,
		Info
	}

	public void SetPage() {
		Element label = RootPanel.get("page").getElement();
		Page inner;
		try {
			inner = Page.valueOf(label.getInnerHTML());
		} catch (IllegalArgumentException e) {
			inner = Page.Index;
		}
		label.removeFromParent();

		switch (inner) {
			case Index:
				Index.Build();
				break;
			case Login:
				Login loginPage = new Login();
				break;
			case Forgot:
				Forgot forgotPage = new Forgot();
				break;
            case Home:
                Home homePage = new Home();
                break;
            case Info:
                Info infoPage = new Info();
		}
	}

	public void onModuleLoad() {
		SetPage();
	}

	public static native String B64Encode(String original) /*-{
		return $wnd.btoa(original);
	}-*/;

	public static native void Redirect(String url) /*-{
		$wnd.location = url;
	}-*/;
}