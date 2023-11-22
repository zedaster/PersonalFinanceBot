package ru.naumen.personalfinancebot.repository;

import org.hibernate.SessionFactory;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.budget.HibernateBudgetRepository;

public class TestHibernateBudgetRepository extends HibernateBudgetRepository {

    private final TestHibernateTransactions testHibernateTransactions;

    public TestHibernateBudgetRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.testHibernateTransactions = new TestHibernateTransactions(User.class, sessionFactory);
    }

    public void removeAll() {
        this.testHibernateTransactions.removeAll();
    }
}
