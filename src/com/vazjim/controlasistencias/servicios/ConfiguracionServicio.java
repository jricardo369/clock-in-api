package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Configuraciones")
public class ConfiguracionServicio {

	private static Logger log = Logger.getLogger(ConfiguracionServicio.class);

	String json = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerConfiguraciones() {

		log.info("Obtener configuraciones");
		List<Configuracion> lista = null;

		try {
			lista = ConfiguracionLogica.obtener("");
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",lista);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		
	}
	@GET
	@Path("/por-identificado/{identificador}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerConfiguracion(@PathParam("identificador") String identificador) {

		log.info("Obtener configuraciones");
		List<Configuracion> lista = null;

		try {
			lista = ConfiguracionLogica.obtener(identificador);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",lista);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		
	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String editarConfig(String JSONasueto) {

		Utilidades util = new Utilidades();

		ConfiguracionLogica configLogica = new ConfiguracionLogica();
		Gson gSon = new GsonBuilder().create();
		Configuracion config = new Configuracion();

		json = util.isJSONValid(JSONasueto);

		if (!json.equals("")) {
			return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
		}

		config = (Configuracion) gSon.fromJson(JSONasueto, Configuracion.class);
		try {

			configLogica.actualizarCampo(config.getIdentificador(), config.getValorAbajo());

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompleto("CONF-ACT", "ES");
	}

}