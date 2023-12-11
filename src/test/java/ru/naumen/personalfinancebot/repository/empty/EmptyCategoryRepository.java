package ru.naumen.personalfinancebot.repository.empty;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Хранилище с категориями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyCategoryRepository implements CategoryRepository {
    @Override
    public List<Category> getUserCategoriesByType(Session session, User user, CategoryType type) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public List<Category> getStandardCategoriesByType(Session session, CategoryType type) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Category createUserCategory(Session session, User user, CategoryType type, String categoryName) throws CreatingExistingUserCategoryException, CreatingExistingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Category createStandardCategory(Session session, CategoryType type, String categoryName) throws CreatingExistingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public void removeCategoryById(Session session, Long id) throws RemovingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public void removeUserCategoryByName(Session session, User user, CategoryType type, String categoryName) throws RemovingNonExistentCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Optional<Category> getCategoryByName(Session session, User user, CategoryType type, String categoryName) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }
}
