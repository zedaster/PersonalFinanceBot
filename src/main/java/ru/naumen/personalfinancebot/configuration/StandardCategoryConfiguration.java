package ru.naumen.personalfinancebot.configuration;

import org.yaml.snakeyaml.Yaml;
import ru.naumen.personalfinancebot.model.Category;
import ru.naumen.personalfinancebot.model.CategoryType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Класс, который работает с конфигурацией для стандартных категорий
 */
public class StandardCategoryConfiguration {
    /**
     * Путь к ресурсу с конфигурацией
     */
    private static final String CONFIG_RESOURCE_PATH = "standard_categories.yaml";

    /**
     * Хранит список категорий
     */
    private final List<Category> categories;

    public StandardCategoryConfiguration() {
        this.categories = parseCategories();
    }

    /**
     * Возвращает неизменяемый список стандартных категорий
     */
    public List<Category> getStandardCategories() {
        return Collections.unmodifiableList(this.categories);
    }

    /**
     * Парсит стандартные категории из файла
     */
    private List<Category> parseCategories() {
        Map<String, Object> categoryTypesMap;

        try (InputStream inputStream = getConfigInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlContent = yaml.load(inputStream);
            categoryTypesMap = (Map<String, Object>) yamlContent.get("standard_categories");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> expenseCategoryNames = (List<String>) categoryTypesMap.get("expense");
        List<String> incomeCategoryNames = (List<String>) categoryTypesMap.get("income");
        List<Category> expenseCategories = getCategoryList(CategoryType.EXPENSE, expenseCategoryNames);
        List<Category> incomeCategories = getCategoryList(CategoryType.INCOME, incomeCategoryNames);
        return Stream.concat(expenseCategories.stream(), incomeCategories.stream()).toList();
    }

    /**
     * Создает список категорий
     *
     * @param type          Тип категории
     * @param categoryNames Названия для новых категорий
     * @return Список категорий типа type и переданными названиями
     */
    private List<Category> getCategoryList(CategoryType type, List<String> categoryNames) {
        return categoryNames.stream()
                .map(name -> new Category(null, name, type))
                .toList();
    }

    /**
     * Создает и возвращает входной поток с конфигурационным файлом
     */
    private InputStream getConfigInputStream() {
        return this.getClass().getClassLoader().getResourceAsStream(CONFIG_RESOURCE_PATH);
    }


}