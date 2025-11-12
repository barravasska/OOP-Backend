package services

import models.Categories
import models.Category
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object CategoryService {
    suspend fun getAllCategories(): List<Category> {
        return newSuspendedTransaction(context = Dispatchers.IO) {
            Categories.selectAll()
                .map { row ->
                    Category(
                        id = row[Categories.id].value,
                        name = row[Categories.name],
                        slug = row[Categories.slug]
                    )
                }
        }
    }
}
