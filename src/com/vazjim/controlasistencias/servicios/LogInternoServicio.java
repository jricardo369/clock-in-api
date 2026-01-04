package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.LogLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.LogInterno;

@Path("LogInterno")
public class LogInternoServicio {

	private static Logger log = Logger.getLogger(LogInternoServicio.class);

	String json = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerConfiguraciones(@QueryParam("fechai")String fechai,@QueryParam("fechaf")String fechaf,
			@QueryParam("tipo")String tipo,@QueryParam("accion")String accion) {

		log.info("Obtener log de " + fechai + " a " + fechaf + " con tipo:" + tipo + " acción:" + accion);
		LogLogica logLogica = new LogLogica();
		List<LogInterno> lista = null;

		try {
			lista = logLogica.obtener(fechai, fechaf,tipo,accion);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("LOG-OBTENER", "ES",lista);
	}

}