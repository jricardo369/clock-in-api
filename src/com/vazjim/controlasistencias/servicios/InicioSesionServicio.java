package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InicioSesionLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("IniciarSesion")
public class InicioSesionServicio {

	private static final Logger log = LoggerFactory.getLogger(InicioSesionServicio.class);

	@POST
	@Path("iniciar-sesion")
	@Produces(MediaType.APPLICATION_JSON)
	public String iniciarSesion(String JSONAut) {

		Utilidades util = new Utilidades();
		InicioSesionLogica usuarioObj = new InicioSesionLogica();
		UsuarioLogica usuarioLogica = new UsuarioLogica();

		String token = "";
		String json = "";
		Gson gSon = new GsonBuilder().create();
		Usuario usuario = null;
		String us = "";
		String pass = "";

		json = util.isJSONValid(JSONAut);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		usuario = (Usuario) gSon.fromJson(JSONAut, Usuario.class);

		if ("".equals(usuario.getUsuario()) || "".equals(usuario.getContrasenia())) {
			return MensajeLogica.obtenerMensajeCompleto("US-DATOSLOGIN-VACIOS", "ES");
		}

		Usuario u = null;
		try {

			if (usuario.getUsuario() != null) {
				us = usuario.getUsuario().toUpperCase();
			}
			if (usuario.getContrasenia() != null) {
				pass = usuario.getContrasenia().toUpperCase();
			}

			boolean usuarioExiste = usuarioLogica.usuarioExiste(us);
			u = usuarioObj.iniciarSesion(us, pass);
			if (u != null) {
				if (u.getEstatus().equals("1")) {
					token = usuarioObj.generarToken(us);
					u.setToken(token);
				} else if (u.getEstatus().equals("0")) {
					u = null;
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				} else if (u.getEstatus().equals("3")) {
					u = null;
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQ-PAGO", "ES");
				}
			} else {
				if (usuarioExiste) {

					Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("LIMITE-INTENTOS");
					int intentosPermitidos = 10;
					if (conf != null) {
						log.info("Limite intentos:" + conf.getValorAbajo());
						intentosPermitidos = Integer.parseInt(conf.getValorAbajo());
					}
					List<Usuario> listaUsuario = usuarioLogica.obtener("usuario", us,0,false);
					int intentos = listaUsuario.get(0).getIntentos() + 1;
					if (intentos > intentosPermitidos) {
						return MensajeLogica.obtenerMensajeCompleto("US-INTENTOS-FALLIDOS", "ES");
					} else {
						usuarioLogica.actualizarCampo("intentos", String.valueOf(intentos), listaUsuario.get(0).getIdUsuario());
					}

				}
				return MensajeLogica.obtenerMensajeCompleto("US-DATOS-INCORRECTOS", "ES");
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", u);
	}
	
	@GET
	@Path("iniciar-sesion")
	@Produces(MediaType.APPLICATION_JSON)
	public String INI(String JSONAut) {

		Utilidades util = new Utilidades();
		InicioSesionLogica usuarioObj = new InicioSesionLogica();
		UsuarioLogica usuarioLogica = new UsuarioLogica();

		String token = "";
		String json = "";
		Gson gSon = new GsonBuilder().create();
		Usuario usuario = null;
		String us = "";
		String pass = "";

		json = util.isJSONValid(JSONAut);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		usuario = (Usuario) gSon.fromJson(JSONAut, Usuario.class);

		if ("".equals(usuario.getUsuario()) || "".equals(usuario.getContrasenia())) {
			return MensajeLogica.obtenerMensajeCompleto("US-DATOSLOGIN-VACIOS", "ES");
		}

		Usuario u = null;
		try {

			if (usuario.getUsuario() != null) {
				us = usuario.getUsuario().toUpperCase();
			}
			if (usuario.getContrasenia() != null) {
				pass = usuario.getContrasenia().toUpperCase();
			}

			boolean usuarioExiste = usuarioLogica.usuarioExiste(us);
			u = usuarioObj.iniciarSesion(us, pass);
			if (u != null) {
				if (u.getEstatus().equals("1")) {
					token = usuarioObj.generarToken(us);
					u.setToken(token);
				} else if (u.getEstatus().equals("0")) {
					u = null;
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				} else if (u.getEstatus().equals("3")) {
					u = null;
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQ-PAGO", "ES");
				}
			} else {
				if (usuarioExiste) {

					Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("LIMITE-INTENTOS");
					int intentosPermitidos = 10;
					if (conf != null) {
						log.info("Limite intentos:" + conf.getValorAbajo());
						intentosPermitidos = Integer.parseInt(conf.getValorAbajo());
					}
					List<Usuario> listaUsuario = usuarioLogica.obtener("usuario", us,0,false);
					int intentos = listaUsuario.get(0).getIntentos() + 1;
					if (intentos > intentosPermitidos) {
						return MensajeLogica.obtenerMensajeCompleto("US-INTENTOS-FALLIDOS", "ES");
					} else {
						usuarioLogica.actualizarCampo("intentos", String.valueOf(intentos), listaUsuario.get(0).getIdUsuario());
					}

				}
				return MensajeLogica.obtenerMensajeCompleto("US-DATOS-INCORRECTOS", "ES");
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", u);
	}

}
