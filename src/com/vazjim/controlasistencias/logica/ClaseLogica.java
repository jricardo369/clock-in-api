package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.ListaEspera;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Respuesta;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class ClaseLogica {

	private static final Logger log = LoggerFactory.getLogger(ClaseLogica.class);

	public List<Clase> obtenerClases(String columna, String valor, int sociedad,boolean activas,boolean conLugares) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		AsistenciaAClaseLogica al = null;
		
		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		String horario = "";

		List<Clase> clases = new ArrayList<>();
		Clase clase;

		querySql = "SELECT c.id_clase,c.nombre,c.hora_inicio,c.hora_fin,c.horario,c.personas,c.profesor,c.estatus,c.sociedad,c.dia,u.nombre FROM clase c "
				  +"JOIN usuario u ON u.id_usuario = c.profesor ";

		String where = "";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " c.sociedad = " + sociedad + " ";
		}
		
		if (activas == true) {
			if (!where.equals(""))
				where += " AND ";
			where += " c.estatus = '1'";
		}
		
		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}
		
		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY c.dia,c.horario,c.hora_inicio";
			querySql += where;
		}

	log.info("query: {}", querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();
			String dia = "";
			
			if (conLugares) {
				al = new AsistenciaAClaseLogica();
			}

			while (rs != null && rs.next()) {

				clase = new Clase();
				clase.setIdClase(rs.getInt(1));
				clase.setNombre(rs.getString(2));
				clase.setHoraInicio(rs.getString(3));
				clase.setHoraFin(rs.getString(4));
				clase.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				clase.setPersonas(rs.getString(6));
				clase.setProfesor(rs.getString(7));
				clase.setEstatus(rs.getString(8));
				clase.setSociedad(rs.getInt(9));
				clase.setDia(rs.getString(10));
				dia = rs.getString(10);
				if(dia != null){
					clase.setDiaDescripcion(Utilidades.fechaEnLetra(rs.getString(10)));
				}	

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else {
					horario = "PM";
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);
				clase.setDescCorta(clase.getDia()+" | "+clase.getHoraInicio() +" a "+ clase.getHoraFin() + " " + horario);
				clase.setEstatus(rs.getString(8));
				clase.setProfesorNombre(rs.getString(11));
				
			    if(conLugares){
					int numLugaresUsados = al.obtenerNumLugaresUsados(clase.getIdClase(), clase.getDia());
					if (numLugaresUsados == Integer.valueOf(clase.getPersonas())) {
						clase.setLugaresDisponibles("CLASE LLENA");
					}else{
						clase.setLugaresDisponibles(numLugaresUsados + " / " + clase.getPersonas());
					}	
			    }
			    
				clases.add(clase);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		log.info("Clases obtenidas:" + clases.size());

		return clases;
	}

	public List<Clase> obtenerClasesDisponiblesUsuario(String fecha, int sociedad,int idProfesor) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		String horario = "";

		List<Clase> clases = new ArrayList<>();
		Clase clase;

		querySql = "SELECT c.id_clase,c.nombre,c.hora_inicio,c.hora_fin,c.horario,c.personas,c.profesor,c.estatus,c.sociedad,c.dia,u.nombre " 
		+ "FROM clase c "
		+ "JOIN usuario u ON u.id_usuario = c.profesor "
		+ "WHERE NOT EXISTS (select a.id_clase from asueto a where a.id_clase = c.id_clase and a.fecha IN (?)) "
		+ "AND c.estatus = '1' AND c.dia = ? ORDER BY horario ASC,hora_inicio ASC ";
		
		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!querySql.equals(""))
				querySql += " AND ";
			querySql += " c.sociedad = ? ";
		}
		
		if (idProfesor != 0 && !"".equals(idProfesor)) {
			if (!querySql.equals(""))
				querySql += " AND ";
			querySql += " c.profesor = ? ";
		}
		
		

	log.info("query: {}", querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setString(2, fecha);
			if(sociedad != 0){
				st.setInt(3, sociedad);
			}
			if(idProfesor != 0){
				if(sociedad != 0){
					st.setInt(4, idProfesor);
				}else{
					st.setInt(3, idProfesor);
				}
			}
			rs = st.executeQuery();
			String dia = "";

			while (rs != null && rs.next()) {

				clase = new Clase();
				clase.setIdClase(rs.getInt(1));
				clase.setNombre(rs.getString(2));
				clase.setHoraInicio(rs.getString(3));
				clase.setHoraFin(rs.getString(4));
				clase.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				clase.setPersonas(rs.getString(6));
				clase.setProfesor(rs.getString(7));
				clase.setEstatus(rs.getString(8));
				clase.setSociedad(rs.getInt(9));
				clase.setDia(rs.getString(10));
				dia = rs.getString(10);
				if(dia != null){
					clase.setDiaDescripcion(Utilidades.fechaEnLetra(rs.getString(10)));
				}

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else {
					horario = "PM";
				}

				clase.setDescripcionHorario("" + Utilidades.convertirHoraA12Hrs(clase.getHoraInicio()) +  " a " + Utilidades.convertirHoraA12Hrs(clase.getHoraFin()) + " " + horario);
				clase.setEstatus(rs.getString(8));
				clase.setProfesorNombre(rs.getString(11));
				clase.setDescCorta(clase.getDia()+" | "+clase.getHoraInicio() +" a "+ clase.getHoraFin() + " " + horario);

				clases.add(clase);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		log.info("Clases obtenidas:" + clases.size());

		return clases;
	}

	public Clase obtenerClase(String columna, String valor,int sociedad,boolean activas) throws SQLException {

		Clase c = null;
		ClaseLogica logicaCl = new ClaseLogica();
		// Buscando clase
		List<Clase> clase = logicaCl.obtenerClases("id_clase", String.valueOf(valor), sociedad,activas,false);
		if (!clase.isEmpty()) {
			c = clase.get(0);
		}

		return c;

	}

	public List<Clase> obtenerClasesPorFecha(String fecha, int idUsuario, int sociedad,int idProfesor) throws SQLException {

		AsistenciaAClaseLogica acl = new AsistenciaAClaseLogica();
		ListaEsperaLogica lp = new ListaEsperaLogica();

		String fechaFormateada;
		List<Clase> clases = null;
		List<Clase> clasesUs = new ArrayList<>();
		String lugares = "";
		Clase estaEnClase = null;
		boolean horarioFueraDeClase = false;
		boolean valido = true;
		boolean fueraDeHorario = false;
		
		String claseLlena = MensajeLogica.obtenerMensajeDesc("C-CLASE-LLENA", "ES");
		boolean isClaseLlena = false;

		// Formatear fecha
		fechaFormateada = Utilidades.fechaEnLetra(fecha);

		clases = obtenerClasesDisponiblesUsuario(fecha,sociedad,idProfesor);

		try{
			for (Clase clase : clases) {

				String hora;

				hora = Utilidades.generarFecha(false, true, false, "", 0, fecha).substring(0, 2);
				horarioFueraDeClase = acl.horarioFueraDeClase(hora, clase.getHoraInicio(), clase.getHorario());

				log.info("clase: {}", clase.getDescCorta());
				if (horarioFueraDeClase) {
					log.info("Asistencia: {}", clase.getAsistencia());
					
					if (idUsuario != 0) {
						String fec = Utilidades.generarFecha(true, false, false, "", 0, fecha);
						log.info("fecha entrada: {}", fec);
						int c = Utilidades.compararFechaActualVsFecha(fec);
						if (c==0) {
							valido = false;
							fueraDeHorario = true;
						}else{
							valido = true;					
						}
						
						
					} else {
						valido =  true;
					}
				} else {
					fueraDeHorario = false;
					valido = true;
				}
				
				

				lugares = acl.obtenerLugaresUsadosEnClase(clase.getIdClase(), fecha);
				if (lugares.equals(clase.getPersonas())) {
					clase.setLugaresDisponibles(claseLlena);
					isClaseLlena = true;
					clase.setFecha(fechaFormateada);
				} else {
					if (lugares.equals("")) {
						lugares = "0";
					}
					clase.setLugaresDisponibles(lugares + " / " + clase.getPersonas());
					clase.setFecha(fechaFormateada);
					isClaseLlena = false;
				}
				clase.setClaseLlena(isClaseLlena);
				if (idUsuario != 0) {
					// Buscando si esta el usuario en clase
					estaEnClase = acl.estaEnClase(clase.getIdClase(), idUsuario, fecha);
					if (estaEnClase.getLugar() != null) {
						clase.setLugar(estaEnClase.getLugar());
						clase.setAsistencia("true");
						valido = true;
					} else {
						clase.setLugar("0");
						clase.setAsistencia("false");
					}

					//Buscando si esta en lista espera
					ListaEspera le = lp.obtenerByClaseByUsuarioAndfecha(clase.getIdClase(), idUsuario, clase.getDia());
					if(le != null){
						clase.setListaEspera(true);
					}else{
						clase.setListaEspera(false);
					}
				}
				
				if (valido) {
					clasesUs.add(clase);
				}

				clase.setFueraDeHorario(fueraDeHorario);
				

			}
		
		
		} catch (ParseException e) {
		log.error("Excepción capturada", e);
		}
		
		if (idUsuario != 0) {
			return clasesUs;
		} else {
			return clases;
		}
		
		
	}

	public String obtenerUsuarioDeClase(int idUsuario, int idClase, String fecha,int sociedad) throws SQLException {

		Respuesta resp = new Respuesta();
		AsistenciaAClaseLogica acl = new AsistenciaAClaseLogica();
		ClaseLogica cl = new ClaseLogica();
		Clase clase = acl.estaEnClaseEnFecha(fecha, idUsuario);
		if (clase != null) {
			// Si esta en clase
			resp.setEstatus("1");
			Mensaje m = MensajeLogica.obtenerMensaje("CLASES-USUARIO-EN-CLASE-1", "ES");
			String mensaje = m.getMensaje().replace("P1", clase.getNombre()).replace("P2", Utilidades.fechaEnLetra(fecha));
			resp.setMensaje(mensaje);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("CLASES-USUARIO-EN-CLASE-1", "ES",resp);
		} else {
			// No esta en clase
			resp.setEstatus("0");
			Mensaje m = MensajeLogica.obtenerMensaje("CLASES-USUARIO-EN-CLASE-0", "ES");
			//Obtener clase
			clase = cl.obtenerClase("id_clase", String.valueOf(idClase),sociedad,true);
			String mensaje = m.getMensaje().replace("P1", clase.getNombre()).replace("P2", Utilidades.fechaEnLetra(fecha));
			resp.setMensaje(mensaje);
			return MensajeLogica.obtenerMensajeCompletoConRespuesta("CLASES-USUARIO-EN-CLASE-0", "ES",resp);
		}
	}

	public String insertar(Clase clase) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO clase(nombre,hora_inicio,hora_fin,horario,personas,profesor,estatus,dia,sociedad) VALUES(?,?,?,?,?,?,?,?,?);";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, clase.getNombre());
			st.setString(2, clase.getHoraInicio());
			st.setString(3, clase.getHoraFin());
			st.setString(4, clase.getHorario());
			st.setString(5, clase.getPersonas());
			st.setString(6, clase.getProfesor());
			st.setString(7, "1");
			st.setString(8, clase.getDia());
			st.setInt(9,clase.getSociedad());
			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-CREAR", "ES");
		return m.getMensaje();
	}

	public String actualizar(Clase clase) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE clase " + "SET nombre = ?, hora_inicio = ?, hora_fin = ?, horario = ?, personas = ?, profesor = ?, estatus = ?, sociedad = ? ,dia = ? " + "WHERE id_clase = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setString(1, clase.getNombre());
			st.setString(2, clase.getHoraInicio());
			st.setString(3, clase.getHoraFin());
			st.setString(4, clase.getHorario());
			st.setString(5, clase.getPersonas());
			st.setString(6, clase.getProfesor());
			st.setString(7, clase.getEstatus());
			st.setInt(8, clase.getSociedad());
			st.setString(9, clase.getDia());
			st.setInt(10, clase.getIdClase());

			st.executeUpdate();
			conn.commit();

		} catch (SQLException e) {
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ACTUALIZAR", "ES");
		return m.getMensaje();
	}

	public String eliminar(int idClase) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM clase WHERE id_clase = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);

			st.execute();
			conn.commit();

		} catch (SQLException e) {
			// e.printStackTrace();
			throw new SQLException(e);
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}

		Mensaje m = MensajeLogica.obtenerMensaje("CLASES-ELIMINAR", "ES");
		return m.getMensaje();
	}

	public boolean validarDuplicidadClase(String nombre, String horaInicio, String horaFin,String dia,String horario,int soc) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		int count = 0;
		boolean salida = false;

		querySql = "SELECT id_clase FROM clase WHERE nombre = ? and hora_inicio = ? and hora_fin = ? and dia=? and horario = ? and sociedad= ?";

		// log.info("query:"+querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, nombre);
			st.setString(2, horaInicio);
			st.setString(3, horaFin);
			st.setString(4, dia);
			st.setString(5, horario);
			st.setInt(6, soc);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				count++;
			}

			if (count > 0) {
				salida = true;
			}

			

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
				rs = null;
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					;
				}
				st = null;
			}
			if (conn != null && !conn.isClosed()) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		log.info("Clase duplicada:" + salida);

		return salida;
	}

}
