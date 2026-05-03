package edu.uiowa.team7;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.http.client.*;

public class SearchUI extends VerticalPanel  {

    // UI Routing Navigation Pointer
    private Runnable onBack;

    // Computed Array List Index
    int[] facilitypks = new int[] {1, 2, 3};

    // Constructor Entry Routine Generation
    public SearchUI(Runnable onBack){
        this.onBack = onBack;

        setSpacing(10);
        setWidth("100%");

        Button backbtn = new Button(" <- Back to DashBoard");
        backbtn.addClickHandler(event -> onBack.run());
        add(backbtn);

        add(new HTML("<h3>ER Search Results</h3>"));
        loadSearchResults();
    }

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

                    card.add(new Label(key + ": " + value));
                }
            }

            add(card);
        }
    }
}