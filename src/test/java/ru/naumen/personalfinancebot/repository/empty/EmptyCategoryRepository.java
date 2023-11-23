package ru.naumen.personalfinancebot.repository.empty;

import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;

import java.util.List;
import java.util.Optional;

/**
 * Хранилище с категориями, которое при любой операции бросит ${@link RuntimeException}
 */
public class EmptyCategoryRepository implements CategoryRepository {
    @Override
    public List<Category> getUserCategoriesByType(User user, CategoryType type) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public List<Category> getStandardCategoriesByType(CategoryType type) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Category createUserCategory(User user, CategoryType type, String categoryName) throws CreatingExistingUserCategoryException, CreatingExistingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Category createStandardCategory(CategoryType type, String categoryName) throws CreatingExistingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public void removeCategoryById(Long id) throws RemovingStandardCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public void removeUserCategoryByName(User user, CategoryType type, String categoryName) throws RemovingNonExistentCategoryException {
        throw new RuntimeException("Category repository shouldn't be touched");
    }

    @Override
    public Optional<Category> getCategoryByName(User user, CategoryType type, String categoryName) {
        throw new RuntimeException("Category repository shouldn't be touched");
    }
}
