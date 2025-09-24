package com.vazjim.controlasistencias.logica;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.AsistenciaAClase;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.Configuracion;
import com.vazjim.controlasistencias.modelo.Inscripcion;
import com.vazjim.controlasistencias.modelo.InscripcionUs;
import com.vazjim.controlasistencias.modelo.LugaresClase;
import com.vazjim.controlasistencias.modelo.LugaresClaseSeccionado;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.modelo.Usuario;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class AsistenciaAClaseLogica {

	private static Logger log = Logger.getLogger(AsistenciaAClaseLogica.class);

	public AsistenciaAClase obtenerAsistenciaAClaseProfesor(int idClase, String fecha, int idEntrenador, int sociedad)
			throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		int c = 0;
		Clase clase = null;
		Usuario usuario = null;
		AsistenciaAClase asistenciaClase = new AsistenciaAClase();
		ClaseLogica cl = new ClaseLogica();
		List<Usuario> usuarios = new ArrayList<>();
		String querySql = "";
		String horario = "";

		querySql = "SELECT u.id_usuario,u.usuario,u.nombre,c.id_clase,c.nombre,p.nombre as profesor,c.horario,c.hora_inicio,c.hora_fin,u.sexo,c.personas,c.estatus "
				+ "FROM asistencia_a_clase  a " + "JOIN usuario u ON u.id_usuario = a.id_usuario "
				+ "JOIN clase c ON c.id_clase = a.id_clase " + "JOIN usuario p ON p.id_usuario = c.profesor "
				+ "WHERE a.id_clase = ? and a.fecha = ?  AND profesor = ? ORDER BY u.nombre;";

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setString(2, fecha);
			st.setInt(3, idEntrenador);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				if (c == 0) {
					clase = new Clase();
					clase.setIdClase(rs.getInt(4));
					clase.setNombre(rs.getString(5));
					clase.setProfesor(rs.getString(6));
					clase.setHorario(rs.getString(7));
					clase.setHoraInicio(rs.getString(8));
					clase.setHoraFin(rs.getString(9));
					clase.setPersonas(rs.getString(11));
					clase.setEstatus(rs.getString(12));

					if (clase.getHorario().equals("M")) {
						horario = "am";
					} else {
						horario = "pm";
					}

					clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a "
							+ clase.getHoraFin() + " " + horario);

					asistenciaClase.setFecha(fecha);
					asistenciaClase.setClase(clase);

				}

				usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setUsuario(rs.getString(2));
				usuario.setNombre(rs.getString(3));
				usuario.setSexo(rs.getString(10));
				usuario.setIniciales(Utilidades.inicialesNombre(usuario.getNombre()));

				usuarios.add(usuario);
			}

			// Obtener personas de clase
			Clase cbd = cl.obtenerClase("id_clase", String.valueOf(idClase), sociedad,true);
			int numPersonasPermitidas = Integer.valueOf(cbd.getPersonas());

			// int lugaresDisp = numPersonasPermitidas - usuarios.size();
			int lugaresUsados = usuarios.size();

			asistenciaClase.setLugares("Lugares " + lugaresUsados + " / " + numPersonasPermitidas);
			asistenciaClase.setUsuarios(usuarios);

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

		return asistenciaClase;

	}

	public AsistenciaAClase obtenerAsistenciaAClase(int idClase, String fecha, int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		int c = 0;
		Clase clase = null;
		Usuario usuario = null;
		AsistenciaAClase asistenciaClase = new AsistenciaAClase();
		//ClaseLogica cl = new ClaseLogica();
		List<Usuario> usuarios = new ArrayList<>();
		String querySql = "";
		String horario = "";
		String personas = "";

		querySql = "SELECT u.id_usuario,u.usuario,u.nombre,c.id_clase,c.nombre,c.profesor,c.horario,c.hora_inicio,c.hora_fin,u.sexo,c.personas,c.estatus,a.lugar,a.asistio "
				+ "FROM asistencia_a_clase  a " + "JOIN usuario u ON u.id_usuario = a.id_usuario "
				+ "JOIN clase c ON c.id_clase = a.id_clase";

		String where = "", orderby = " ORDER BY cast(lugar as unsigned) ASC ";

		if (idClase != 0) {
			if (!where.equals(""))
				where += " AND ";
			where += " a.id_clase = '" + idClase + "'  ";
		}

		if (fecha != null && !"".equals(fecha)) {
			if (!where.equals(""))
				where += " AND ";
			where += " a.fecha = '" + fecha + "'  ";
		}

		if (!where.equals("")) {
			where = " WHERE " + where;
			querySql += where;
		}

		querySql += orderby;
		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				if (c == 0) {
					clase = new Clase();
					clase.setIdClase(rs.getInt(4));
					clase.setNombre(rs.getString(5));
					clase.setProfesor(rs.getString(6));
					clase.setHorario(rs.getString(7));
					clase.setHoraInicio(rs.getString(8));
					clase.setHoraFin(rs.getString(9));
					clase.setPersonas(rs.getString(11));
					personas = rs.getString(11);
					clase.setEstatus(rs.getString(12));

					if (clase.getHorario().equals("M")) {
						horario = "am";
					} else {
						horario = "pm";
					}

					clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a "
							+ clase.getHoraFin() + " " + horario);

					asistenciaClase.setFecha(fecha);
					asistenciaClase.setClase(clase);

				}	

				usuario = new Usuario();
				usuario.setIdUsuario(rs.getInt(1));
				usuario.setUsuario(rs.getString(2));
				usuario.setNombre(rs.getString(3));
				usuario.setSexo(rs.getString(10));
				usuario.setIniciales(Utilidades.inicialesNombre(rs.getString(3)));
				usuario.setLugar(rs.getInt(13));
				usuario.setAsistio(rs.getString(14));

				usuarios.add(usuario);
			}

			// Obtener personas de clase
			//Clase cbd = cl.obtenerClase("id_clase", String.valueOf(idClase), sociedad);
			//int numPersonasPermitidas = Integer.valueOf(cbd.getPersonas());

			int lugaresDisp = usuarios.size();

			asistenciaClase.setLugares("Lugares " + lugaresDisp + " / " + personas);
			asistenciaClase.setUsuarios(usuarios);

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

		return asistenciaClase;
	}
	
	

	public String obtenerLugaresUsadosEnClase(int idClase, String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String personas = "";
		String querySql = "";

		querySql = "SELECT count(c.id_clase) as resta " + "FROM clase c "
				+ "JOIN asistencia_a_clase ac ON ac.id_clase = c.id_clase " + "WHERE c.id_clase = ? AND ac.fecha = ? "
				+ "GROUP BY ac.fecha";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setString(2, fecha);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				personas = rs.getString(1);
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

		return personas;
	}

	public int obtenerNumeroDePersonasDeClase(int idClase, String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		int count = 0;

		querySql = "SELECT count(id_clase) FROM asistencia_a_clase  a WHERE a.fecha = ? AND a.id_clase = ? ;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idClase);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				count = rs.getInt(1);

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
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
				conn = null;
			}

		}

		return count;
	}

	public Clase estaEnClase(int idClase, int idUsuario, String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		// int count = 0;
		Clase salida = null;

		querySql = "SELECT count(id_clase),lugar FROM asistencia_a_clase  a WHERE a.fecha = ? AND a.id_clase = ? AND a.id_usuario = ? ;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idClase);
			st.setInt(3, idUsuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				salida = new Clase();
				salida.setLugar(rs.getString(2));

			}

			/*
			 * if (count > 0) { salida = true; }
			 */

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

		// log.info("Usuario esta en clase?" + salida);
		return salida;
	}

	public Clase estaEnClaseEnFecha(String fecha, int idUsuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		Clase salida = null;

		querySql = "SELECT a.id_clase, c.nombre FROM asistencia_a_clase  a JOIN clase c on a.id_clase = c.id_clase "
				+ "WHERE a.fecha = ? AND a.id_usuario = ?;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idUsuario);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				salida = new Clase();
				salida.setIdClase(rs.getInt(1));
				salida.setNombre(rs.getString(2));
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

		return salida;
	}
	
	public boolean lugarOcupado(String fecha, String lugar,int idClase) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		boolean salida = false;

		querySql = "SELECT a.id_usuario,a.id_clase, c.nombre FROM asistencia_a_clase  a JOIN clase c on a.id_clase = c.id_clase "
				+"WHERE a.fecha = ? AND a.lugar = ? AND a.id_clase = ?;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setString(2, lugar);
			st.setInt(3, idClase);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				
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

		return salida;
	}

	public String estaEnClasePorFechaUsuarioClase(String fecha, int idUsuario, int idClase) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		String salida = "";

		querySql = "SELECT a.id_clase, c.nombre FROM asistencia_a_clase  a JOIN clase c on a.id_clase = c.id_clase "
				+ "WHERE a.fecha = ? AND a.id_usuario = ? and a.id_clase = ?;";

		// log.info(querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setString(1, fecha);
			st.setInt(2, idUsuario);
			st.setInt(3, idClase);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				salida = rs.getString(2);
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

		log.info("Esta el usuario en clase?" + salida);
		return salida;
	}

	public String agregarUsuarioAClase(int idUsuario, int idClase, String fecha, String lugar) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO asistencia_a_clase(id_usuario,id_clase,fecha,lugar) " + "VALUES(?,?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, idUsuario);
			st.setInt(2, idClase);
			st.setString(3, fecha);
			st.setString(4, lugar);

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

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-AGREGAR", "ES");
		return m.getMensaje();
	}

	public String eliminarUsuarioAClase(int idUsuario, int idClase, String fecha) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase WHERE id_clase = ? AND id_usuario = ? AND fecha = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setInt(2, idUsuario);
			st.setString(3, fecha);

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

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-ELIMINAR", "ES");
		return m.getMensaje();
	}

	public String eliminarUsuarioAClaseVariasFechas(int idClase, List<String> fechas) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "DELETE FROM asistencia_a_clase WHERE id_clase = ? AND fecha = ?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			for (String string : fechas) {
				st.setInt(1, idClase);
				st.setString(2, string);
				st.addBatch();
			}

			st.executeBatch();
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

		Mensaje m = MensajeLogica.obtenerMensaje("ASISCLASE-ELIMINAR", "ES");
		return m.getMensaje();

	}

	public boolean horarioFueraDeClase(String hora, String horaClase, String horario) {

		boolean salida = false;

		log.info("Hora Atual:" + hora);
		log.info("Hora Clase:" + horaClase);

		if ("V".equals(horario)) {
			horaClase = Utilidades.convertirHoraA24Hrs(horaClase);
		}

		horaClase = Utilidades.completaCeros(horaClase, 2);

		log.info("Hora Ingresada despues formato:" + hora);
		log.info("Hora Clase despues de formato:" + horaClase);

		log.info(hora + " es mayor que " + horaClase);

		if (Integer.valueOf(hora) >= Integer.valueOf(horaClase)) {
			salida = true;
		}

		log.info("Clase fuera de horario?:" + salida);

		return salida;
	}
	
	public boolean horarioFueraDeClaseConValNumHora(String hora, String horaClase, String horario) {

		boolean salida = false;
		int hm = 0;
		int horaInt = 0;
		log.info("----- Validar horario fuera de clase");
		horaInt = Integer.valueOf(hora)-1;
		log.info("Hora Atual:" + horaInt);
		log.info("Hora Clase:" + horaClase);

		if ("V".equals(horario)) {
			horaClase = Utilidades.convertirHoraA24Hrs(horaClase);
		}

		horaClase = Utilidades.completaCeros(horaClase, 2);

		log.info("Hora clase despues de formato:" + horaClase);
		log.info(horaInt + " es mayor que " + horaClase);
		
		if (horaInt >= Integer.valueOf(horaClase)) {
			salida = true;
		}
		
		hm = Integer.valueOf(horaClase)-5;
		log.info("horaMin " + hm);
		log.info(horaInt + " esta entre " + hm +" y  " + horaClase + "?");
		if (horaInt >= hm && horaInt <= Integer.valueOf(horaClase)) {
			log.info("El horario esta entre "+hm+" y "+Integer.valueOf(horaClase));
			salida = true;
		}else{
			log.info("No esta en el rango de horario permitido");
		}

		log.info("Clase fuera de horario?:" + salida);
		log.info("----- Fin horario fuera de clase");

		return salida;
	}

	public boolean horarioParaFalta(String hora, String horaClase, String horario) {

		boolean salida = false;

		log.info("Hora Actual:" + hora);
		log.info("Hora Clase:" + horaClase);
//		Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("HORAS-PERMITIDAS-CANCELACION");
//		int horasPerVal = Integer.valueOf(conf.getValorAbajo());
		int horasPerVal = 3;
		
		
		
		int hc = Integer.valueOf(horaClase) - horasPerVal;
		log.info("Hora Clase menos horario permitido:" + hc);
		int horasP = Integer.valueOf(horaClase) - hc;
		log.info("Horas antes de clase:" + horasP);
		
		horaClase = "" + hc;

		if ("V".equals(horario)) {
			horaClase = Utilidades.convertirHoraA24Hrs(horaClase);
			hora = Utilidades.convertirHoraA24Hrs(hora);
		}

		horaClase = Utilidades.completaCeros(horaClase, 2);

		log.info("Hora Ingresada despues formato:" + hora);
		log.info("Hora Clase despues de formato:" + horaClase);

		log.info(hora + " es mayor que " + horaClase);

		if (Integer.valueOf(hora) >= Integer.valueOf(horaClase)) {
			salida = true;
		}

		log.info("Clase fuera de horario?:" + salida);

		return salida;
	}
	
	public boolean horarioParaValFalta(String hora, String horaClase, String horario) {

		boolean salida = false;

		//int horaA = Integer.valueOf(hora)-1;
		//hora = String.valueOf(horaA);
		log.info("Hora Actual:" + hora);
		log.info("Hora Clase:" + horaClase);
		Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("HORAS-PERMITIDAS-CANCELACION");
		int horasPerVal = Integer.valueOf(conf.getValorAbajo());

		if ("V".equals(horario)) {
			horaClase = Utilidades.convertirHoraA24Hrs(horaClase);
			hora = Utilidades.convertirHoraA24Hrs(hora);
		}

		horaClase = Utilidades.completaCeros(horaClase, 2);

		log.info("Hora Ingresada despues formato:" + hora);
		log.info("Hora Clase despues de formato:" + horaClase);

		int horasP24 = Integer.valueOf(horaClase) -  Integer.valueOf(hora);
		log.info("Horas antes de clase en 24h:" + horasP24);

		log.info("horasP24" + horasP24 +" es menor o igual a "+horasPerVal);
		if (horasP24 <= horasPerVal) {
			salida = true;
		}

		log.info("Clase fuera de horario?:" + salida);

		return salida;
	}
	
	public boolean horarioParaValFaltaV2(String hora, String horaClase, String horario) {

		boolean salida = false;

		int horaNum = Integer.valueOf(hora);
		//hora = String.valueOf(horaA);
		log.info("Hora Actual:" + hora);
		log.info("Hora Clase:" + horaClase);
		Configuracion conf = ConfiguracionLogica.obtenerConIdentificador("HORAS-PERMITIDAS-CANCELACION");
		int horasPerVal = Integer.valueOf(conf.getValorAbajo());

		if ("V".equals(horario)) {
			horaClase = Utilidades.convertirHoraA24Hrs(horaClase);
			if (horaNum > 12) {
				hora = Utilidades.convertirHoraA24Hrs(hora);
			}
		}

		horaClase = Utilidades.completaCeros(horaClase, 2);

		log.info("Hora Ingresada despues formato:" + hora);
		log.info("Hora Clase despues de formato:" + horaClase);

		int horasP24 = Integer.valueOf(horaClase) -  Integer.valueOf(hora);
		log.info("Horas antes de clase en 24h:" + horasP24);

		log.info("horasP24: " + horasP24 +" es menor o igual a: "+horasPerVal);
		if (horasP24 <= horasPerVal) {
			salida = true;
		}

		log.info("Clase fuera de horario?:" + salida);

		return salida;
	}
	
	public static void main(String args[]) throws SQLException {
		AsistenciaAClaseLogica al = new AsistenciaAClaseLogica();

			boolean horarioFueraPorFalta = false;
			String hora = "";
			//hora = "5";
			try {
				hora = Utilidades.generarFecha(false, true, false, "", 0, "2024-04-19").substring(0, 2);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			String horaInicio = "21";
			String horarioClase = "V";
//		
			horarioFueraPorFalta = al.horarioParaValFalta(hora, horaInicio, horarioClase);		
			
			System.out.println("Horario fuera de clase:"+horarioFueraPorFalta);		
		
		
	}

	public List<String> obtenerLugaresUsados(int idClase, String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		List<String> lugares = new ArrayList<>();
		String lugar = "";
		String querySql = "";

		querySql = "SELECT LUGAR FROM ASISTENCIA_A_CLASE " + "WHERE LUGAR IS NOT NULL AND LUGAR NOT IN('0') "
				+ "AND ID_CLASE = ? AND FECHA = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setString(2, fecha);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				lugar = rs.getString(1);
				lugares.add(lugar);
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

		return lugares;
	}
	
	public int obtenerNumLugaresUsados(int idClase, String fecha) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		int lugares = 0;
		String querySql = "";

		querySql = "SELECT count(*) FROM ASISTENCIA_A_CLASE WHERE LUGAR IS NOT NULL AND LUGAR NOT IN('0') "
				+ "AND ID_CLASE = ? AND FECHA = ?";

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setString(2, fecha);
			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				lugares = rs.getInt(1);
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

		return lugares;
	}

	public InscripcionUs validoInscripcionUsuario(int idUsuario,boolean eliminar) {
		InscripcionUs InscripcionUs = null;

		InscripcionLogica insLogica = new InscripcionLogica();
		String mensaje = "";
		Inscripcion ins;
		try {
			ins = insLogica.obtenerUlimaInscripcionDeUsuario(idUsuario);

			if (ins != null) {
				InscripcionUs = new InscripcionUs();
				InscripcionUs.setInscripcion(ins);
				if (!ins.getTipoInscripcionDesc().equals("Mensual")) {
					
					log.info("Clases de paquete:" + ins.getClases() + " - Clases restantes:" + ins.getClases_restantes()
							+ " Fecha corte:" + ins.getFechaCorte());
					Date fechaActual = new Date();
					Date fechaCorte = Utilidades.cadenaToDate(ins.getFechaCorte());
					if (fechaActual.after(fechaCorte)) {
						log.info("La fecha permitida para usar clases se termino");
						mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FECHAPERMITIDAPAQCLASES", "ES");
					}
					InscripcionUs.setEsPorClases(true);
					if (!eliminar) {
						if (ins.getClases_restantes() == 0) {
							log.info("Ya no tiene clases");
							mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SINCLASES", "ES");
						}
					}
					
					
				} else {
					
					InscripcionUs.setEsPorClases(false);
					log.info("Fecha corte:" + ins.getFechaCorte());
					Date fechaActual = new Date();
					Date fechaCorte = Utilidades.cadenaToDate(ins.getFechaCorte());
					if (fechaActual.after(fechaCorte)) {
						log.info(MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FINMENSUALIDAD", "ES"));
						mensaje = MensajeLogica.obtenerMensajeCompleto("ASISCLASE-FINMENSUALIDAD", "ES");
					}
					
				}
				InscripcionUs.setMensaje(mensaje);
				
			} else {
				log.info("No tiene inscripcion");
				InscripcionUs = new InscripcionUs();
				mensaje =  MensajeLogica.obtenerMensajeCompleto("ASISCLASE-SININSCRIPCION", "ES");
				InscripcionUs.setMensaje(mensaje);
			}
		} catch (SQLException e) {

			e.printStackTrace();
			InscripcionUs = new InscripcionUs();
			InscripcionUs.setMensaje(e.getMessage());
			return InscripcionUs;
		}
		 return InscripcionUs;
	}
	
	public String obtenerMensajeUsuario(int idUsuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;
		String querySql = "";
		String h = "";
		boolean hoy = false;
		
		StringBuilder sb = new StringBuilder();

		querySql = "SELECT c.nombre,c.hora_inicio,c.hora_fin,c.horario,c.dia FROM ASISTENCIA_A_CLASE  a "+
				   "JOIN clase c ON c.id_clase = a.id_clase "+ 
				   "WHERE id_usuario = ? and fecha between ? and ?";

		// log.info(querySql);

		try {
			
			String fi = Utilidades.generarFecha(true, false, false, "", 0, null);
			String ff = Utilidades.generarFecha(true, false, false, "", 1, null);

			st = conn.prepareStatement(querySql);
			
			st.setInt(1, idUsuario);
			st.setString(2, fi);
			st.setString(3, ff);
			rs = st.executeQuery();

			
			
			int i = 0;
			String f = "";
			String ho = "";
			while (rs != null && rs.next()) {
				if (i == 0) {
					sb.append("Recuerda que tienes clase");
				}
				f = rs.getString(5);
				ho = rs.getString(4);
				System.out.println("dia:"+f);
				if (fi.equals(f)) {
					if (ho.equals("M")){
						h = " AM";
					}else{
						h = " PM";
					}
					sb.append(" el día de hoy a las " + rs.getString(2) + h);
					hoy = true;
				}
				if (ff.equals(f)) {
					if (ho.equals("M")){
						h = " AM";
					}else{
						h = " PM";
					}
					if (hoy) {
						sb.append(" y");
					}
					sb.append(" el día de mañana a las " + rs.getString(2) + h);
				}
				i++;
			}
			
			if (sb.length() == 0) {
				sb.append("");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		} catch (ParseException e) {
			e.printStackTrace();
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

		return sb.toString();
	}
	

	public LugaresClaseSeccionado lugaresClase(int idClase, String fecha) {

		LugaresClaseSeccionado lcs = new LugaresClaseSeccionado();
		List<LugaresClase> listLugares = new ArrayList<>();
		List<LugaresClase> listLugares2 = new ArrayList<>();
		List<LugaresClase> listLugares3 = new ArrayList<>();
		List<LugaresClase> listLugares4 = new ArrayList<>();
		List<LugaresClase> listLugares5 = new ArrayList<>();

		try {

			ClaseLogica cl = new ClaseLogica();
			Clase c = cl.obtenerClase("id_clase", "" + idClase, 0,true);
			List<String> lugaresUsados = obtenerLugaresUsados(idClase, fecha);
			System.out.println("personas:" + c.getPersonas());
			boolean encontro = false;
			for (int i = 1; i < Integer.valueOf(c.getPersonas()) + 4; i++) {
				String l = "" + i;
				//System.out.println("Lugar:" + l);
				for (String s : lugaresUsados) {
					System.out.println("s:" + s + " es igual a" + ":" + l);
					if (s.equals(l)) {
						System.out.println("Se econtro lugar:" + i);
						encontro = true;
						break;
					}
				}

				LugaresClase lg = new LugaresClase();
				lg.setNumero(i);
				if (encontro) {
					lg.setSeleccionado(true);
				} else {
					lg.setSeleccionado(false);
				}
				if (i <= 4) {
					listLugares.add(lg);
				}

				if ((i >= 5) && (i <= 8)) {
					listLugares2.add(lg);
				}

				if ((i >= 9) && (i <= 12)) {
					listLugares3.add(lg);
				}

				if ((i >= 13) && (i <= 16)) {
					listLugares4.add(lg);
				}

				if ((i >= 17) && (i <= 21)) {
					listLugares5.add(lg);
				}

				encontro = false;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		lcs.setL1(listLugares);
		lcs.setL2(listLugares2);
		lcs.setL3(listLugares3);
		lcs.setL4(listLugares4);
		Collections.reverse(listLugares5);
		lcs.setL5(listLugares5);

		return lcs;
	}

	public void generarImagen(List<String> lista, int clase, String fecha) {
		/*
		 * List<String> lista = new ArrayList<>(); lista.add("1");
		 * lista.add("2"); lista.add("3"); lista.add("4"); lista.add("5");
		 * lista.add("6"); lista.add("7"); lista.add("8"); lista.add("9");
		 * lista.add("10"); lista.add("11"); lista.add("12");
		 */
		try {
			BufferedImage img = ImageIO.read(new File("/Users/joser.vazquez/Downloads/Lugares.png"));
			Graphics2D g = img.createGraphics();
			int x = 200;
			int y = 450;
			Font font = new Font("Arial", Font.BOLD, 90);
			for (String s : lista) {
				System.out.println("x" + x);
				System.out.println("y:" + y);
				g.setColor(Color.red);
				g.setFont(font);
				// VALOR,=>,||
				if (s.equals("1")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("2")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("3")) {
					g.drawString("x", x, y);
					x = x + 750;
					y = 450;
				}

				///
				if (s.equals("4")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("5")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("6")) {
					g.drawString("x", x, y);
					x = x + 750;
					y = 450;
				}

				///
				if (s.equals("7")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("8")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("9")) {
					g.drawString("x", x, y);
					x = x + 750;
					y = 450;
				}

				///
				if (s.equals("10")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("11")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
				if (s.equals("12")) {
					g.drawString("x", x, y);
					y = y + 600;
				}
			}
			g.dispose();
			ImageIO.write(img, "png", new File("/Users/joser.vazquez/Documents/apache-tomcat-8.0.46/webapps/imgs/lug2"
					+ clase + fecha.replaceAll("-", "") + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public String actualizarAsistencia(String campo, String valor, int idUsuario,int idClase,String fecha) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "UPDATE asistencia_a_clase SET " + campo + " = ? WHERE id_usuario = ? AND id_clase = ? AND fecha=?";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setString(1, valor);
			st.setInt(2, idUsuario);
			st.setInt(3, idClase);
			st.setString(4, fecha);

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
		Mensaje m = MensajeLogica.obtenerMensaje("US-ACTUALIZADO", "ES");
		return m.getMensaje();
	}
	
	public Clase obtenerAsistenciaDeUsuario(int idClase, String fecha,int idUsuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		
		PreparedStatement st = null;
		ResultSet rs = null;
		Clase clase = null;

		String querySql = "";
		String horario = "";

		querySql = "SELECT c.id_clase,c.nombre,c.hora_inicio,c.hora_fin,c.horario,u.id_usuario,c.sociedad,a.fecha,a.lugar,a.asistio,u.nombre "
				+ "FROM ASISTENCIA_A_CLASE  a " 
				+ "JOIN clase c ON c.id_clase = a.id_clase "
				+ "JOIN usuario u ON u.id_usuario = c.profesor " 
				+ "WHERE a.id_usuario = ? and fecha = ? and a.id_clase = ?";

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setString(2, fecha);
			st.setInt(3, idClase);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				clase = new Clase();
				clase.setIdClase(rs.getInt(1));
				clase.setNombre(rs.getString(2));
				clase.setHoraInicio(rs.getString(3));
				clase.setHoraFin(rs.getString(4));
				clase.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				clase.setProfesor(rs.getString(6));
				clase.setSociedad(rs.getInt(7));
				clase.setDia(rs.getString(8));

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else {
					horario = "PM";
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);
				clase.setDescCorta(clase.getDia()+" | "+clase.getHoraInicio() +" a "+ clase.getHoraFin() + " " + horario);
				clase.setProfesorNombre(rs.getString(11));
				clase.setLugar(rs.getString(9));
				clase.setAsistio(rs.getString(10));
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

		return clase;
	}
	
	public List<Clase> obtenerAsistenciasUsuario(int idUsuario) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();
		
		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		String horario = "";

		List<Clase> clases = new ArrayList<>();
		Clase clase;

		querySql = "SELECT c.id_clase,c.nombre,c.hora_inicio,c.hora_fin,c.horario,u.id_usuario,c.sociedad,a.fecha,a.lugar,a.asistio,u.nombre "
				+ "FROM ASISTENCIA_A_CLASE  a " 
				+ "JOIN clase c ON c.id_clase = a.id_clase "
				+ "JOIN usuario u ON u.id_usuario = c.profesor " 
				+ "WHERE a.id_usuario = ? and fecha < ? order by fecha desc limit 30";

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idUsuario);
			st.setString(2, Utilidades.fechaActual());

			rs = st.executeQuery();

			while (rs != null && rs.next()) {

				clase = new Clase();
				clase.setIdClase(rs.getInt(1));
				clase.setNombre(rs.getString(2));
				clase.setHoraInicio(rs.getString(3));
				clase.setHoraFin(rs.getString(4));
				clase.setHorario(rs.getString(5) != null ? rs.getString(5) : "");
				clase.setProfesor(rs.getString(6));
				clase.setSociedad(rs.getInt(7));
				clase.setDia(rs.getString(8));

				if (clase.getHorario().equals("M")) {
					horario = "AM";
				} else {
					horario = "PM";
				}

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a " + clase.getHoraFin() + " " + horario);
				clase.setDescCorta(clase.getDia()+" | "+clase.getHoraInicio() +" a "+ clase.getHoraFin() + " " + horario);
				clase.setProfesorNombre(rs.getString(11));
				clase.setLugar(rs.getString(9));
				clase.setAsistio(rs.getString(10));
			    
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

}
