package com.vazjim.controlasistencias.conexion;



import java.sql.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;



public class Conexion {

	public static Connection getConnectionDbPool() {

		InitialContext initCtx = null;
		Connection conn = null;

		try {
			
			initCtx = new InitialContext();
			listContext(initCtx, "");

			DataSource ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/control_asistencias-lua");

			//DataSource ds = (DataSource) initCtx.lookup("control_asistencias-lua");

			if (ds != null) {
				conn = ds.getConnection();
			}

		} catch (SQLException| NamingException e) {
			throw new RuntimeException(e);
		}

		return conn;
	}

	private static void listContext(Context ctx, String prefix) {
    try {
        NamingEnumeration<NameClassPair> list = ctx.list("");
        while (list.hasMore()) {
            NameClassPair nc = list.next();
            System.out.println("---------------------------JNDI: " + prefix + nc.getName());
            // Si es un subcontexto, recursividad (opcional)
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
