package ru.naumen.personalfinancebot.configuration;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Класс для работы с конфигурацией Hibernate
 */
public class HibernateConfiguration {
    /**
     * SessionFactory в Hibernate
     * Он необходим для открытия сессий в Hibernate
     */
    private final SessionFactory sessionFactory;

    /**
     * Конфигурирует Hibernate на основе файла hibernate.cfg.xml
     */
    public HibernateConfiguration() {
        Configuration configuration = new Configuration().configure();
        sessionFactory = buildSessionFactory(configuration);
    }

    /**
     * Конфигурирует Hibernate с использованием указанных параметров, затем параметров из hibernate.cfg.xml
     */
    public HibernateConfiguration(String dbUrl, String dbUsername, String dbPassword) {
        Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.url", dbUrl)
                .setProperty("hibernate.connection.username", dbUsername)
                .setProperty("hibernate.connection.password", dbPassword)
                .configure();
        sessionFactory = buildSessionFactory(configuration);
    }

    /**
     * Создает SessionFactory в Hibernate
     * Он необходим для открытия сессий в Hibernate
     */
    private SessionFactory buildSessionFactory(Configuration configuration) {
        try {
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be helpful for debugging
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Возвращает SessionFactory для Hibernate
     * Он необходим для открытия сессий в Hibernate
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
