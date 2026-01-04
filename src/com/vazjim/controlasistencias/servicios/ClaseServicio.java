package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vazjim.controlasistencias.logica.AsuetoLogica;
import com.vazjim.controlasistencias.logica.ClaseLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("Clases")
public class ClaseServicio {

	private static final Logger log = LoggerFactory.getLogger(ClaseServicio.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClases(@QueryParam("sociedad") int sociedad) {

		ClaseLogica logica = new ClaseLogica();
		List<Clase> asistenciaAClase = null;
		try {
			asistenciaAClase = logica.obtenerClases(null, null,sociedad,false,false);
			log.info("asistenciaAClase:{}", asistenciaAClase.size());
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",asistenciaAClase);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		
	}
	
	@GET
	@Path("/activas-para-admin/{fecha}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClasesActivasParaAdmin(@QueryParam("sociedad") int sociedad,@PathParam("fecha") String fecha) {

		ClaseLogica logica = new ClaseLogica();
		List<Clase> asistenciaAClase = null;
		try {
			asistenciaAClase = logica.obtenerClases("c.dia", fecha,sociedad,false,true);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",asistenciaAClase);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		
	}

	@GET
	@Path("/{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClase(@PathParam("idClase") String idClase) {

		ClaseLogica logica = new ClaseLogica();
		Clase asistenciaAClase = null;
		try {
			asistenciaAClase = logica.obtenerClase("id_clase", idClase,0,true);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",asistenciaAClase);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		
	}

	@GET
	@Path("/por-fecha/{fecha}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClasesDeFecha(@PathParam("fecha") String fecha,@QueryParam("sociedad")int sociedad) {

		log.info("Clases por fecha");

		ClaseLogica logica = new ClaseLogica();
		List<Clase> clases = null;
		try {
			clases = logica.obtenerClasesPorFecha(fecha,0,sociedad,0);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",clases);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

		
	}
	
	@GET
	@Path("/por-fecha/{fecha}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClasesDeFechaYUsuario(@PathParam("fecha") String fecha,@PathParam("idUsuario") int idUsuario,@QueryParam("sociedad")int sociedad) {

		log.info("Clases por fecha usuario");
		boolean esAsuetoGeneral = false;

		ClaseLogica logica = new ClaseLogica();
		AsuetoLogica asl = new AsuetoLogica();
		List<Clase> clases = null;
		
		try {
			
			// Validar que no sea aseuto general de todas las clases
			esAsuetoGeneral = asl.esAsuetoGeneral(fecha,sociedad);

			if (esAsuetoGeneral) {
				
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOCLASES", "ES");
				
			} else {
				
				clases = logica.obtenerClasesPorFecha(fecha, idUsuario,sociedad,0);
				return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",clases);
				
			}
			
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
	}
	
	@GET
	@Path("/por-fecha/profesor/{fecha}/{idProfesor}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerClasesDeFechaYUsuarioProfesor(@PathParam("fecha") String fecha,@PathParam("idProfesor") int idProfesor,@QueryParam("sociedad")int sociedad) {

		log.info("Clases por fecha profesor");
		boolean esAsuetoGeneral = false;

		ClaseLogica logica = new ClaseLogica();
		AsuetoLogica asl = new AsuetoLogica();
		List<Clase> clases = null;
		
		try {
			
			// Validar que no sea aseuto general de todas las clases
			esAsuetoGeneral = asl.esAsuetoGeneral(fecha,sociedad);

			if (esAsuetoGeneral) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOCLASES", "ES");
			} else {
				clases = logica.obtenerClasesPorFecha(fecha, 0,sociedad,idProfesor);
				return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",clases);
			}
			
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
	}

	@GET
	@Path("/esta-en-clase/{idClase}/{idUsuario}/{fecha}")
	@Produces(MediaType.APPLICATION_JSON)
	public String usuarioEstaEnClase(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario, @PathParam("fecha") String fecha,
			@QueryParam("sociedad") int sociedad) {

		log.info("Clases por fecha");

//		AsistenciaAClaseLogica logicaAsistenciaClase = new AsistenciaAClaseLogica();
		ClaseLogica logica = new ClaseLogica();
		try {
//			// Validar que no este dado de alta en la clase
//			log.info("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
//			Clase clase = logicaAsistenciaClase.estaEnClaseEnFecha(fecha, idUsuario);
//			if (clase != null) {
//				if (Integer.valueOf(clase.getIdClase()) != idClase) {
//					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES", clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
//				}
//			}
			return logica.obtenerUsuarioDeClase(idUsuario, idClase, fecha,sociedad);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String crearClase(String JSONClase) {

		log.info("json entrada:"+JSONClase);
		Utilidades util = new Utilidades();

		ClaseLogica logica = new ClaseLogica();
		Gson gSon = new GsonBuilder().create();
		Clase clase = new Clase();

		String asistenciaAClase = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONClase);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			clase = (Clase) gSon.fromJson(JSONClase, Clase.class);

			boolean duplicado = logica.validarDuplicidadClase(clase.getNombre(), clase.getHoraInicio(), clase.getHoraFin(),clase.getDia(),clase.getHorario(),clase.getSociedad());

			if (duplicado) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-DULICADA", "ES");
			} else {

				UsuarioLogica us = new UsuarioLogica();
				List<Usuario> lu = us.obtener("id_usuario", clase.getProfesor(),clase.getSociedad(),false);

				if (lu != null) {
					if (!lu.isEmpty()) {
						asistenciaAClase = logica.insertar(clase);
						return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);
					} else {
						return MensajeLogica.obtenerMensajeCompleto("CLASE-PRO-NOEXISTE", "ES");
					}

				}else{
					return MensajeLogica.obtenerMensajeCompleto("CLASE-PRO-NOEXISTE", "ES");
				}
			}

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String actualizarClase(String JSONClase) {

		Utilidades util = new Utilidades();

		ClaseLogica logica = new ClaseLogica();
		Gson gSon = new GsonBuilder().create();
		Clase clase = new Clase();

		String asistenciaAClase = "";
		String json = "";
		try {

			json = util.isJSONValid(JSONClase);

			if (!json.equals("")) {
				return MensajeLogica.obtenerMensajeCompleto("JSON-INVALIDO", "ES");
			}

			clase = (Clase) gSon.fromJson(JSONClase, Clase.class);
			asistenciaAClase = logica.actualizar(clase);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarClase(@PathParam("idClase") int idClase) {

		ClaseLogica logica = new ClaseLogica();

		String asistenciaAClase = "";
		try {

			asistenciaAClase = logica.eliminar(idClase);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}
		
	}

	@GET
	@Path("/duplicidad/{hi}/{hf}/{n}")
	@Produces(MediaType.APPLICATION_JSON)
	public String claseDuplicada(@PathParam("hi") String hi, @PathParam("hf") String hf, @PathParam("n") String n) {

		log.info("Clases por fecha");

		ClaseLogica logica = new ClaseLogica();
		boolean r = false;
		try {
			r = logica.validarDuplicidadClase(n, hi, hf,"","",0);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "",r);
		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

}