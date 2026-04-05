package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/updateFacility")
public class UpdateFacility_Controller extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res)throws IOException {
        int facilityId, beds, wait;

        try{
            facilityId = Integer.parseInt(req.getParameter("facilityId"));
            beds = Integer.parseInt(req.getParameter("bedavail"));
            wait = Integer.parseInt(req.getParameter("waitTime"));
        }
        catch(NumberFormatException e){
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid Numeric Input");
            return;
        }

        try{
            boolean updated =  Queries.UpdateFacility(facilityId, beds, wait);

            if(!updated){
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("No Facility Was Updated");
                return;
            }

        }
        catch(SQLException e){
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Database Error Updating Facility");
        }
    }
}
