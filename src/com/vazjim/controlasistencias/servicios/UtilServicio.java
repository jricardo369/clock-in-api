package com.vazjim.controlasistencias.servicios;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class UtilServicio {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String test() {

		return "API funcionando correctamente";

		
	}
	
	

}