package edu.uiowa.team7;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.RootPanel;

public class App implements EntryPoint {
	public enum Page {
		Index,
		Login
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
				Login.Build();
				break;
		}
	}

	public void onModuleLoad() {
		SetPage();
	}
}