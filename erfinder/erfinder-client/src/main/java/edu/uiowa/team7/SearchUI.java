package edu.uiowa.team7;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.http.client.*;

import java.util.Arrays;

public class SearchUI extends VerticalPanel  {

    // UI Routing Navigation Pointer
    private Runnable onBack;

    // Computed Array List Index
    int[] facilitypks;

    // Constructor Entry Routine Generation
    public SearchUI(int esiScore, int injury, int priority, Runnable onBack){
        this.onBack = onBack;

        setSpacing(10);
        setWidth("100%");

        String results = GetSearchPK(esiScore, injury, priority);

        if (results.isEmpty()) {
            facilitypks = new int[0];
        }
        else {
            String[] pks = results.split(",");
            facilitypks = new int[pks.length];
            for (int i = 0; i < facilitypks.length; i ++) {
                facilitypks[i] = Integer.parseInt(pks[i]);
            }
        }

        Button backbtn = new Button(" <- Back to DashBoard");
        backbtn.addClickHandler(event -> { Backout(); onBack.run();});
        add(backbtn);

        add(new HTML("<h3>ER Search Results</h3>"));
        loadSearchResults();
    }

    public static native String GetSearchPK(int esi, int inj, int pri) /*-{
        return $wnd.get_search_pk(esi, inj, pri, true);
    }-*/;

    public static native void Backout() /*-{
        $wnd.return_to_input();
    }-*/;

    // Call Endpoints Passing Primary Array Array Values
    private void loadSearchResults(){
        if (facilitypks == null || facilitypks.length == 0){
            add(new Label("No ER Facilities found"));
            return;
        }

        StringBuilder pkString = new StringBuilder();

        for (int i = 0; i < facilitypks.length; i++){
            if (i > 0) pkString.append(",");
            pkString.append(facilitypks[i]);
        }

        String url = "/api/search?pks=" + pkString.toString();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            builder.sendRequest(null, new RequestCallback(){
                @Override
                public void onResponseReceived(Request request, Response response){
                    if (response.getStatusCode() == 200){
                        displayFacilities(response.getText());
                    } else {
                        add(new Label("Failed to load results"));
                    }
                }

                @Override
                public void onError(Request request, Throwable exception){
                    add(new Label("Error connecting to server"));
                }
            });
        } catch (RequestException e){
            add(new Label("Request failed"));
        }
    }

    // Organize String Array Return Content For Graphics Block Output
    private void displayFacilities(String json){
        json = json.replace("[", "").replace("]", "");

        if (json.trim().isEmpty()){
            add(new Label("No results returned"));
            return;
        }

        String[] items = json.split("},");

        for (int i = 0; i < items.length; i++){
            String item = items[i]
                    .replace("{", "")
                    .replace("}", "");

            VerticalPanel card = new VerticalPanel();
            card.setSpacing(5);

            if (i == 0){
                card.add(new HTML("<b>Best Match</b>"));
            } else {
                card.add(new HTML("<b>Result #" + (i + 1) + "</b>"));
            }

            String[] fields = item.split(",");

            for (String f : fields){
                String[] kv = f.split(":");

                if (kv.length == 2){
                    String key = kv[0].replace("\"", "");
                    String value = kv[1].replace("\"", "");

                    if (key.equals("wait")){
                        card.add(new Label ("wait time: " + value + " mins"));
                    }
                    else if (key.equals("beds")){
                        card.add(new Label("beds available: " + value));
                    }
                    else if (key.equals("name")){
                        card.add(new HTML("<b>" + value + "</b>"));
                    }
                    else {
                        card.add(new Label(key + ": " + value));
                    }
                }
            }

            add(card);
        }
    }
}
