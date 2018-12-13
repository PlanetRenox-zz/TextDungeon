
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet Filter implementation class ControllerFilter
 */
@WebFilter("/ControllerFilter")
public class ControllerFilter implements Filter {

	/**
	 * Default constructor.
	 */
	public ControllerFilter() {

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {

	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		String path = req.getRequestURI().substring(req.getContextPath().length());

		if (path.startsWith("/htmlresources/") || path.equals("/") || path.equals("/about.html")
				|| path.contains("%20")) {
			chain.doFilter(request, response);
		} else {
			path = path.replaceAll("[^a-zA-Z0-9_/@-]", "_");
			request.getRequestDispatcher("/d" + path).forward(request, response);
		}

	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {

	}

}
