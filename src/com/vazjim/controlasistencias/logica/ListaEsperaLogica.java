package com.vazjim.controlasistencias.logica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vazjim.controlasistencias.conexion.Conexion;
import com.vazjim.controlasistencias.modelo.ListaEspera;
import com.vazjim.controlasistencias.modelo.Mensaje;

public class ListaEsperaLogica {

	private static Logger log = Logger.getLogger(ListaEsperaLogica.class);

	public List<ListaEspera> obtener(String columna, String valor, int sociedad) throws SQLException {

		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		List<ListaEspera> lista = new ArrayList<>();
		ListaEspera obj;

		querySql = "SELECT id_lista_esp,id_clase,fecha,id_usuario FROM lista_espera";

		String where = "";

		if (sociedad != 0 && !"".equals(sociedad)) {
			if (!where.equals(""))
				where += " AND ";
			where += " a.sociedad = " + sociedad + " ";
		}

		if (columna != null && !"".equals(columna)) {
			if (!where.equals(""))
				where += " AND ";
			where += " " + columna + " = '" + valor + "'";
		}

		if (!where.equals("")) {
			if (!where.equals(""))
				where = " WHERE " + where + " ORDER BY id_lista_esp DESC";
			querySql += where;
		}

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				obj = new ListaEspera();
				obj.setIdListaEspera(rs.getInt(1));
				obj.setIdClase(rs.getInt(2));
				obj.setFecha(rs.getString(3));
				obj.setIdUsuario(rs.getInt(4));
				lista.add(obj);
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

		log.info("Se obtuvieron asuetos:" + lista.size());
		return lista;
	}

	public List<ListaEspera> listaEsperaDeClase(int idClase) {
		List<ListaEspera> salida = null;
		try {
			salida = obtener("id_clase", "", 0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return salida;
	}

	public ListaEspera obtenerByClaseByUsuarioAndfecha(int idClase, int idUsuario, String fecha) {
		Connection conn = Conexion.getConnectionDbPool();

		PreparedStatement st = null;
		ResultSet rs = null;

		String querySql = "";

		ListaEspera obj = null;

		querySql = "SELECT id_lista_esp,id_clase,fecha,id_usuario FROM lista_espera where id_clase = ? and id_usuario = ? and fecha = ?";

		System.out.println("query:" + querySql);

		try {

			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setInt(2, idUsuario);
			st.setString(3, fecha);

			rs = st.executeQuery();

			while (rs != null && rs.next()) {
				obj = new ListaEspera();
				obj.setIdListaEspera(rs.getInt(1));
				obj.setIdClase(rs.getInt(2));
				obj.setFecha(rs.getString(3));
				obj.setIdUsuario(rs.getInt(4));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				throw new SQLException();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
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
			try {
				if (conn != null && !conn.isClosed()) {
					try {
						conn.close();
					} catch (SQLException e) {
						;
					}
					conn = null;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return obj;
	}

	public String insertar(int idClase, String fecha, int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		querySql = "INSERT INTO lista_espera(id_clase,fecha,id_usuario) VALUES(?,?,?)";

		try {
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);

			st.setInt(1, idClase);
			st.setString(2, fecha);
			st.setInt(3, idUsuario);
			st.execute();
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
		Mensaje m = MensajeLogica.obtenerMensaje("LISTA-ESP-INSERT", "ES");
		return m.getMensaje();
	}

	public String eliminar(int idClase, String fecha, int idUsuario) throws SQLException {

		PreparedStatement st = null;
		String querySql;
		Connection conn = Conexion.getConnectionDbPool();

		try {

			log.info("elminará por fecha");
			querySql = "DELETE FROM lista_espera WHERE id_clase = ? AND fecha = ? AND id_usuario = ?";
			conn.setAutoCommit(false);
			st = conn.prepareStatement(querySql);
			st.setInt(1, idClase);
			st.setString(2, fecha);
			st.setInt(3, idUsuario);

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

		Mensaje m = MensajeLogica.obtenerMensaje("LISTA-ESP-DEL", "ES");
		return m.getMensaje();
	}

}
