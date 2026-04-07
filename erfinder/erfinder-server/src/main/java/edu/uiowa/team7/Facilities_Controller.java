package edu.uiowa.team7;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/facilities")
public class Facilities_Controller extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try{
            String json = Queries.GetAllFacilitiesJSON();
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(json);
        }
        catch(SQLException e){
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Failed to Load Facilities");
        }
    }
}
