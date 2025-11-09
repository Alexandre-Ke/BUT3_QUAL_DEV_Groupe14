package com.iut.banque.converter;

import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Client;
import com.opensymphony.xwork2.conversion.TypeConversionException;

public class ClientConverter extends StrutsTypeConverter {

	private static final Logger LOG = LoggerFactory.getLogger(ClientConverter.class);

	/** Injection par constructeur, plus de champ static. */
	private final IDao dao;

	public ClientConverter(IDao dao) {
		this.dao = dao;
		LOG.debug("Création du convertisseur de client");
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object convertFromString(Map context, String[] values, Class toClass) {
		if (values == null || values.length == 0 || values[0] == null) {
			throw new TypeConversionException("Valeur de client manquante.");
		}
		final Client client = (Client) dao.getUserById(values[0]);
		if (client == null) {
			throw new TypeConversionException("Impossible de convertir la chaîne : " + values[0]);
		}
		return client;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public String convertToString(Map context, Object value) {
		if (!(value instanceof Client)) {
			return null;
		}
		final Client client = (Client) value;
		return client.getIdentity();
	}
}
