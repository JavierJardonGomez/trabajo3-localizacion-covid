package com.practica.ems.covid;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import com.practica.excecption.EmsDuplicateLocationException;
import com.practica.excecption.EmsDuplicatePersonException;
import com.practica.excecption.EmsInvalidNumberOfDataException;
import com.practica.excecption.EmsInvalidTypeException;
import com.practica.excecption.EmsLocalizationNotFoundException;
import com.practica.excecption.EmsPersonNotFoundException;
import com.practica.genericas.*;
import com.practica.lista.ListaContactos;

import static com.practica.genericas.Utils.parsearFecha;
import static java.lang.Float.parseFloat;

public class ContactosCovid {
	private Poblacion poblacion;
	private Localizacion localizacion;
	private ListaContactos listaContactos;

	public ContactosCovid() {
		this.poblacion = new Poblacion();
		this.localizacion = new Localizacion();
		this.listaContactos = new ListaContactos();
	}

	public Poblacion getPoblacion() {
		return poblacion;
	}

	public void setPoblacion(Poblacion poblacion) {
		this.poblacion = poblacion;
	}

	public Localizacion getLocalizacion() {
		return localizacion;
	}

	public void setLocalizacion(Localizacion localizacion) {
		this.localizacion = localizacion;
	}
	
	

	public ListaContactos getListaContactos() {
		return listaContactos;
	}

	public void setListaContactos(ListaContactos listaContactos) {
		this.listaContactos = listaContactos;
	}

	public void loadData(String data, boolean reset) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException,
			EmsDuplicatePersonException, EmsDuplicateLocationException {
		// borro información anterior
		if (reset) {
			this.poblacion = new Poblacion();
			this.localizacion = new Localizacion();
			this.listaContactos = new ListaContactos();
		}
		String datas[] = dividirEntrada(data);
		checkDatas(datas);
	}

	public void loadDataFile(String fichero, boolean reset) {
		Lector lector = new Lector();
		String datas[] = null, data = null;
		loadDataFile(fichero, reset,lector, datas, data);
		
	}

	@SuppressWarnings("resource")
	public void loadDataFile(String fichero, boolean reset, Lector lector, String datas[], String data ) {
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			lector.setArchivo(new File(fichero));
			lector.setFr(new FileReader(lector.getArchivo()));
			lector.setBr(new BufferedReader(lector.getFr()));
			if (reset) {
				this.poblacion = new Poblacion();
				this.localizacion = new Localizacion();
				this.listaContactos = new ListaContactos();
			} 
			/**
			 * Lectura del fichero	línea a línea. Compruebo que cada línea 
			 * tiene el tipo PERSONA o LOCALIZACION y cargo la línea de datos en la 
			 * lista correspondiente. Sino viene ninguno de esos tipos lanzo una excepción
			 */
			while ((data = lector.getBr().readLine()) != null) {
				datas = dividirEntrada(data.trim());
				checkDatas(datas);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si to do va bien como si salta
			// una excepcion.
			try {
				if (null != lector.getFr()) {
					lector.getFr().close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	private void checkDatas(String[] datas) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException, EmsDuplicatePersonException, EmsDuplicateLocationException {
		for (String linea : datas) {
			String datos[] = this.dividirLineaData(linea);
			if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
				throw new EmsInvalidTypeException();
			}
			if (datos[0].equals("PERSONA")) {
				if (datos.length != Constantes.MAX_DATOS_PERSONA) {
					throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
				}
				this.poblacion.addPersona(this.crearPersona(datos));
			}
			if (datos[0].equals("LOCALIZACION")) {
				if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
					throw new EmsInvalidNumberOfDataException(
							"El número de datos para LOCALIZACION es menor de 6" );
				}
				PosicionPersona pp = this.crearPosicionPersona(datos);
				this.localizacion.addLocalizacion(pp);
				this.listaContactos.insertarNodoTemporal(pp);
			}
		}
	}


	public int findPersona(String documento) throws EmsPersonNotFoundException {
		int pos;
		try {
			pos = this.poblacion.findPersona(documento);
			return pos;
		} catch (EmsPersonNotFoundException e) {
			throw new EmsPersonNotFoundException();
		}
	}

	public int findLocalizacion(String documento, String fecha, String hora) throws EmsLocalizationNotFoundException {

		int pos;
		try {
			pos = localizacion.findLocalizacion(documento, fecha, hora);
			return pos;
		} catch (EmsLocalizationNotFoundException e) {
			throw new EmsLocalizationNotFoundException();
		}
	}

	public List<PosicionPersona> localizacionPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0;
		List<PosicionPersona> lista = new ArrayList<PosicionPersona>();
		Iterator<PosicionPersona> it = this.localizacion.getLista().iterator();
		while (it.hasNext()) {
			PosicionPersona pp = it.next();
			if (pp.getDocumento().equals(documento)) {
				cont++;
				lista.add(pp);
			}
		}
		if (cont == 0)
			throw new EmsPersonNotFoundException();
		else
			return lista;
	}

	public boolean delPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0, pos = -1;
		Iterator<Persona> it = this.poblacion.getLista().iterator();
		while (it.hasNext()) {
			Persona persona = it.next();
			if (persona.getDocumento().equals(documento)) {
				pos = cont;
			}
			cont++;
		}
		if (pos == -1) {
			throw new EmsPersonNotFoundException();
		}
		this.poblacion.getLista().remove(pos);
		return false;
	}

	private String[] dividirEntrada(String input) {
		String cadenas[] = input.split("\\n");
		return cadenas;
	}

	private String[] dividirLineaData(String data) {
		String cadenas[] = data.split("\\;");
		return cadenas;
	}

	private Persona crearPersona(String[] data) {
		Persona persona = new Persona();
		Map<Integer, String> personaNueva= new HashMap<>();

		for (int i = 1; i < Constantes.MAX_DATOS_PERSONA; i++) {
			personaNueva.put(i,data[i]);
		}
			persona.setDocumento(personaNueva.get(1));
			persona.setNombre(personaNueva.get(2));
			persona.setApellidos(personaNueva.get(3));
			persona.setEmail(personaNueva.get(4));
			persona.setDireccion(personaNueva.get(5));
			persona.setCp(personaNueva.get(6));
			persona.setFechaNacimiento(parsearFecha(personaNueva.get(7)));
		return persona;
	}

	private PosicionPersona crearPosicionPersona(String[] data) {
		PosicionPersona posicionPersona = new PosicionPersona();
		Map<Integer, String> nuevaPosicionPersona = new HashMap<>();

		for (int i = 1; i < Constantes.MAX_DATOS_LOCALIZACION; i++) {
			nuevaPosicionPersona.put(i, data[i]);
		}
		posicionPersona.setDocumento(nuevaPosicionPersona.get(1));
		posicionPersona.setFechaPosicion(parsearFecha(nuevaPosicionPersona.get(2),nuevaPosicionPersona.get(3)));
		posicionPersona.setCoordenada(new Coordenada(parseFloat(nuevaPosicionPersona.get(4)), parseFloat(nuevaPosicionPersona.get(5))));
		return posicionPersona;
	}
	

	


}
