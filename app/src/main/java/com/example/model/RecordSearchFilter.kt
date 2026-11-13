package com.example.model

enum class ProductSortOption {
    NAME_ASC,
    PRICE_ASC
}

enum class CustomerSortOption {
    NAME_ASC,
    SPENT_DESC
}

object RecordSearchFilter {

    fun filterProducts(
        products: List<Product>,
        query: String,
        selectedCategory: String = "__ALL__",
        onlyActive: Boolean = false,
        lowStockOnly: Boolean = false,
        sortBy: ProductSortOption = ProductSortOption.NAME_ASC
    ): List<Product> {
        val q = query.trim().lowercase()
        return products
            .filter { prod ->
                val catOk = selectedCategory == "__ALL__" || prod.category == selectedCategory
                val activeOk = !onlyActive || prod.active
                val stockOk = !lowStockOnly || prod.stock <= 5L
                val queryOk = q.isEmpty() ||
                    prod.name.lowercase().contains(q) ||
                    prod.sku.lowercase().contains(q) ||
                    prod.description.lowercase().contains(q) ||
                    prod.category.lowercase().contains(q)
                catOk && activeOk && stockOk && queryOk
            }
            .sortedWith(
                compareBy<Product>(
                    { prod ->
                        if (q.isEmpty()) 0
                        else {
                            val nameLower = prod.name.lowercase()
                            when {
                                nameLower.startsWith(q) -> 0
                                nameLower.contains(q) -> 1
                                else -> 2
                            }
                        }
                    },
                    { prod ->
                        when (sortBy) {
                            ProductSortOption.NAME_ASC -> prod.name.lowercase()
                            ProductSortOption.PRICE_ASC -> ""
                        }
                    },
                    { prod ->
                        when (sortBy) {
                            ProductSortOption.PRICE_ASC -> prod.price
                            ProductSortOption.NAME_ASC -> 0.0
                        }
                    }
                )
            )
    }

    fun filterCustomers(
        customers: List<Customer>,
        query: String,
        segmentFilter: String = "all",
        sortBy: CustomerSortOption = CustomerSortOption.NAME_ASC
    ): List<Customer> {
        val q = query.trim().lowercase()
        return customers
            .filter { cust ->
                val segOk = segmentFilter == "all" || cust.segment == segmentFilter
                val queryOk = q.isEmpty() ||
                    cust.name.lowercase().contains(q) ||
                    cust.phone.lowercase().contains(q) ||
                    cust.email.lowercase().contains(q) ||
                    cust.company.lowercase().contains(q) ||
                    cust.tags.any { it.lowercase().contains(q) }
                segOk && queryOk
            }
            .sortedWith(
                compareBy<Customer>(
                    { cust ->
                        if (q.isEmpty()) 0
                        else {
                            val nameLower = cust.name.lowercase()
                            when {
                                nameLower.startsWith(q) -> 0
                                nameLower.contains(q) -> 1
                                else -> 2
                            }
                        }
                    },
                    { cust ->
                        when (sortBy) {
                            CustomerSortOption.NAME_ASC -> cust.name.lowercase()
                            CustomerSortOption.SPENT_DESC -> ""
                        }
                    },
                    { cust ->
                        when (sortBy) {
                            CustomerSortOption.SPENT_DESC -> -cust.totalSpent
                            CustomerSortOption.NAME_ASC -> 0.0
                        }
                    }
                )
            )
    }
}
