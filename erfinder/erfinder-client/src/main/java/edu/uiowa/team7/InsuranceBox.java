package edu.uiowa.team7;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;

import java.util.Date;
import java.util.HashMap;

public class InsuranceBox {

    // Core Panel Tracking
    public HTMLPanel p;
    public boolean fresh;
    public boolean deleting = false;
    public boolean editing = false;

    // Controls
    public final Button editButton = new Button("Edit");
    public final Button delButton = new Button("Delete");
    public final Button canButton = new Button("Cancel");
    public final Label areYouSureLabel = new Label("Are you sure?");
    public final Label progress = new Label();

    private InsuranceField[] fields;

    // Create Fresh Component
    public InsuranceBox(HTMLPanel par) {
        InstanceFields(true);
        BuildForm(par);
        fresh = true;
        Confirm();
    }

    // Create Parsed Component
    public InsuranceBox(String info, HTMLPanel par) {
        InstanceFields(false);
        ParseFields(info);
        BuildForm(par);
        fresh = false;
    }

    // Initialize Box Models
    private void InstanceFields(boolean fresh) {
        fields = new InsuranceField[] {
                new InsuranceTextField("member_id", "Member ID:", true, fresh),
                new InsuranceTextField("insurer_name", "Insurer:"),
                new InsuranceIntField("policy_num", "Policy #:", 10),
                new InsuranceIntField("group_num", "Group #:", 10),
                new InsurancePlanField("plan_type", "Plan Type:"),
                new InsuranceTextField("rx_bin", "Rx BIN;"),
                new InsuranceTextField("rx_pcn", "Rx PCN:"),
                new InsuranceTextField("rx_group", "Rx Group:"),
                new InsuranceTextField("rx_id", "Rx ID:"),
                new InsuranceTextField("customer_service_phone", "Insurance Contact:"),
                new InsuranceDateField("effective_date", "Effective Date:"),
                new InsuranceIntField("copay", "Copay (Cents):", 10),
                new InsuranceIntField("coinsurance", "Coinsurance (Percent):", 2),
                new InsuranceIntField("out_of_pocket_max", "Out of Pocket Maximum (Cents):", 10),
        };
    }

    // Decode Network Information
    private void ParseFields(String info) {
        String[] fieldvalpairs = info.split(",");
        HashMap<String,String> map = new HashMap<>();
        for (String pair : fieldvalpairs) {
            String[] div = pair.split(":");
            map.put(div[0], div[1]);
        }
        for (InsuranceField f : fields) {
            f.Parse(map);
        }
    }

    // Construct HTML Table Map
    public void BuildForm(HTMLPanel par) {
        p = new HTMLPanel("table", "");
        p.getElement().setAttribute("style", "\n" +
                "    background: white;\n" +
                "    padding: 15px;\n" +
                "    margin: 15px;" +
                "    border-radius: 8px;\n" +
                "    min-width: 350px;" +
                "    min-height: 475px;" +
                "    box-shadow: 0 6px 12px rgba(0,0,0,0.15);\n" +
                "\n");
        for (InsuranceField f : fields) {
            f.Add(p);
        }
        HTMLPanel lrow = new HTMLPanel("tr","");
        lrow.add(areYouSureLabel);
        areYouSureLabel.setVisible(false);
        p.add(lrow);

        HTMLPanel brow = new HTMLPanel("tr","");
        HTMLPanel c0 = new HTMLPanel("td","");
        c0.add(editButton);
        HTMLPanel c1 = new HTMLPanel("td","");
        c0.add(canButton);
        c1.add(delButton);
        brow.add(c0);
        brow.add(c1);

        p.add(brow);
        p.add(progress);

        editButton.addClickHandler(event -> Confirm());
        canButton.addClickHandler(event -> Cancel());
        delButton.addClickHandler(event -> Delete());
        canButton.setVisible(false);
        par.add(p);
    }

    // Handle Save / Finalize Confirmation
    public void Confirm() {
        if (deleting) {
            progress.setText("Processing...");
            areYouSureLabel.setVisible(false);
            progress.setVisible(true);
            editButton.setVisible(false);
            canButton.setVisible(false);
            delButton.setVisible(false);

            InsuranceTextField f = (InsuranceTextField) fields[0];

            RequestBuilder req = new RequestBuilder(RequestBuilder.GET, "/api/delinsurance?blob=" +
                    App.B64Encode(f.box.getText()));
            try {
                req.sendRequest(null, new DeleteInsuranceCallback());
            } catch (Exception e) {
                progress.setText("Unexpected Error. Try refreshing the page.");
            }
            return;
        }

        if (!editing) {
            progress.setVisible(false);
            editButton.setText("Submit");
            canButton.setVisible(true);
            delButton.setVisible(false);
            for (InsuranceField f : fields) {
                f.BeginEdit();
            }
            editing = true;
        } else {
            boolean can_submit = true;
            boolean edited = false;
            for (InsuranceField f : fields) {
                if (!f.ValidSub()) can_submit = false;
                if (f.Edited()) edited = true;
            }

            if (can_submit && edited) {
                progress.setVisible(true);
                progress.setText("Submitting...");
                editButton.setText("Edit");
                editButton.setVisible(false);
                canButton.setVisible(false);
                delButton.setVisible(false);

                for (InsuranceField f : fields) {
                    f.SubmitEdit();
                }
                editing = false;

                String res = BuildResult();
                String endpoint = fresh ? "/api/setinsurance?f=y&blob=" : "/api/setinsurance?f=n&blob=";
                RequestBuilder req = new RequestBuilder(RequestBuilder.GET, endpoint + res);

                try {
                    req.sendRequest(null, new SubmitInsuranceCallback());
                } catch (Exception e) {
                    // Ignored
                }
            } else if (!can_submit) {
                progress.setVisible(true);
                progress.setText("Invalid Entry.");
            } else {
                progress.setVisible(true);
                progress.setText("No Changes Made.");
                editButton.setText("Edit");
                editButton.setVisible(true);
                canButton.setVisible(false);
                delButton.setVisible(true);
            }

            fresh = false;
        }
    }

    // Reject Revisions Command
    public void Cancel() {
        if (fresh) {
            p.removeFromParent();
        }
        editing = false;
        editButton.setText("Edit");
        canButton.setVisible(false);
        delButton.setVisible(true);
        for (InsuranceField f : fields) {
            f.CancelEdit();
        }
    }

    // Confirm Destructive Event
    public void Delete() {
        if (deleting) {
            areYouSureLabel.setVisible(false);
            editButton.setText("Edit");
            delButton.setText("Delete");
            deleting = false;
            return;
        }
        deleting = true;
        areYouSureLabel.setVisible(true);
        editButton.setText("Yes; Delete");
        delButton.setText("No; Keep Info");
    }

    // Prepare JSON Submissions
    public String BuildResult() {
        StringBuilder b = new StringBuilder();
        for (InsuranceField f : fields) {
            String encoded = f.Encode();
            if (encoded != null) {
                b.append(f.Encode());
                b.append(",");
            }
        }
        return App.B64Encode(b.toString());
    }

    // Submit Action Request Processor
    private class SubmitInsuranceCallback implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case App.HTTP_OK_200:
                    progress.setText("Information updated!");
                    editButton.setVisible(true);
                    delButton.setVisible(true);
                    break;
                case App.HTTP_UNAUTHORIZED_401:
                case App.HTTP_INTERNAL_SERVER_ERROR_500:
                    progress.setText("An error occured. Please refresh the page.");
                    break;
            }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
        }
    }

    // Delete Action Callback Processor
    private class DeleteInsuranceCallback implements RequestCallback {
        @Override
        public void onResponseReceived(Request request, Response response) {
            switch (response.getStatusCode()) {
                case App.HTTP_OK_200:
                    p.removeFromParent();
                    break;
                case App.HTTP_UNAUTHORIZED_401:
                    progress.setText("Session expired.");
                    break;
                case App.HTTP_INTERNAL_SERVER_ERROR_500:
                    progress.setText("There was an unexpected error.");
                    break;
            }
        }

        @Override
        public void onError(Request request, Throwable throwable) {
        }
    }

    // Abstract Shared Field Methods
    private abstract class InsuranceField {
        public final Label label = new Label();
        public boolean always_submit = false;
        public final String fieldname;

        public InsuranceField(String fieldname, String label) {
            this.fieldname = fieldname;
            this.label.setText(label);
        }
        public abstract void Parse(HashMap<String,String> map);
        public abstract void Add(HTMLPanel parent);
        public abstract String Encode();
        public abstract void BeginEdit();
        public abstract void CancelEdit();
        public abstract boolean ValidSub();
        public abstract void SubmitEdit();
        public abstract boolean Edited();
    }

    // GWT Standard Text Inputs
    private class InsuranceTextField extends InsuranceField {
        public boolean fresh;
        public final TextBox box = new TextBox();
        public String preEditText = "";

        public InsuranceTextField(String fieldname, String label, boolean always, boolean fresh) {
            super(fieldname, label);
            this.always_submit = always;
            this.box.setReadOnly(true);
            this.fresh = fresh;
        }
        public InsuranceTextField(String fieldname, String label) {
            this(fieldname, label, false, false);
        }
        @Override
        public void Parse(HashMap<String, String> map) {
            String b64encodedval = map.getOrDefault(fieldname, "");
            preEditText = App.B64Decode(b64encodedval);
            this.box.setText(preEditText);
        }
        @Override
        public void Add(HTMLPanel parent) {
            HTMLPanel row = new HTMLPanel("tr","");
            HTMLPanel c0 = new HTMLPanel("td","");
            c0.add(this.label);
            row.add(c0);
            HTMLPanel c1 = new HTMLPanel("td","");
            c1.getElement().setPropertyInt("colspan",2);
            c1.add(this.box);
            row.add(c1);
            parent.add(row);
        }
        @Override
        public String Encode() {
            if (!always_submit && box.getText().equals(preEditText)) {
                return null;
            }
            return fieldname + ":" + App.B64Encode(box.getText());
        }
        @Override
        public void BeginEdit() {
            preEditText = this.box.getText();
            if (always_submit) {
                if (!fresh) return;
                fresh = false;
            }
            this.box.setReadOnly(false);
        }
        @Override
        public void SubmitEdit() {
            this.box.setReadOnly(true);
        }
        @Override
        public boolean ValidSub() {
            return !always_submit || !this.box.getText().isEmpty();
        }
        @Override
        public boolean Edited() {
            return !box.getValue().equals(preEditText);
        }
        @Override
        public void CancelEdit() {
            this.box.setText(preEditText);
            this.box.setReadOnly(true);
        }
    }

    // GWT Standard Integer Parsers
    private class InsuranceIntField extends InsuranceField {
        public int preEditInt = 0;
        public final IntegerBox box = new IntegerBox();

        public InsuranceIntField(String fieldname, String label, int maxlength) {
            super(fieldname, label);
            this.box.setReadOnly(true);
            box.setMaxLength(maxlength);
            box.setValue(0);
        }

        @Override
        public void Parse(HashMap<String, String> map) {
            String[] div = map.getOrDefault(fieldname, "0.0").split("\\.");
            preEditInt = Integer.parseInt(div[0]);
            this.box.setValue(preEditInt);
        }

        @Override
        public void Add(HTMLPanel parent) {
            HTMLPanel row = new HTMLPanel("tr","");
            HTMLPanel c0 = new HTMLPanel("td","");
            c0.add(this.label);
            row.add(c0);
            HTMLPanel c1 = new HTMLPanel("td","");
            c1.getElement().setPropertyInt("colspan",2);
            c1.add(this.box);
            row.add(c1);
            parent.add(row);
        }
        @Override
        public String Encode() {
            if (box.getValue() == preEditInt) return null;
            return fieldname + ":" + box.getValue();
        }

        @Override
        public void BeginEdit() {
            preEditInt = this.box.getValue();
            this.box.setReadOnly(false);
        }
        @Override
        public void SubmitEdit() {
            this.box.setReadOnly(true);
        }
        @Override
        public boolean ValidSub() {
            return true;
        }
        @Override
        public void CancelEdit() {
            this.box.setValue(preEditInt);
            this.box.setReadOnly(true);
        }
        @Override
        public boolean Edited() {
            return box.getValue() != preEditInt;
        }
    }

    // Formatting Calendar Inputs
    private class InsuranceDateField extends InsuranceField {
        public Date preEditDate = new Date(0);
        public final DateBox box = new DateBox();

        public InsuranceDateField(String fieldname, String label) {
            super(fieldname, label);
            this.box.setEnabled(false);
            DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd");
            box.setFormat(new DateBox.DefaultFormat(format));
        }

        @Override
        public void Parse(HashMap<String, String> map) {
            String b64encodedval = map.getOrDefault(fieldname, "");
            preEditDate = this.box.getFormat().parse(this.box, App.B64Decode(b64encodedval), true);
            this.box.setValue(preEditDate);
        }

        @Override
        public void Add(HTMLPanel parent) {
            HTMLPanel row = new HTMLPanel("tr","");
            HTMLPanel c0 = new HTMLPanel("td","");
            c0.add(this.label);
            row.add(c0);
            HTMLPanel c1 = new HTMLPanel("td","");
            c1.getElement().setPropertyInt("colspan",2);
            c1.add(this.box);
            row.add(c1);
            parent.add(row);
        }
        @Override
        public String Encode() {
            if (this.box.getValue().equals(this.preEditDate)) return null;
            return fieldname + ":" + App.B64Encode(box.getFormat().format(box,box.getValue()));
        }

        @Override
        public void BeginEdit() {
            preEditDate = this.box.getValue();
            this.box.setEnabled(true);
        }
        @Override
        public void SubmitEdit() {
            this.box.setEnabled(false);
        }
        @Override
        public boolean ValidSub() {
            return true;
        }
        @Override
        public void CancelEdit() {
            this.box.setValue(preEditDate);
            this.box.setEnabled(false);
        }
        @Override
        public boolean Edited() {
            return !box.getValue().equals(preEditDate);
        }
    }

    // Formatting Standard Plan Tiers
    private class InsurancePlanField extends InsuranceField {
        public int preEditPlan = 0;
        public final ListBox box = new ListBox();

        public InsurancePlanField(String fieldname, String label) {
            super(fieldname, label);
            box.addItem("--");
            box.addItem("PPO");
            box.addItem("HMO");
            box.addItem("POS");
            this.box.setEnabled(false);
        }

        @Override
        public void Parse(HashMap<String, String> map) {
            String val = map.getOrDefault(fieldname,"");
            switch (App.B64Decode(val)) {
                case "PPO":
                    preEditPlan = 1;
                    box.setItemSelected(1, true);
                    break;
                case "HMO":
                    preEditPlan = 2;
                    box.setItemSelected(2,true);
                    break;
                case "POS":
                    preEditPlan = 3;
                    box.setItemSelected(3,true);
                    break;
                default:
                    preEditPlan = 0;
                    box.setItemSelected(0,true);
                    break;
            }
        }

        @Override
        public void Add(HTMLPanel parent) {
            HTMLPanel row = new HTMLPanel("tr","");
            HTMLPanel c0 = new HTMLPanel("td","");
            c0.add(this.label);
            row.add(c0);
            HTMLPanel c1 = new HTMLPanel("td","");
            c1.getElement().setPropertyInt("colspan",2);
            c1.add(this.box);
            row.add(c1);
            parent.add(row);
        }
        @Override
        public String Encode() {
            if (box.getSelectedIndex() == preEditPlan) return null;
            return fieldname + ":" + App.B64Encode(box.getSelectedValue());
        }

        @Override
        public void BeginEdit() {
            preEditPlan = this.box.getSelectedIndex();
            this.box.setEnabled(true);
        }
        @Override
        public void SubmitEdit() {
            this.box.setEnabled(false);
        }
        @Override
        public boolean ValidSub() {
            return true;
        }
        @Override
        public void CancelEdit() {
            this.box.setSelectedIndex(preEditPlan);
            this.box.setEnabled(false);
        }
        @Override
        public boolean Edited() {
            return box.getSelectedIndex() != preEditPlan;
        }
    }
}