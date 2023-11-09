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
    private final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Создает SessionFactory в Hibernate на основе файла hibernate.cfg.xml
     * Он необходим для открытия сессий в Hibernate
     */
    private SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            return new Configuration()
                    .configure()
                    .buildSessionFactory();
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
