package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

// Facility API Endpoints
@WebServlet({
        "/api/facilities",
        "/api/addfacility",
        "/api/updateFacility",
        "/api/updateWaitTime"
})
public class FacilityController extends HttpServlet {

    // GET Request Routing
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getServletPath();
        if ("/api/facilities".equals(path)) {
            handleGetFacilities(res);
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // POST Request Routing
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = req.getServletPath();
        switch (path) {
            case "/api/addfacility":
                handleAddFacility(req, res);
                break;
            case "/api/updateFacility":
                handleUpdateFacility(req, res);
                break;
            case "/api/updateWaitTime":
                handleUpdateWaitTime(req, res);
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // Handle Get Facilities
    private void handleGetFacilities(HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try {
            String json = Queries.GetAllFacilitiesJSON();
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(json);
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Failed to load facilities");
        }
    }

    // Handle Add Facility
    private void handleAddFacility(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        String name = req.getParameter("ername");
        String phone = req.getParameter("phonenum");
        String address = req.getParameter("address");
        String zip = req.getParameter("zip");
        String trauma = req.getParameter("traumalevel");
        String specialties = req.getParameter("specialties");

        int beds;
        int waitTime;

        try {
            beds = Integer.parseInt(req.getParameter("bedavail"));
            waitTime = Integer.parseInt(req.getParameter("waitTime"));
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid numeric input");
            return;
        }

        String fullAddress = address + " " + zip;
        double lat, lon;

        try {
            double[] coords = GeoCodeService.geocode(fullAddress);
            lat = coords[0];
            lon = coords[1];
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Unable to geocode address");
            return;
        }

        try {
            Queries.AddFacility(name, phone, address, zip, trauma, specialties, beds, waitTime, lat, lon);
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Database error while adding facility");
            return;
        }

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("OK");
    }

    // Handle Update Facility
    private void handleUpdateFacility(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int facilityId, beds, wait;

        try {
            facilityId = Integer.parseInt(req.getParameter("facilityId"));
            beds = Integer.parseInt(req.getParameter("bedavail"));
            wait = Integer.parseInt(req.getParameter("waitTime"));
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid numeric input");
            return;
        }

        try {
            boolean updated = Queries.UpdateFacility(facilityId, beds, wait);
            if (!updated) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("No facility was updated");
                return;
            }
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("OK");
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Database error updating facility");
        }
    }

    // Handle Update Wait Time
    private void handleUpdateWaitTime(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");

        int facilityId;
        int waitTime;

        try {
            String body = req.getReader().lines().reduce("", (acc, line) -> acc + line);
            body = body.replaceAll("[{}\"]", "");
            String[] parts = body.split(",");

            String idStr = null;
            String waitStr = null;

            for (String p : parts) {
                String[] kv = p.split(":");
                if (kv.length != 2) continue;

                if (kv[0].trim().equals("facilityId")) {
                    idStr = kv[1].trim();
                } else if (kv[0].trim().equals("waitTime")) {
                    waitStr = kv[1].trim();
                }
            }

            if (idStr == null || waitStr == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Missing required fields");
                return;
            }

            facilityId = Integer.parseInt(idStr);
            waitTime = Integer.parseInt(waitStr);

            if (waitTime < 0) throw new NumberFormatException();

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid JSON input");
            return;
        }

        try {
            boolean updated = Queries.UpdateWaitTime(facilityId, waitTime);
            if (!updated) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("No facility updated");
                return;
            }
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("OK");
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Database error updating wait time");
        }
    }
}