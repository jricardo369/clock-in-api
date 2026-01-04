package com.vazjim.controlasistencias.utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Propiedades {
	private static final Properties configProperties = new Properties();
	private static final Properties errorProperties = new Properties();

	static {
		try (InputStream configStream = Propiedades.class.getClassLoader().getResourceAsStream("config.properties")) {
			if (configStream != null) {
				configProperties.load(configStream);
			} else {
				System.err.println("No se encontró config.properties en el classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (InputStream errorStream = Propiedades.class.getClassLoader().getResourceAsStream("msjerrores.properties")) {
			if (errorStream != null) {
				errorProperties.load(errorStream);
			} else {
				System.err.println("No se encontró msjerrores.properties en el classpath");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Properties getConfigProperties() {
		return configProperties;
	}

	public static Properties getErrorProperties() {
		return errorProperties;
	}

	// Métodos antiguos para compatibilidad, puedes eliminarlos si ya no los usas
	public Properties getProperties() {
		return getConfigProperties();
	}

	public Properties getPropertiesErrores() {
		return getErrorProperties();
	}

	public static void main(String args[]) {
		Properties pp = getConfigProperties();
		String v = pp.getProperty("version");
		System.out.println(v);
	}
}
