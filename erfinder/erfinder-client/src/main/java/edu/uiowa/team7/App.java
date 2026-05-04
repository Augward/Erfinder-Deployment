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

	// HTTP Status Constants
	public static final int HTTP_OK_200 = 200;
	public static final int HTTP_ACCEPTED_202 = 202;
	public static final int HTTP_UNAUTHORIZED_401 = 401;
	public static final int HTTP_NOT_FOUND_404 = 404;
	public static final int HTTP_INTERNAL_SERVER_ERROR_500 = 500;

	// Auth State Tracker
	private static boolean logged_in = false;

	// Available Application Pages
	public enum Page {
		Landing, Login, Forgot, Signup, Home, Profile, Pending, Reset
	}

	// Parse Current HTML Page
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

	// Handle Authentication Redirects
	public static void TryRedirect(Page inner) {
		switch (inner) {
			case Landing:
			case Pending:
				break;

			case Reset:
				if (logged_in) {
					Window.Location.assign("home.html");
				} else {
					String token = Window.Location.getParameter("t");
					if (token == null || token.isEmpty()) {
						Window.Location.assign("landing.html");
					}
				}
				break;

			case Login:
			case Forgot:
			case Signup:
				if (logged_in) Window.Location.assign("home.html");
				break;

			case Profile:
			case Home:
				if (!logged_in) Window.Location.assign("login.html");
				break;

			default:
				Window.Location.assign("landing.html");
				break;
		}
	}

	// Construct Page Logic
	public static void BuildPage(Page inner) {
		switch (inner) {
			case Login: new Login(); break;
			case Forgot: new Forgot(); break;
			case Signup: new Signup(); break;
			case Home: new Home(); break;
			case Profile: new Profile(); break;
			case Pending: new Pending(); break;
			case Reset: new Reset(); break;
			case Landing:
			default: new Landing(logged_in); break;
		}
	}

	// Application Initialization Entry
	public void onModuleLoad() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "/api/username");
		try {
			builder.sendRequest("", new LoginCheck());
		} catch (Exception e) {
			// Handle error
		}
	}

	// Base64 Encode Native JS
	public static native String B64Encode(String original) /*-{
		return $wnd.btoa(original);
	}-*/;

	// Base64 Decode Native JS
	public static native String B64Decode(String original) /*-{
		return $wnd.atob(original);
	}-*/;

	// Validate Auth Status Payload
	private static class LoginCheck implements RequestCallback {
		@Override
		public void onResponseReceived(Request request, Response response) {
			switch (response.getStatusCode()) {
				case HTTP_OK_200:
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
			// Handle error
		}
	}
}