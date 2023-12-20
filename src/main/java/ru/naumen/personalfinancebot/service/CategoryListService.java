package ru.naumen.personalfinancebot.service;

import org.hibernate.Session;
import ru.naumen.personalfinancebot.message.Message;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;
import ru.naumen.personalfinancebot.model.User;
import ru.naumen.personalfinancebot.repository.category.CategoryRepository;

import java.util.List;

/**
 * Сервис для работы со списком категорий
 *
 * @author Sergey Kazantsev
 */
public class CategoryListService {
    /**
     * Шаблон для вывода сообщения о доступных пользователю категориях
     */
    private static final String LIST_TYPED_CATEGORIES = """
            Все доступные вам категории %s:
            Стандартные:
            %s
            Персональные:
            %s""";

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
    public String getListContent(Session session, User user, CategoryType categoryType) {
        List<Category> typedStandardCategories = categoryRepository.getStandardCategoriesByType(session, categoryType);
        List<Category> personalCategories = categoryRepository.getUserCategoriesByType(session, user, categoryType);

        return LIST_TYPED_CATEGORIES.formatted(
                categoryType.getPluralShowLabel(),
                formatCategoryList(typedStandardCategories),
                formatCategoryList(personalCategories)
        );
    }

    /**
     * Форматирует список категорий в строку, содержащую нумерованный список из названия этих категорий или
     * {@link Message#EMPTY_LIST_CONTENT}, если список пуст.
     */
    private String formatCategoryList(List<Category> categories) {
        if (categories.isEmpty()) {
            return Message.EMPTY_LIST_CONTENT + "\n";
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
