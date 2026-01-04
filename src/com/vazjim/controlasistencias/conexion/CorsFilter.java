package com.vazjim.controlasistencias.conexion;

import java.io.IOException;
import java.util.Properties;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vazjim.controlasistencias.utilidades.Propiedades;

import io.jsonwebtoken.Jwts;

public class CorsFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(CorsFilter.class);

	boolean validarToken = false;
	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getProperties();
	private static final String AUTHORZATION_HEADER = "Authorization";
	private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer";

	// == URLS EXCLUYENTES DE TOKEN == //
	private static final String URI_LOGIN = "IniciarSesion/iniciar-sesion";

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		log.info("CORSFilter HTTP Request: " + request.getMethod());

		// Authorize (allow) all domains to consume the content
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Max-Age", "3600");

		// For HTTP OPTIONS verb/method reply with OK status code -- per CORS handshake
		if (request.getMethod().equals("OPTIONS")) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		if (validarToken) {
			StringBuffer uri = request.getRequestURL();
			log.info("REQUEST PATH:" + uri);
			if (!uri.toString().contains(URI_LOGIN)) {

				String header = request.getHeader(AUTHORZATION_HEADER);
				log.info("HEADER:" + header);
				String token = "";
				if (header != null) {
					token = header.substring(AUTHORIZATION_HEADER_PREFIX.length()).trim();
					log.info("TOKEN:" + token);
				}

				if (validarToken(token)) {	
					chain.doFilter(request, servletResponse);
				} else {
					log.info("Error");
					String url = "http://" + request.getContextPath() + request.getRequestURI();
					response.reset();
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setHeader("Location", url);
				}
			} else {
				chain.doFilter(request, servletResponse);
			}
		} else {
			chain.doFilter(request, servletResponse);
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

	private boolean validarToken(String authToken) {
		boolean salida = true;
		try {
			// Valida que el token, si el token es incorrecto regresa una
			// excepción
			Jwts.parser().setSigningKey(prop.getProperty("id_sesion").getBytes()).parseClaimsJws(authToken);
		} catch (Exception e) {
			salida = false;
			return salida;
		}
		return salida;
	}
}