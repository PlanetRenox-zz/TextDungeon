
import java.io.BufferedOutputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

import java.util.Scanner;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import PlanetRenox.AES256_GCM;

@WebServlet
public class ControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ServletContext context = request.getServletContext();
		String warPath = context.getRealPath("/");

		File file = new File(warPath + "WEB-INF/pages/" + request.getRequestURI().substring(3));

		if (file.isFile()) {

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n"
					+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
					+ "<html>\r\n" + "<head><style>body {background-color: #041d2c;} p {color: gray;}</style></head>"
					+ "<body>\r\n" + "<p>Page has existing owner.</p>"
					+ "	<form method='post' action='ControllerServlet'>\r\n" + "		<div>\r\n"
					+ "			<input type='hidden' name='path' value='" + request.getRequestURI().substring(3)
					+ "' size='20' />\r\n" + "			<p>\r\n"
					+ "				<input type='password' name='pass' size='20' />\r\n" + "			</p>\r\n"
					+ "			<p>\r\n" + "				<input value='Enter' type='submit' />\r\n"
					+ "			</p>\r\n" + "		</div>\r\n" + "	</form>\r\n" + "</body>\r\n" + "</html>");

			out.close();
		} else {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n"
					+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
					+ "<html>\r\n" + "<head><style>body {background-color: #041d2c;} p {color: gray;}</style></head>"
					+ "<body>\r\n" + "<p>Claim this page:</p>" + "	<form method='post' action='ControllerServlet'>\r\n"
					+ "		<div>\r\n" + "			<input type='hidden' name='path' value='"
					+ request.getRequestURI().substring(3) + "' size='20' />\r\n" + "			<p>\r\n"
					+ "				<input type='password' name='pass' size='20' />\r\n" + "			</p>\r\n"
					+ "			<p>\r\n" + "				<input value='Enter' type='submit' />\r\n"
					+ "			</p>\r\n" + "		</div>\r\n" + "	</form>\r\n" + "</body>\r\n" + "</html>");

			out.close();
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (request.getRequestURI().substring(3).equals("ControllerServlet")) {
			response.setStatus(307);
			response.setHeader("Location", request.getParameter("path"));
			return;
		}
		
		

		SecureRandom random = new SecureRandom();
		ServletContext context = request.getServletContext();
		String warPath = context.getRealPath("/");

		File file = new File(warPath + "WEB-INF/pages/" + request.getParameter("path"));
		
		

		if (request.getParameter("textcontent1") == null || request.getParameter("recovery") != null) // if null ?
																										// coming from
																										// frontpage !
																										// user page
		{

			if (file.isFile()) // if file exists decrypt, if not, dont
			{
				Path path = Paths.get(warPath + "WEB-INF/pages/" + request.getParameter("path"));
				
				byte[] data = Files.readAllBytes(path);
				
				String rawFileText = AES256_GCM.decrypt(data, request.getParameter("pass"));
				
				if (rawFileText == null) // file exists but wrong password. display password field
				{
					response.setContentType("text/html");
					PrintWriter out = response.getWriter();

					out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n"
							+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
							+ "<html>\r\n"
							+ "<head><style>body {background-color: #041d2c;} p {color: red;}</style></head>"
							+ "<body>\r\n" + " <p>Incorrect Password.</p>"
							+ "	<form method='post' action='ControllerServlet'>\r\n" + "		<div>\r\n"
							+ "			<input type='hidden' name='path' value='" + request.getParameter("path")
							+ "' size='20' />\r\n" + "			<p>\r\n"
							+ "			<input type='hidden' name='recovery' value='"+ request.getParameter("recovery")
							+"' size='20' />\r\n" + "			<p>\r\n"
							+ "				<input type='password' name='pass' size='20' />\r\n"
							+ "			</p>\r\n" + "			<p>\r\n"
							+ "				<input value='Enter' type='submit' />\r\n" + "			</p>\r\n"
							+ "		</div>\r\n" + "	</form>\r\n" + "</body>\r\n" + "</html>");

					out.close();
				} else // file exists and password is correct. display user page
				{
					if (request.getParameter("recovery") != null) {
						AES256_GCM.encrypt(request.getParameter("recovery"), request.getParameter("pass"));
					}



					String sessionId = String.valueOf(random.nextLong()) + System.currentTimeMillis();

					context.setAttribute(sessionId, request.getParameter("pass"));

					int tabCount = Integer.parseInt(rawFileText.substring(0, 1));

					Scanner s = null;
					try {
						s = new Scanner(rawFileText);
						s.useDelimiter("-nextpage-|-meta-");
						s.next();
						response.setContentType("text/html");
						PrintWriter out = response.getWriter();
						// AES256_GCM.decrypt(data, request.getParameter("pass"))
						out.println("<!doctype html>\r\n" + "<html class=\"no-js\" lang=\"\">\r\n" + "\r\n"
								+ "<head>\r\n" + "  <meta charset=\"utf-8\">\r\n"
								+ "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\r\n"
								+ "  <title>Text Dungeon</title>\r\n"
								+ "  <meta name=\"description\" content=\"Lock your text away in our deepest darkest encrypted dungeons.\">\r\n"
								+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\r\n"
								+ "\r\n" + "  <link rel=\"manifest\" href=\"site.webmanifest\">\r\n"
								+ "  <link rel=\"apple-touch-icon\" href=\"htmlresources/img/icon29.png\">\r\n"
								+ "  <link rel=\"shortcut icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n"
								+ "  <link rel=\"icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n" + "\r\n"
								+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/normalize.css\">\r\n"
								+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/main.css\">\r\n" + "</head>\r\n"
								+ "\r\n" + "<body>\r\n" + "  <!--[if lte IE 9]>\r\n"
								+ "    <p class=\"browserupgrade\">You are using an <strong>outdated</strong> browser. Please <a href=\"https://browsehappy.com/\">upgrade your browser</a> to improve your security.</p>\r\n"
								+ "  <![endif]-->\r\n" + "\r\n" + "\r\n" + "\r\n" + "  <div id=\"colL\">\r\n" + "\r\n"
								+ "  </div>\r\n" + "\r\n" + "\r\n" + "  <!-- <section> -->\r\n"
								+ "  <div id=\"textdiv\">\r\n" + "\r\n"
								+ "    <form method=\"post\" action=\"ControllerServlet\">\r\n" + "\r\n"
								+ "      <article class=\"tabs\">\r\n" + "\r\n" + "        <section id=\"tab1\">\r\n"
								+ "          <h2><a href=\"#tab1\">Tab 1</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent1\">"
								+ s.next() + "</textarea>\r\n" + "        </section>\r\n" + "\r\n" + "\r\n"
								+ "        <section id=\"tab2\">\r\n" + "          <h2 "
								+ ((tabCount >= 2) ? "" : "hidden") + "><a href=\"#tab2\">Tab 2</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent2\">"
								+ ((tabCount >= 2) ? s.next() : "") + "</textarea>\r\n" + "        </section>\r\n"
								+ "\r\n" + "        <section id=\"tab3\">\r\n" + "          <h2 "
								+ ((tabCount >= 3) ? "" : "hidden") + "><a href=\"#tab3\">Tab 3</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent3\">"
								+ ((tabCount >= 3) ? s.next() : "") + "</textarea>\r\n" + "        </section>\r\n"
								+ "\r\n" + "        <section id=\"tab4\">\r\n" + "          <h2 "
								+ ((tabCount >= 4) ? "" : "hidden") + "><a href=\"#tab4\">Tab 4</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent4\">"
								+ ((tabCount >= 4) ? s.next() : "") + "</textarea>\r\n" + "        </section>\r\n"
								+ "\r\n" + "        <section id=\"tab5\">\r\n" + "          <h2 "
								+ ((tabCount >= 5) ? "" : "hidden") + "><a href=\"#tab5\">Tab 5</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent5\">"
								+ ((tabCount >= 5) ? s.next() : "") + "</textarea>\r\n" + "        </section>\r\n"
								+ "\r\n" + "        <section id=\"tab6\">\r\n" + "          <h2 "
								+ ((tabCount >= 6) ? "" : "hidden") + "><a href=\"#tab6\">Tab 6</a></h2>\r\n"
								+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent6\">"
								+ ((tabCount >= 6) ? s.next() : "") + "</textarea>\r\n" + "        </section>\r\n"
								+ "\r\n" + "\r\n" + "\r\n" + "      </article>\r\n" + "\r\n"
								+ "      <input type=\"hidden\" name=\"path\" value=\"" + request.getParameter("path")
								+ "\" />\r\n" + "      <input type=\"hidden\" name=\"sessionAuth\" value=\"" + sessionId
								+ "\" />\r\n" + "      <input type=\"hidden\" name=\"meta\" value=\"" + tabCount
								+ "t-meta-\" />\r\n" + "\r\n"
								+ "      <button id=\"submit\" value=\"submit\" type=\"submit\"></button>\r\n"
								+ "      <button id=\"addtab\" value=\"addtab\" name=\"addtab\" type=\"submit\">+</button>\r\n"
								+ "\r\n" + "    </form>\r\n" + "\r\n" + "  </div>\r\n" + "  <!-- </section> -->\r\n"
								+ "\r\n" + "\r\n" + "  <!-- </section> -->\r\n" + "  <div id=\"colR\">\r\n" + "\r\n"
								+ "  </div>\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "</body>\r\n" + "\r\n"
								+ "</html>");

						out.close();
					}

					finally {
						s.close();
					}

				}

			} else // file doesnt exist, create a new default text page for user
			{
				String path = request.getParameter("path");
				path = path.replaceAll("[^a-zA-Z0-9_/@&-]", "_");

				String sessionId = String.valueOf(random.nextLong()) + System.currentTimeMillis();
				context.setAttribute(sessionId, request.getParameter("pass"));

				response.setContentType("text/html");
				PrintWriter out = response.getWriter();

				out.println("<!doctype html>\r\n" + "<html class=\"no-js\" lang=\"\">\r\n" + "\r\n" + "<head>\r\n"
						+ "  <meta charset=\"utf-8\">\r\n"
						+ "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\r\n"
						+ "  <title>Text Dungeon</title>\r\n"
						+ "  <meta name=\"description\" content=\"Lock your text away in our deepest darkest encrypted dungeons.\">\r\n"
						+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\r\n"
						+ "\r\n" + "  <link rel=\"manifest\" href=\"site.webmanifest\">\r\n"
						+ "  <link rel=\"apple-touch-icon\" href=\"htmlresources/img/icon29.png\">\r\n"
						+ "  <link rel=\"shortcut icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n"
						+ "  <link rel=\"icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n" + "\r\n"
						+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/normalize.css\">\r\n"
						+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/main.css\">\r\n" + "</head>\r\n" + "\r\n"
						+ "<body>\r\n" + "  <!--[if lte IE 9]>\r\n"
						+ "    <p class=\"browserupgrade\">You are using an <strong>outdated</strong> browser. Please <a href=\"https://browsehappy.com/\">upgrade your browser</a> to improve your security.</p>\r\n"
						+ "  <![endif]-->\r\n" + "\r\n" + "\r\n" + "\r\n" + "  <div id=\"colL\">\r\n" + "\r\n"
						+ "  </div>\r\n" + "\r\n" + "\r\n" + "  <!-- <section> -->\r\n" + "  <div id=\"textdiv\">\r\n"
						+ "\r\n" + "    <form method=\"post\" action=\"ControllerServlet\">\r\n" + "\r\n"
						+ "      <article class=\"tabs\">\r\n" + "\r\n" + "        <section id=\"tab1\">\r\n"
						+ "          <h2><a href=\"#tab1\">Tab 1</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent1\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "\r\n" + "        <section id=\"tab2\">\r\n"
						+ "          <h2 hidden><a href=\"#tab2\">Tab 2</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent2\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab3\">\r\n"
						+ "          <h2 hidden><a href=\"#tab3\">Tab 3</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent3\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab4\">\r\n"
						+ "          <h2 hidden><a href=\"#tab4\">Tab 4</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent4\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab5\">\r\n"
						+ "          <h2 hidden><a href=\"#tab5\">Tab 5</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent5\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab6\">\r\n"
						+ "          <h2 hidden><a href=\"#tab6\">Tab 6</a></h2>\r\n"
						+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent6\"></textarea>\r\n"
						+ "        </section>\r\n" + "\r\n" + "\r\n" + "\r\n" + "      </article>\r\n" + "\r\n"
						+ "      <input type=\"hidden\" name=\"path\" value=\"" + path
						+ "\" />\r\n" + "      <input type=\"hidden\" name=\"meta\" value=\"1t-meta-\" />\r\n"
						+ "      <input type=\"hidden\" name=\"sessionAuth\" value=\"" + sessionId + "\" />\r\n"
						+ "\r\n" + "      <button id=\"submit\" value=\"submit\" type=\"submit\"></button>\r\n"
						+ "      <button id=\"addtab\" value=\"addtab\" name=\"addtab\" type=\"submit\">+</button>\r\n"
						+ "\r\n" + "    </form>\r\n" + "\r\n" + "  </div>\r\n" + "  <!-- </section> -->\r\n" + "\r\n"
						+ "\r\n" + "  <!-- </section> -->\r\n" + "  <div id=\"colR\">\r\n" + "\r\n" + "  </div>\r\n"
						+ "\r\n" + "\r\n" + "\r\n" + "\r\n" + "</body>\r\n" + "\r\n" + "</html>");

				out.close();
			}

		} else // user is trying to save new text &&|| add tab
		{

			BufferedOutputStream writeBuffer = null;
			PrintWriter out = null;
			try {
				int tabCount = Integer.parseInt(request.getParameter("meta").substring(0, 1));

				if ("addtab".equals(request.getParameter("addtab"))) // user trying to add tab if true
				{
					if (tabCount < 6) {
						tabCount += 1;
					}
				}

				String toBeEncrypted = tabCount + "t-meta-";

				for (int i = 1; i <= Integer.parseInt(request.getParameter("meta").substring(0, 1)); i++) {
					toBeEncrypted += request.getParameter("textcontent" + i) + "-nextpage-";
				}
				// format of saved text = *-meta- + textcontent# + -nextpage- + textcont...

				
				

				
				if (context.getAttribute(request.getParameter("sessionAuth")) == null) { // session expired, warn user

					response.setContentType("text/html");
					out = response.getWriter();
					out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\r\n"
							+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
							+ "<html>\r\n"
							+ "<head><style>body {background-color: #041d2c;} p {color: red;}</style></head>"
							+ "<body>\r\n"
							+ " <p>Your session expired, however your changes are not lost. Please input your password again to make sure your latest changes are saved.</p>"
							+ "	<form method='post' action='ControllerServlet'>\r\n" + "		<div>\r\n"
							+ "			<input type='hidden' name='path' value='" + request.getParameter("path")
							+ "' size='20' />\r\n" + "			<p>\r\n"
							+ "			<input type='hidden' name='recovery' value='" + toBeEncrypted
							+ "' size='20' />\r\n" + "			<p>\r\n"
							+ "				<input type='password' name='pass' size='20' />\r\n"
							+ "			</p>\r\n" + "			<p>\r\n"
							+ "				<input value='Enter' type='submit' />\r\n" + "			</p>\r\n"
							+ "		</div>\r\n" + "	</form>\r\n" + "</body>\r\n" + "</html>");

				} else { // they saved before session expired		
					writeBuffer = new BufferedOutputStream(new FileOutputStream(file));
					writeBuffer.write(AES256_GCM.encrypt(toBeEncrypted,
							(String) context.getAttribute(request.getParameter("sessionAuth"))));
					
					
					
					String sessionId = String.valueOf(random.nextLong()) + System.currentTimeMillis();
					context.setAttribute(sessionId, request.getParameter("sessionAuth"));
					
					context.removeAttribute(request.getParameter("sessionAuth"));
					

					response.setContentType("text/html");
					out = response.getWriter();
					out.println("<!doctype html>\r\n" + "<html class=\"no-js\" lang=\"\">\r\n" + "\r\n" + "<head>\r\n"
							+ "  <meta charset=\"utf-8\">\r\n"
							+ "  <meta http-equiv=\"x-ua-compatible\" content=\"ie=edge\">\r\n"
							+ "  <title>Text Dungeon</title>\r\n"
							+ "  <meta name=\"description\" content=\"Lock your text away in our deepest darkest encrypted dungeons.\">\r\n"
							+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\r\n"
							+ "\r\n" + "  <link rel=\"manifest\" href=\"site.webmanifest\">\r\n"
							+ "  <link rel=\"apple-touch-icon\" href=\"htmlresources/img/icon29.png\">\r\n"
							+ "  <link rel=\"shortcut icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n"
							+ "  <link rel=\"icon\" href=\"favicon.ico\" type=\"image/x-icon\">\r\n" + "\r\n"
							+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/normalize.css\">\r\n"
							+ "  <link rel=\"stylesheet\" href=\"htmlresources/css/main.css\">\r\n" + "</head>\r\n"
							+ "\r\n" + "<body>\r\n" + "  <!--[if lte IE 9]>\r\n"
							+ "    <p class=\"browserupgrade\">You are using an <strong>outdated</strong> browser. Please <a href=\"https://browsehappy.com/\">upgrade your browser</a> to improve your security.</p>\r\n"
							+ "  <![endif]-->\r\n" + "\r\n" + "\r\n" + "\r\n" + "  <div id=\"colL\">\r\n" + "\r\n"
							+ "  </div>\r\n" + "\r\n" + "\r\n" + "  <!-- <section> -->\r\n"
							+ "  <div id=\"textdiv\">\r\n" + "\r\n"
							+ "    <form method=\"post\" action=\"ControllerServlet\">\r\n" + "\r\n"
							+ "      <article class=\"tabs\">\r\n" + "\r\n" + "        <section id=\"tab1\">\r\n"
							+ "          <h2><a href=\"#tab1\">Tab 1</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent1\">"
							+ request.getParameter("textcontent1") + "</textarea>\r\n" + "        </section>\r\n"
							+ "\r\n" + "\r\n" + "        <section id=\"tab2\">\r\n" + "          <h2 "
							+ ((tabCount >= 2) ? "" : "hidden") + "><a href=\"#tab2\">Tab 2</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent2\">"
							+ ((tabCount >= 2) ? request.getParameter("textcontent2") : "") + "</textarea>\r\n"
							+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab3\">\r\n" + "          <h2 "
							+ ((tabCount >= 3) ? "" : "hidden") + "><a href=\"#tab3\">Tab 3</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent3\">"
							+ ((tabCount >= 3) ? request.getParameter("textcontent3") : "") + "</textarea>\r\n"
							+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab4\">\r\n" + "          <h2 "
							+ ((tabCount >= 4) ? "" : "hidden") + "><a href=\"#tab4\">Tab 4</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent4\">"
							+ ((tabCount >= 4) ? request.getParameter("textcontent4") : "") + "</textarea>\r\n"
							+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab5\">\r\n" + "          <h2 "
							+ ((tabCount >= 5) ? "" : "hidden") + "><a href=\"#tab5\">Tab 5</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent5\">"
							+ ((tabCount >= 5) ? request.getParameter("textcontent5") : "") + "</textarea>\r\n"
							+ "        </section>\r\n" + "\r\n" + "        <section id=\"tab6\">\r\n" + "          <h2 "
							+ ((tabCount >= 6) ? "" : "hidden") + "><a href=\"#tab6\">Tab 6</a></h2>\r\n"
							+ "          <textarea spellcheck=\"false\" class=\"textcontent\" name=\"textcontent6\">"
							+ ((tabCount >= 6) ? request.getParameter("textcontent6") : "") + "</textarea>\r\n"
							+ "        </section>\r\n" + "\r\n" + "\r\n" + "\r\n" + "      </article>\r\n" + "\r\n"
							+ "      <input type=\"hidden\" name=\"path\" value=\"" + request.getParameter("path")
							+ "\" />\r\n" + "      <input type=\"hidden\" name=\"sessionAuth\" value=\""
							+ sessionId + "\" />\r\n"
							+ "      <input type=\"hidden\" name=\"meta\" value=\"" + tabCount + "t-meta-\" />\r\n"
							+ "\r\n" + "      <button id=\"submit\" value=\"submit\" type=\"submit\"></button>\r\n"
							+ "      <button id=\"addtab\" value=\"addtab\" name=\"addtab\" type=\"submit\">+</button>\r\n"
							+ "\r\n" + "    </form>\r\n" + "\r\n" + "  </div>\r\n" + "  <!-- </section> -->\r\n"
							+ "\r\n" + "\r\n" + "  <!-- </section> -->\r\n" + "  <div id=\"colR\">\r\n" + "\r\n"
							+ "  </div>\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "</body>\r\n" + "\r\n" + "</html>");

				}
			} finally {
				out.close();
				writeBuffer.close();
			}
		}

	}

}
