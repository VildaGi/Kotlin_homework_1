import kotlin.math.roundToInt

enum class Category(val displayName: String) {
    FOOD("Еда"),
    ELECTRONICS("Электроника"),
    BOOKS("Книги"),
    OTHER("Другое");
}

sealed class OrderStatus {
    data object Created : OrderStatus()

    data class Paid(val transactionId: String) : OrderStatus()

    data class Cancelled(val reason: String) : OrderStatus()

    data object Delivered : OrderStatus()
}

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: Category,
)

data class OrderItem(
    val product: Product,
    val count: Int,
)

data class Customer(
    val name: String,
    val email: String,
    val discount: Double = 0.0,
)

data class Order(
    val id: Int,
    val customer: Customer,
    val items: List<OrderItem>,
    val status: OrderStatus,
)

fun Double.toMoney(): String = "%.2f".format(this)

fun Double.toPercent(): String = "${(this * 100).roundToInt()}%"

fun OrderStatus.describe(): String = when (this) {
    is OrderStatus.Created -> "Создан"
    is OrderStatus.Paid -> "Оплачен, транзакция: $transactionId"
    is OrderStatus.Cancelled -> "Отменён, причина: $reason"
    is OrderStatus.Delivered -> "Доставлен"
}

fun Product.displayName(): String = when (category) {
    Category.FOOD -> "Еда: $name"
    Category.ELECTRONICS -> "Электроника: $name"
    Category.BOOKS -> "Книга: $name"
    Category.OTHER -> name
}

fun OrderItem.lineTotal(): Double = product.price * count

fun Order.subtotal(): Double = items.sumOf { it.lineTotal() }

fun Order.total(): Double = subtotal() * (1 - customer.discount)

fun Order.toReceipt(): String = buildString {
    appendLine("Заказ #$id")
    appendLine("Покупатель: ${customer.name} <${customer.email}>")
    appendLine("Статус: ${status.describe()}")
    appendLine()

    items.forEach { item ->
        appendLine("${item.product.displayName()} × ${item.count} = ${item.lineTotal().toMoney()}")
    }

    appendLine()
    if (customer.discount > 0.0) {
        appendLine("Скидка: ${customer.discount.toPercent()}")
    }
    appendLine("Итого: ${total().toMoney()}")
}

fun List<Order>.toReceipts(): String = buildString {
    this@toReceipts.forEachIndexed { index, order ->
        append(order.toReceipt())
        if (index != lastIndex) appendLine("\n${"-".repeat(30)}\n")
    }
}

fun main() {
    val kotlinBook = Product(
        id = 1,
        name = "Kotlin in Action",
        price = 1_500.0,
        category = Category.BOOKS,
    )

    val coffee = Product(
        id = 2,
        name = "Coffee",
        price = 500.0,
        category = Category.FOOD,
    )

    val headphones = Product(
        id = 3,
        name = "Headphones",
        price = 8_000.0,
        category = Category.ELECTRONICS,
    )

    val customer = Customer(
        name = "Иван",
        email = "ivan@example.com",
        discount = 0.10,
    )

    // я добавил в массив немного данных, и сделал функцию для отображения листа toReceipts(),
    // которая вызывает toReceipt() у каждого элемента.
    val orders = listOf(
        Order(
            id = 1,
            customer = customer,
            items = listOf(
                OrderItem(kotlinBook, count = 2),
                OrderItem(coffee, count = 1),
            ),
            status = OrderStatus.Paid(transactionId = "TX-9000"),
        ),
        Order(
            id = 2,
            customer = customer.copy(name = "Пётр", email = "petr@example.com", discount = 0.0),
            items = listOf(
                OrderItem(headphones, count = 1),
            ),
            status = OrderStatus.Cancelled(reason = "Нет в наличии"),
        ),
        Order(
            id = 3,
            customer = customer.copy(name = "Анна", discount = 0.05),
            items = listOf(
                OrderItem(coffee, count = 3),
            ),
            status = OrderStatus.Created,
        ),
        Order(
            id = 4,
            customer = customer.copy(name = "Ольга", email = "olga@example.com", discount = 0.0),
            items = listOf(
                OrderItem(kotlinBook, count = 1),
                OrderItem(headphones, count = 1),
            ),
            status = OrderStatus.Delivered,
        ),
    )

    println(orders.toReceipts())
}