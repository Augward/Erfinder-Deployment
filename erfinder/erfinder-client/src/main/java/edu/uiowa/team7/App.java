package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {

	public static final int HTTP_OK_200 = 200;
	public static final int HTTP_ACCEPTED_202 = 202;
	public static final int HTTP_UNAUTHORIZED_401 = 401;
	public static final int HTTP_NOT_FOUND_404 = 404;
	public static final int HTTP_INTERNAL_SERVER_ERROR_500 = 500;
	// note :: keep around for nav-bar ??
	//private static String userid = "";
	private static boolean logged_in = false;

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

	public static Page GetPage() {
		Element label = RootPanel.get("page").getElement();
		Page inner;
		try {
			inner = Page.valueOf(label.getInnerHTML());
		} catch (IllegalArgumentException e) {
			inner = Page.Landing;
		}
		label.removeFromParent();
		return inner;
	}

	public static void TryRedirect(Page inner) {
		switch (inner) {
			case Landing: // page should adapt based on sign-in
			case Pending:
			case Reset:
				break;
			// users already logged in shouldn't be on these::
			case Login:
			case Forgot:
			case Signup:
				if (logged_in)
					Window.Location.assign("home.html");
				break;
			// users should only be on these if logged in::
			case Profile:
			case Home:
				if (!logged_in)
					Window.Location.assign("login.html");
				break;
			// realistically this script wouldn't be on unknown page,
			// but just to be sure::
			default:
				Window.Location.assign("index.html");
				break;
		}
	}

	// Page Routing
	public static void BuildPage(Page inner) {
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
				new Landing(logged_in);
				break;
		}
	}

	// Initialization
	public void onModuleLoad() {
		// redirect to a more appropriate page depending on sign-in status
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/username");
		try {
			builder.sendRequest("", new LoginCheck());
		} catch (Exception e) {
			// todo :: add error message :)
		}
	}

	// Native JS Methods

	// note :: This is NOT encryption!!!!
	// This is just to prevent stuff like '&'
	// from messing with the parameter encoding
	public static native String B64Encode(String original) /*-{
		return $wnd.btoa(original);
	}-*/;


	private static class LoginCheck implements RequestCallback {
		@Override
		public void onResponseReceived(Request request, Response response) {
			switch (response.getStatusCode()) {
				case HTTP_OK_200:
					//userid = response.getText();
					logged_in = true;
					break;
				case HTTP_UNAUTHORIZED_401:
					logged_in = false;
					break;
			}

			Page inner = GetPage();
			TryRedirect(inner);
			BuildPage(inner);
		}

		@Override
		public void onError(Request request, Throwable throwable) {
			// todo :: add error message :)
		}
	}
}