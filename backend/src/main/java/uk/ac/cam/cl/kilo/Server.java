package uk.ac.cam.cl.kilo;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

public class Server extends HttpServlet {

    @Override
    //Handle GET request
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (request.getMethod().equals("POST")) {
            doPost(request, response);

        } else if (request.getMethod().equals("GET")) {
            doGet(request, response);

        }
        //otherwise consider redirect from FB
        // put info from FB into DB

        return;
    }

    @Override
    //Handle GET request
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
       return;
    }

    // Handle POST request
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // Set the response content type
        response.setContentType("text; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String state = request.getParameter("state");
            if(state == null || htmlFilter(state).isEmpty()){
                //didn't include valid state
                return;
            }

            if (/*lookup state in database*/ false) {
                //state not in db
                //add state to db
                response.sendRedirect("https://www.facebook.com/v3.2/dialog/oauth?&" +
                        "client_id={2332527273433130}&" +
                        "redirect_uri={"+/*redirect uri*/ "}&"  +
                        "state={"+state+"}");
            }
            else{
                response.sendError(1,"Shouldn't be posting");
            }

        }
        finally {
            out.close();
        }
    }

    // Filter the string
    private static String htmlFilter(String message) {
        if (message == null) return null;
        int len = message.length();
        StringBuffer result = new StringBuffer(len + 20);
        char aChar;

        for (int i = 0; i < len; ++i) {
            aChar = message.charAt(i);
            switch (aChar) {
                case '<': result.append("&lt;"); break;
                case '>': result.append("&gt;"); break;
                case '&': result.append("&amp;"); break;
                case '"': result.append("&quot;"); break;
                default: result.append(aChar);
            }
        }
        return (result.toString());
    }
}