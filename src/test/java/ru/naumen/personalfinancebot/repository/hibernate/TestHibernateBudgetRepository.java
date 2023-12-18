package ru.naumen.personalfinancebot.repository.hibernate;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Budget;
import ru.naumen.personalfinancebot.repository.budget.HibernateBudgetRepository;

public class TestHibernateBudgetRepository extends HibernateBudgetRepository {

    private final TestHibernateTransaction testHibernateTransactions;

    public TestHibernateBudgetRepository() {
        this.testHibernateTransactions = new TestHibernateTransaction(Budget.class);
    }

    public void removeAll(Session session) {
        this.testHibernateTransactions.removeAll(session);
    }
}
