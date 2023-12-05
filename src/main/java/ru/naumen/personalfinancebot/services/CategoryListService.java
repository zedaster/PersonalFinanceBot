package ru.naumen.personalfinancebot.services;

import ru.naumen.personalfinancebot.messages.Messages;
import ru.naumen.personalfinancebot.models.Category;
import ru.naumen.personalfinancebot.models.CategoryType;
import ru.naumen.personalfinancebot.models.User;
import ru.naumen.personalfinancebot.repositories.category.CategoryRepository;

import java.util.List;

/**
 * Сервис для работы со списком категорий
 *
 * @author Sergey Kazantsev
 */
public class CategoryListService {
    /**
     * Хранилище категорий
     */
    private final CategoryRepository categoryRepository;

    public CategoryListService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Получает текст сообщения для вывода категорий доходов или расходов.
     */
    public String getListContent(User user, CategoryType categoryType) {
        List<Category> typedStandardCategories = categoryRepository.getStandardCategoriesByType(categoryType);
        List<Category> personalCategories = categoryRepository.getUserCategoriesByType(user, categoryType);

        return Messages.LIST_TYPED_CATEGORIES
                .replace("{type}", categoryType.getPluralShowLabel())
                .replace("{standard_list}", formatCategoryList(typedStandardCategories))
                .replace("{personal_list}", formatCategoryList(personalCategories));
    }

    /**
     * Форматирует список категорий в строку, содержащую нумерованный список из названия этих категорий или
     * {@link Messages#EMPTY_LIST_CONTENT}, если список пуст.
     */
    private String formatCategoryList(List<Category> categories) {
        if (categories.isEmpty()) {
            return Messages.EMPTY_LIST_CONTENT + "\n";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            stringBuilder
                    .append(i + 1)
                    .append(". ")
                    .append(categories.get(i).getCategoryName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
