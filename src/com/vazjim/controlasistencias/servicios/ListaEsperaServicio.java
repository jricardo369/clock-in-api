package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.ListaEsperaLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.modelo.ListaEspera;
import com.vazjim.controlasistencias.utilidades.Propiedades;

@Path("lista-espera")
public class ListaEsperaServicio {

	private static Logger log = Logger.getLogger(ListaEsperaServicio.class);

	public static Propiedades p = new Propiedades();
	public static Properties prop = p.getPropertiesErrores();

	String json = "";
	String respuesta = "";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerListasEspera(@QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad) {

		log.info("Obtener listas espera:" + fecha);
		ListaEsperaLogica logica = new ListaEsperaLogica();
		List<ListaEspera> lista = null;
		if(fecha == null){
			fecha = "";
		}

		try {
			lista = logica.obtener(null, "", sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lista);
	}

	@POST
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String insertar(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("lugar") String lugar,@QueryParam("sociedad") int sociedad) {

		ListaEsperaLogica logica = new ListaEsperaLogica();

		try {
					respuesta = logica.insertar(idClase, fecha, idUsuario);
			

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("LISTA-ESP-INSERT", "ES", respuesta);
	}

	@DELETE
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarUsuario(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("lugar") String lugar,@QueryParam("sociedad") int sociedad) {

		ListaEsperaLogica logica = new ListaEsperaLogica();
		boolean conError = false;


		try {
			if (!conError) {
				respuesta = logica.eliminar(idClase, fecha, idUsuario);
			} else {
				return MensajeLogica.obtenerMensajeCompleto("ERROR-ELIMINAR-ASUETO", "ES");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return MensajeLogica.obtenerMensajeCompletoConRespuesta("LISTA-ESP-DEL", "ES", respuesta);
	}

}