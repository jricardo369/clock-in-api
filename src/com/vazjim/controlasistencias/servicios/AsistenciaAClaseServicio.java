package com.vazjim.controlasistencias.servicios;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.logica.AsistenciaAClaseLogica;
import com.vazjim.controlasistencias.logica.AsistenciaMultaLogica;
import com.vazjim.controlasistencias.logica.ClaseLogica;
import com.vazjim.controlasistencias.logica.ConfiguracionLogica;
import com.vazjim.controlasistencias.logica.InscripcionLogica;
import com.vazjim.controlasistencias.logica.MensajeLogica;
import com.vazjim.controlasistencias.logica.UsuarioLogica;
import com.vazjim.controlasistencias.modelo.AsistenciaAClase;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LugaresClaseSeccionado;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

@Path("AsistenciaClases")
public class AsistenciaAClaseServicio {

	private static Logger log = Logger.getLogger(AsistenciaAClaseServicio.class);

	@GET
	@Path("/{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuarios(@PathParam("idClase") int idClase, @QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad)
			throws ParseException {

		log.info("Otener asistencia a clase " + idClase + " de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaAClase asistenciaAClase = null;
		if (sociedad == 0) {
			sociedad = 1;
		}

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}
			asistenciaAClase = logica.obtenerAsistenciaAClase(idClase, fecha,sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseLogica cl = new ClaseLogica();
				Clase clase = cl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,false);
				int lugaresDisp = logica.obtenerNumLugaresUsados(idClase, fecha);
				asistenciaAClase.setLugares("Lugares " + lugaresDisp + " / " + clase.getPersonas());
				asistenciaAClase.setClase(clase);
			}
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/{idClase}/{idEntrenador}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciaDeUsuariosDeEntrenador(@PathParam("idClase") int idClase,@PathParam("idEntrenador") int idEntrenador, @QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad)
			throws ParseException {

		log.info("Otener asistencia a clase " + idClase + " de entrenador "+idEntrenador +" de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaAClase asistenciaAClase = null;

		try {

			if (fecha == null) {
				fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
			}
			asistenciaAClase = logica.obtenerAsistenciaAClaseProfesor(idClase, fecha, idEntrenador,sociedad);

			if (asistenciaAClase.getUsuarios().isEmpty()) {
				ClaseLogica cl = new ClaseLogica();
				Clase clase = cl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
				asistenciaAClase.setClase(clase);
			}
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@POST
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String agregarUsuarioDeClase(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("lugar") String lugar,@QueryParam("sociedad") int sociedad) throws ParseException {

		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		String asistenciaAClase = "";
		boolean horarioFueraDeClase = false;

		log.info("Agregando asistencia Id clase:" + idClase + " / Id usuario:" + idUsuario + " / Fecha:" + fecha+ " / Lugar:" + lugar);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}
		try {
			
			//Validar duplicidad en lugar,fecha y clase
			boolean vd = logica.lugarOcupado(fecha, lugar, idClase);
			if (vd) {
				return MensajeLogica.obtenerMensajeCompleto("AC-ASISCLASE-DUP", "ES");
			}

			// Buscar clase
			ClaseLogica logicaCl = new ClaseLogica();
			Clase c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Tomando usuario
			List<Usuario> usuario = logicaUs.obtener("id_usuario", String.valueOf(idUsuario),sociedad,false);
			// Validando que exista el usuario
			log.info("--- Validando que usuario exista");
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				// Validando que el usuario este activo
				log.info("--- Validando que usuario este activo");
				String estatus = usuario.get(0).getEstatus() != null ? usuario.get(0).getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
				if (estatus.equals("3")) {
					return MensajeLogica.obtenerMensajeCompleto("US-BLOQUEADO", "ES");
				}
			}

			// Tomando personas permitidas de clase
			log.info("--- Validando personas permitidas");
			int numPersonasPermitidas = Integer.valueOf(c.getPersonas());
			int numPersonsasActuales = logica.obtenerNumeroDePersonasDeClase(idClase, fecha);
			if (numPersonasPermitidas == numPersonsasActuales) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-CLASELLENA", "ES");
			}

			// Validar que no este dado de alta en la clase
			log.info("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
			Clase clase = logica.estaEnClaseEnFecha(fecha, idUsuario);
			if (clase != null) {
				return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
						clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
			}

			// Tomar si es fecha actual ono
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			log.info("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);

			if (compFechas == 0) {

				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.info("Clase fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}
				
			} else if (compFechas == -1) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			}

			// Validar que tipo de inscripcion tiene
			InscripcionUs insus = logica.validoInscripcionUsuario(Integer.valueOf(idUsuario),false);
			if (!insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}
			System.out.println("Es por clases:"+insus.isEsPorClases());

			log.info("--- Paso todas las validaciones para poder agregarlo a clase");
			if(lugar == null){
				lugar = "0";
			}
			
			// Restar en caso que sea por clases
			if (insus.isEsPorClases()) {
				InscripcionLogica insLogica = new InscripcionLogica();
				int clasesRest = insus.getInscripcion().getClases_restantes() - 1;
				insLogica.actualizarClasesRestantes(insus.getInscripcion().getIdInscripcion(), clasesRest);
			}
						
			asistenciaAClase = logica.agregarUsuarioAClase(idUsuario, idClase, fecha,lugar);
	

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}

	@DELETE
	@Path("{idClase}/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String eliminarUsuarioAClase(@PathParam("idClase") int idClase, @PathParam("idUsuario") int idUsuario,
			@QueryParam("fecha") String fecha,@QueryParam("sociedad") int sociedad) throws ParseException {

		boolean horarioFueraDeClase = false;

		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaMultaLogica logicaM = new AsistenciaMultaLogica();
		InscripcionLogica ins = new InscripcionLogica();
		UsuarioLogica logicaUs = new UsuarioLogica();
		Usuario us = new Usuario();
		boolean validarMultas =  true;

		log.info("Eliminando asistencia Id clase:" + idClase + " / Id usuario:" + idUsuario + " / Fecha:" + fecha);

		if (fecha == null) {
			fecha = Utilidades.generarFecha(true, false, false, "", 0, null);
		}

		String asistenciaAClase = "";
		try {

			// Buscar clase
			ClaseLogica logicaCl = new ClaseLogica();
			Clase c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}

			// Tomando usuario
			List<Usuario> usuario = logicaUs.obtener("id_usuario", String.valueOf(idUsuario),sociedad,false);
			// Validando que exista el usuario
			log.info("--- Validando que usuario exista");
			if (usuario.isEmpty()) {
				return MensajeLogica.obtenerMensajeCompleto("US-NOEXISTENTE", "ES");
			} else {
				us = usuario.get(0);
				// Validando que el usuario este activo
				log.info("--- Validando que usuario este activo");
				String estatus = us.getEstatus() != null ? us.getEstatus() : "";
				if (estatus.equals("0")) {
					return MensajeLogica.obtenerMensajeCompleto("US-INACTIVO", "ES");
				}
			}

			// Validar que no este dado de alta en la clase
			log.info("--- Validando que no este dado de alta el usuario en alguna clase en la fecha");
			Clase clase = logica.estaEnClaseEnFecha(fecha, idUsuario);
			if (clase != null) {
				if (Integer.valueOf(clase.getIdClase()) != idClase) {
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-USUARIOENCLASE", "ES",
							clase.getNombre(), Utilidades.diaSemana(fecha), "", "");
				}
			}
			
			// Validar que tipo de inscripcion tiene 
			InscripcionUs insus = logica.validoInscripcionUsuario(Integer.valueOf(idUsuario),true);
			if (!insus.getMensaje().equals("")) {
				return insus.getMensaje();
			}
			System.out.println("Es por clases:"+insus.isEsPorClases());

			// Tomar si es fecha actual o no
			String fechaRecibida = Utilidades.generarFecha(true, false, false, "", 0, fecha);
			log.info("--- Comparando si es fecha actual o no");
			int compFechas = Utilidades.compararFechaActualVsFecha(fechaRecibida);

			if (compFechas == 0) {

				String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				
				log.info("·····Validar si ya paso el horario de la clase");
				horarioFueraDeClase = logica.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

				if (horarioFueraDeClase) {
					log.info("Clase fuera de horario");
					return MensajeLogica.obtenerMensajeCompletoConParametros("ASISCLASE-FUERAHORARIO-A", "ES",
							c.getNombre(), "", "", "");
				}
				
				log.info("·····Validar si se es fuera de clase con horario");
				String horaInicio = c.getHoraInicio();

				int totalMultasPermitidas = 0;
				
				if (validarMultas) {
					
					log.info("Total faltas:"+us.getTotalMultas() + " Contador multas:"+us.getContadorFaltas());
					horarioFueraDeClase = logica.horarioParaValFaltaV2(hora, horaInicio, c.getHorario());
					Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("TOTAL-FALTAS-PERMITIDAS");
					totalMultasPermitidas = Integer.valueOf(conf.getValorAbajo());
					log.info("Total totalMultasPermitidas:"+totalMultasPermitidas);
					
				}

				log.info("horarioFueraDeClase:"+horarioFueraDeClase);
				
				if (horarioFueraDeClase) {
					
					if (validarMultas) {
						
						Inscripcion insc = ins.obtenerUlimaInscripcionDeUsuario(us.getIdUsuario());
						if (insc.getTipoInscripcionDesc().equals("Mensual")) {
							
							int cF = us.getContadorFaltas() + 1;
							log.info("Clase fuera de horario para multa");
							log.info("Contador Faltas:" + cF);
							boolean val = us.getContadorFaltas() >= totalMultasPermitidas;
							log.info("Contador Faltas mayor que multas permitidas " + val);
							if (cF >= totalMultasPermitidas) {
								int ct = us.getTotalMultas() + 1;
								logicaUs.actualizarCampo("total_multas", String.valueOf(ct), idUsuario);
								logicaUs.actualizarCampo("contador_faltas", "0", idUsuario);
								//Agregar a tabla de asistencia multa
								Clase acu = logica.obtenerAsistenciaDeUsuario(idClase, fecha, idUsuario);
								logicaM.agregarUsuarioAAsistenciaMulta(idUsuario, idClase, fecha, acu.getLugar());
							} else {
								logicaUs.actualizarCampo("contador_faltas", String.valueOf(cF), idUsuario);
							}
							
						} else {

							// No se regresa clase por que ya se paso de horario
							// int cr = insc.getClases_restantes()+1;
							// ins.actualizarClasesRestantes(insc.getIdInscripcion(),
							// cr);
							
							int cF = us.getContadorFaltas() + 1;
							log.info("Clase fuera de horario para multa");
							log.info("Contador Faltas:" + cF);
							boolean val = us.getContadorFaltas() >= totalMultasPermitidas;
							log.info("Contador Faltas mayor que multas permitidas " + val);
							if (cF >= totalMultasPermitidas) {
								int ct = us.getTotalMultas() + 1;
								logicaUs.actualizarCampo("total_multas", String.valueOf(ct), idUsuario);
								logicaUs.actualizarCampo("contador_faltas", "0", idUsuario);
								//Agregar a tabla de asistencia multa
								Clase acu = logica.obtenerAsistenciaDeUsuario(idClase, fecha, idUsuario);
								logicaM.agregarUsuarioAAsistenciaMulta(idUsuario, idClase, fecha, acu.getLugar());
							} else {
								logicaUs.actualizarCampo("contador_faltas", String.valueOf(cF), idUsuario);
							}
							
						}
					}
					
					//return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FUERAHORARIO-E", "ES");
				} else {
					
					log.info("Fecha actual y posible eliminar");
					// Sumar en caso que sea por clases
					InscripcionLogica insLogica = new InscripcionLogica();
					Inscripcion insc = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);
					
					if(!insc.getTipoInscripcionDesc().equals("Mensual")){
						int clasesRest = insc.getClases_restantes() + 1;
						insLogica.actualizarClasesRestantes(insc.getIdInscripcion(), clasesRest);
					}
					
				}

			} else if (compFechas == -1) {
				return MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAANTERIOR", "ES");
			} else if (compFechas > 0) {
				
				// Restar en caso que sea por clases
				if (insus.isEsPorClases()) {
					InscripcionLogica insLogica = new InscripcionLogica();
					Inscripcion insc = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);
					int clasesRest = insc.getClases_restantes() + 1;
					insLogica.actualizarClasesRestantes(insc.getIdInscripcion(), clasesRest);
				}
			}

			log.info("--- Paso todas las validaciones para poder eliminar");
			asistenciaAClase = logica.eliminarUsuarioAClase(idUsuario, idClase, fecha);

			return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", asistenciaAClase);

		} catch (SQLException e) {
			return MensajeLogica.errorBD(e);
		}

	}
	
	@GET
	@Path("/lugares/{idClase}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerLugaresDisponibles(@PathParam("idClase") int idClase, @QueryParam("fecha") String fecha)
			throws ParseException {

		log.info("Otener lugares disponibles a clase " + idClase + " de fecha " + fecha);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();	
		
		LugaresClaseSeccionado lcs = logica.lugaresClase(idClase, fecha);
				
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lcs);

		

	}
	
	@GET
	@Path("/asistencias-usuario/{idUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenerAsistenciasUsuario(@PathParam("idUsuario") int idUsuario)
			throws ParseException {

		log.info("Otener asistencias usuario " + idUsuario);
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		AsistenciaMultaLogica logicaM = new AsistenciaMultaLogica();
		
		List<Clase> lc = null;
		try {
			lc = logica.obtenerAsistenciasUsuario(idUsuario);
			lc.addAll(logicaM.obtenerAsistenciaMultas(idUsuario));
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		return MensajeLogica.obtenerMensajeCompletoConRespuesta("", "", lc);
	}
	
	@GET
	@Path("/prueba-horario")
	@Produces(MediaType.APPLICATION_JSON)
	public String pruebaHorario(@QueryParam("fecha") String fecha,@QueryParam("horaInicio") String horaInicio,@QueryParam("horario") String horario)
			throws ParseException {
		
		AsistenciaAClaseLogica logica = new AsistenciaAClaseLogica();
		int hm = 0;

		StringBuilder s = new StringBuilder();
		String hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
		
		int hn = Integer.valueOf(hora)-1;
		
		s.append("HoraActual " + hora+"\n");
		s.append("HoraActual menos uno " + hn+"\n");
		s.append("horaInicio " + horaInicio+"\n");
		hm = Integer.valueOf(horaInicio)-5;
		s.append("horaMin " + hm+"\n");

		
		
		
		/*boolean hF = logica.horarioFueraDeClaseConValNumHora(hora, horaInicio, horario);
		
		s.append("horarioFueraDeClase "+hF+"\n");
		
		s.append("-----------------------*****\n");*/
		
		boolean hP = logica.horarioParaValFaltaV2("12", horaInicio, horario);
		
		s.append("horarioParaValFalta "+hP+"\n");
		
		s.append("-----------------------*****\n");
		
		return s.toString();

	}

	@GET
	@Path("/validar-eliminar-asistencia")
	@Produces(MediaType.APPLICATION_JSON)
	public String validarBajaAsistencia(@QueryParam("fecha") String fecha,@QueryParam("horaInicio") String horaInicio,@QueryParam("horarioClase") String horarioClase,
			@QueryParam("idClase") int idClase)
			throws ParseException {

		StringBuilder salida = new StringBuilder();
		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();
		Clase c = null;

		salida.append("-------------------------\n");
		salida.append("VALIDANDO CON FECHA Y HORARIOS ENVIADOS\n");
		salida.append("fecha:"+fecha +"\n");
		salida.append("horaInicio:"+horaInicio +"\n");
		salida.append("horarioClase:"+horarioClase +"\n");

		boolean horarioFueraDeClase = false;
		boolean horarioFueraPorFalta = false;
		String hora = "";
		String horaYMin = "";
		//hora = "5";
		try {
			hora = Utilidades.generarFecha(false, true, false, "", 0, "2025-10-05").substring(0, 2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		salida.append("Validar si ya paso el horario de la clase\n");
		c = new Clase();
		c.setHoraInicio(horaInicio);
		c.setHorario(horarioClase);		
		c.setNombre("Clase prueba");
		horarioFueraDeClase = al.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

		if (horarioFueraDeClase) {

			log.info("Clase fuera de horario");
			salida.append("Error:"+MensajeLogica.obtenerMensajeDesc("ASISCLASE-FUERAHORARIO-A", "ES")+"\n");

		}else{

			salida.append("Horario dentro de clase\n");

		}

		salida.append("Hora obtenida:" + hora + "\n");
		horarioFueraPorFalta = al.horarioParaValFaltaV2(hora, horaInicio, horarioClase);		
		
		System.out.println("Horario fuera de clase:"+horarioFueraPorFalta);
		salida.append("Horario fuera de clase:"+horarioFueraPorFalta+"\n");

		salida.append("-------------------------\n");
		salida.append("VALIDANDO POR ID CLASE\n");
		
		ClaseLogica logicaCl = new ClaseLogica();

		try {
			
			c = new Clase();
			c = logicaCl.obtenerClase("id_clase", String.valueOf(idClase),1,true);
			if (c == null) {
				return MensajeLogica.obtenerMensajeCompleto("CLASES-NOXISTE", "ES");
			}		
			
			String horaYmin = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 5);
			
			salida.append("idClase:"+idClase +"\n");
			salida.append("Clase:"+c.getNombre() +"\n");
			salida.append("horaInicio:"+c.getHoraInicio() +"\n");
			salida.append("horarioClase:"+c.getHorario() +"\n");
			salida.append("horario:"+c.getHorario() +"\n");
			salida.append("Hora y min obt:"+horaYmin +"\n");
			
			horarioFueraDeClase = al.horarioFueraDeClase(hora, c.getHoraInicio(), c.getHorario());

			if (horarioFueraDeClase) {
				log.info("Clase fuera de horario");
				salida.append("Error:"+MensajeLogica.obtenerMensajeDesc("ASISCLASE-FUERAHORARIO-A", "ES"));
			}	
		
			horarioFueraPorFalta = al.horarioParaValFaltaV2(hora, horaInicio, horarioClase);		
			
			salida.append("Horario fuera de clase:"+horarioFueraPorFalta);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return salida.toString();

		

	}


	public static void main(String args[]) throws SQLException {
		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();
		
		
		try {
			boolean horarioFueraDeClase = false;
			String hora;
			hora = Utilidades.generarFecha(false, true, false, "", 0, "2026-01-03").substring(0, 2);
			String horaInicio = "13";
			String horarioClase = "";
			log.info("Hora:" + hora);
			log.info("Hora Inicio:" + horaInicio);
			

			horarioFueraDeClase = al.horarioParaFalta(hora, horaInicio, horarioClase);
			
			al.horarioFueraDeClase(hora, horarioClase, horaInicio);
			
			System.out.println("Horario fuera de clase:"+horarioFueraDeClase);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

}