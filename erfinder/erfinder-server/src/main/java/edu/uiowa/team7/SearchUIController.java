package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/search")
public class SearchUIController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // Get PK list from URL: /api/search?pks=1,2,3
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

                // CALL YOUR EXISTING METHOD
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
                // Skip bad PK values so UI doesn't crash
                e.printStackTrace();
            }
        }

        json.append("]");

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(json.toString());
    }

    // Helper to avoid null values breaking JSON
    private String safe(String s) {
        return (s == null) ? "" : s.replace("\"", "'");
    }
}