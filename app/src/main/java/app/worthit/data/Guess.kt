package app.worthit.data

/** Guesses a friendly emoji + category from whatever the user types. Pure delight, zero setup. */
object Guess {
    data class G(val emoji: String, val category: Category)

    private val table: List<Pair<List<String>, G>> = listOf(
        listOf("piano", "synth", "keyboard piano") to G("🎹", Category.Hobbies),
        listOf("guitar", "ukulele", "bass") to G("🎸", Category.Hobbies),
        listOf("drum") to G("🥁", Category.Hobbies),
        listOf("violin", "cello") to G("🎻", Category.Hobbies),
        listOf("lego") to G("🧱", Category.Hobbies),
        listOf("paint", "canvas", "sketch") to G("🎨", Category.Hobbies),
        listOf("camera", "lens", "gopro") to G("📷", Category.Electronics),
        listOf("phone", "iphone", "pixel", "galaxy", "oneplus") to G("📱", Category.Electronics),
        listOf("laptop", "macbook", "computer", "pc") to G("💻", Category.Electronics),
        listOf("keyboard") to G("⌨️", Category.Electronics),
        listOf("headphone", "headphones", "earbuds", "airpods", "headset", "speaker") to G("🎧", Category.Electronics),
        listOf("watch", "smartwatch", "fitbit") to G("⌚", Category.Electronics),
        listOf("tv", "television", "monitor", "projector") to G("📺", Category.Electronics),
        listOf("drone") to G("🚁", Category.Electronics),
        listOf("tablet", "ipad") to G("📱", Category.Electronics),
        listOf("kindle", "book", "books", "course", "class", "lesson", "udemy") to G("📚", Category.Education),
        listOf("console", "playstation", "ps5", "xbox", "switch", "game", "games") to G("🎮", Category.Entertainment),
        listOf("netflix", "spotify", "movie", "cinema") to G("🎬", Category.Entertainment),
        listOf("shoe", "shoes", "sneaker", "sneakers", "boots", "heels") to G("👟", Category.Fashion),
        listOf("jacket", "coat", "hoodie", "sweater") to G("🧥", Category.Fashion),
        listOf("dress", "saree", "lehenga", "kurta") to G("👗", Category.Fashion),
        listOf("shirt", "tshirt", "jeans", "clothes", "trousers") to G("👕", Category.Fashion),
        listOf("bag", "backpack", "handbag", "wallet") to G("👜", Category.Fashion),
        listOf("sunglasses", "glasses") to G("🕶️", Category.Fashion),
        listOf("ring", "necklace", "jewellery", "jewelry", "earrings") to G("💍", Category.Fashion),
        listOf("perfume", "fragrance", "cologne") to G("🌸", Category.Fashion),
        listOf("trip", "flight", "holiday", "vacation", "travel", "goa", "hotel", "getaway", "weekend") to G("✈️", Category.Travel),
        listOf("concert", "ticket", "tickets", "festival", "show", "gig") to G("🎟️", Category.Experiences),
        listOf("spa", "massage") to G("💆", Category.Experiences),
        listOf("coffee", "latte", "espresso", "cappuccino") to G("☕", Category.Food),
        listOf("dinner", "lunch", "restaurant", "food", "pizza", "burger", "sushi", "biryani", "brunch") to G("🍜", Category.Food),
        listOf("bike", "cycle", "bicycle", "scooter") to G("🚲", Category.Fitness),
        listOf("gym", "yoga", "dumbbell", "dumbbells", "treadmill", "protein") to G("🏋️", Category.Fitness),
        listOf("sofa", "couch", "chair", "desk", "bed", "mattress", "lamp", "table") to G("🛋️", Category.Home),
        listOf("fryer", "blender", "mixer", "oven", "microwave", "fridge", "vacuum", "machine") to G("🏠", Category.Home),
        listOf("plant", "plants") to G("🪴", Category.Home),
        listOf("car") to G("🚗", Category.Other),
        listOf("gift", "present") to G("🎁", Category.Other),
        listOf("dog", "cat", "pet") to G("🐶", Category.Other),
    )

    fun from(name: String): G {
        val words = name.lowercase().split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return G("🛍️", Category.Other)
        for ((keys, g) in table) {
            for (k in keys) {
                val hit = if (k.contains(' ')) name.lowercase().contains(k)
                else words.any { w -> w == k || w == k + "s" || (k.length >= 5 && w.startsWith(k)) }
                if (hit) return g
            }
        }
        return G("🛍️", Category.Other)
    }
}
