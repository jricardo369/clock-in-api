package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.Clase;
import com.vazjim.controlasistencias.modelo.Mensaje;
import com.vazjim.controlasistencias.utilidades.Utilidades;

public class AsistenciaMultaLogica {

	public String agregarUsuarioAAsistenciaMulta(int idUsuario, int idClase, String fecha, String lugar)
			throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO asistencia_multa(id_usuario,id_clase,fecha,lugar,sociedad) " + "VALUES(?,?,?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, idUsuario);
			st.setInt(2, idClase);
			st.setString(3, fecha);
			st.setString(4, lugar);
			st.setInt(5, 1);

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

	public List<Clase> obtenerAsistenciaMultas(int idUsuario)
			throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";
		String horario = "";

		List<Clase> clases = new ArrayList<>();
		Clase clase;

		querySql = "SELECT c.id_clase,c.nombre,c.hora_inicio,c.hora_fin,c.horario,u.id_usuario,c.sociedad,a.fecha,a.lugar,u.nombre "
				+ "FROM asistencia_multa a " + "JOIN clase c ON c.id_clase = a.id_clase "
				+ "JOIN usuario u ON u.id_usuario = c.profesor "
				+ "WHERE a.id_usuario = ? and fecha <= ? order by fecha desc limit 30";

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

				clase.setDescripcionHorario("Clase de " + clase.getHoraInicio() + " " + horario + " a "
						+ clase.getHoraFin() + " " + horario);
				clase.setDescCorta(
						clase.getDia() + " | " + clase.getHoraInicio() + " a " + clase.getHoraFin() + " " + horario);
				clase.setProfesorNombre(rs.getString(10));
				clase.setLugar(rs.getString(9));
				clase.setAsistio("C");

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

		return clases;

	}

}
