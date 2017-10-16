package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import remote.Theater;
import remote.WideBox;

/**
 * Servlet implementation class Serv
 */
@WebServlet("/Serv")
public class Serv extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int SERVER_PORT = 5000;
	public static final String HTML_START="<html><body>";
	public static final String HTML_END="</body></html>";

    /**
     * Default constructor. 
     */
    public Serv() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		PrintWriter out = response.getWriter();
		
		Registry registry = LocateRegistry.getRegistry("127.0.0.1", SERVER_PORT);
		WideBox test = null;
		try {
			test = (WideBox) registry.lookup("WideBox");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> theaters = (ArrayList<String>) test.getNames();
		
		out.println(HTML_START);
		for(String s:theaters){
			out.println("<h2>Hi There!</h2><br/><h3>Theaters="+ s +"</h3>");
		}
		out.println(HTML_END);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
