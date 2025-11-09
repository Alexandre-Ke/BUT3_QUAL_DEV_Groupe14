// AccountConverter.java
package com.iut.banque.converter;

import java.util.Map;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.util.StrutsTypeConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.opensymphony.xwork2.conversion.TypeConversionException;
import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Compte;

public class AccountConverter extends StrutsTypeConverter {

	// plus de static + pas d’écriture d’un membre static dans un constructeur (S3010)
	private IDao dao;

	public AccountConverter(IDao dao) {
		this.dao = dao;
	}

	public AccountConverter() {
		// no-op : la DAO sera résolue à la volée si nécessaire
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object convertFromString(Map context, String[] values, Class targetType) {
		ensureDao();
		Compte compte = dao.getAccountById(values[0]);
		if (compte == null) {
			throw new TypeConversionException("Impossible de convertir la chaîne : " + values[0]);
		}
		return compte;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public String convertToString(Map context, Object value) {
		Compte compte = (Compte) value;
		return (compte == null) ? null : compte.getNumeroCompte();
	}

	private void ensureDao() {
		if (this.dao == null) {
			ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(
					ServletActionContext.getServletContext());
			this.dao = ctx.getBean(IDao.class); // récupération par type
		}
	}
}
