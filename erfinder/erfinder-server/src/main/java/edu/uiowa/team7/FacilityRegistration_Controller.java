package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/addfacility")
public class FacilityRegistration_Controller extends HttpServlet {
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException{

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        //Read Parameters From Request
        String name       = request.getParameter("ername");
        String phone      = request.getParameter("phonenum");
        String address    = request.getParameter("address");
        String zip        = request.getParameter("zip");
        String trauma     = request.getParameter("traumalevel");
        String specialties= request.getParameter("specialties");
        int beds;
        int waitTime;

        try{
            beds = Integer.parseInt(request.getParameter("bedavail"));
            waitTime = Integer.parseInt(request.getParameter("waitTime"));
        }
        catch(NumberFormatException e){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid numeric input");
            return;
        }

        //Build Address
        String fullAddress = address + " " + zip;

        //GEOCODE
        double lat;
        double lon;
        try{
            double[] coords = GeoCodeService.geocode(fullAddress);
            lat = coords[0];
            lon = coords[1];
        }
        catch(Exception e){
            response.setStatus(400);
            response.getWriter().write("Unable To GeoCode Address");
            return;
        }

        //Call Stored Procedure
        try{
            Queries.AddFacility(name, phone, address, zip, trauma, specialties, beds, waitTime, lat, lon);
        }
        catch(SQLException e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database Error While Adding Facility");
            return;
        }

        //Return Success
        response.setStatus(200);
        response.getWriter().write("OK");

    }
}


