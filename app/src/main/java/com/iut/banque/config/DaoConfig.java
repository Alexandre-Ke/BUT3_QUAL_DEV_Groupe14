package com.iut.banque.config;

import com.iut.banque.dao.DaoHibernate;
import com.iut.banque.interfaces.IDao;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig {

    @Bean
    public IDao dao(EntityManagerFactory emf) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);

        DaoHibernate dao = new DaoHibernate();
        dao.setSessionFactory(sessionFactory);

        return dao;
    }
}