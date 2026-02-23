package com.iut.banque.converter;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import com.iut.banque.interfaces.IDao;
import com.iut.banque.modele.Client;

@Component
public class ClientConverter implements GenericConverter {

	private final IDao dao;

	public ClientConverter(IDao dao) {
		this.dao = dao;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> set = new HashSet<>();
		set.add(new ConvertiblePair(String.class, Client.class));
		set.add(new ConvertiblePair(Client.class, String.class));
		return set;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null)
			return null;
		Class<?> src = (source instanceof String) ? String.class : source.getClass();
		Class<?> tgt = (Class<?>) targetType.getType();
		if (String.class.equals(src) && Client.class.equals(tgt)) {
			return dao.getUserById((String) source);
		}
		if (Client.class.isAssignableFrom(src) && String.class.equals(tgt)) {
			return ((Client) source).getIdentity();
		}
		return null;
	}
}
