package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Search UI API Endpoints
@WebServlet("/api/search")
public class SearchUIController extends HttpServlet {

    // Process Facility Search Route
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String pksParam = req.getParameter("pks");

        if (pksParam == null || pksParam.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Missing PKs");
            return;
        }

        String[] pkStrings = pksParam.split(",");
        StringBuilder json = new StringBuilder("[");
        boolean first = true;

        for (String pkStr : pkStrings) {
            try {
                int pk = Integer.parseInt(pkStr.trim());
                String[] f = Queries.GetFacility(pk);

                if (f != null) {
                    if (!first) {
                        json.append(",");
                    }
                    first = false;

                    json.append("{")
                            .append("\"name\":\"").append(safe(f[0])).append("\",")
                            .append("\"address\":\"").append(safe(f[1])).append("\",")
                            .append("\"zip\":\"").append(safe(f[2])).append("\",")
                            .append("\"phone\":\"").append(safe(f[3])).append("\",")
                            .append("\"trauma\":\"").append(safe(f[4])).append("\",")
                            .append("\"specialties\":\"").append(safe(f[5])).append("\",")
                            .append("\"beds\":").append(f[6]).append(",")
                            .append("\"wait\":").append(f[7])
                            .append("}");
                }

            } catch (Exception e) {
                // Skip unparseable primary keys to protect display
                e.printStackTrace();
            }
        }

        json.append("]");

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(json.toString());
    }

    // JSON String Safety Formatting
    private String safe(String s) {
        return (s == null) ? "" : s.replace("\"", "'");
    }
}