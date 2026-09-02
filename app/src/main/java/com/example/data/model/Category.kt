package com.example.data.model

enum class CategoryType(
    val id: String,
    val emoji: String,
    val labelEn: String,
    val labelAr: String,
    val labelFr: String
) {
    ALL("all", "✨", "All", "الكل", "Tous"),
    COFFEE("coffee", "☕", "Coffee & Cafes", "مقاهي وكافيهات", "Cafés & Coffee"),
    FOOD("food", "🍔", "Food & Dining", "مطاعم ومأكولات", "Restaurants"),
    GAMING("gaming", "🎮", "Gaming & VR", "ألعاب وVR", "Jeux & VR"),
    NATURE("nature", "🌳", "Nature & Parks", "طبيعة وحدائق", "Nature & Parcs"),
    ENTERTAINMENT("entertainment", "🎬", "Entertainment", "ترفيه وسينما", "Divertissement"),
    SPORTS("sports", "🏋️", "Sports & Action", "رياضة وحركة", "Sports & Fitness"),
    SHOPPING("shopping", "🛍️", "Shopping", "تسوق ومتاجر", "Shopping"),
    MUSIC("music", "🎵", "Live Music", "موسيقى وفعاليات", "Musique & Concerts"),
    DATE("date", "❤️", "Romantic & Date", "أجواء رومانسية", "Romantique"),
    FAMILY("family", "👨‍👩‍👧", "Family & Kids", "عائلي وللأطفال", "Famille"),
    TOURISM("tourism", "🏛️", "Culture & Sights", "معالم وسياحة", "Culture & Sights"),
    NIGHTLIFE("nightlife", "🌙", "Nightlife & Lounges", "سهرات وليالي", "Vie Nocturne"),
    RELAXATION("relaxation", "🧘", "Spa & Wellness", "استرخاء وعناية", "Bien-être & Spa")
}

enum class CompanionType(val labelEn: String, val labelAr: String, val labelFr: String, val emoji: String) {
    SOLO("Solo Explorer", "استكشاف فردي", "En Solo", "🚶"),
    COUPLE("Couple / Date", "شخصين / موعد", "En Couple", "💑"),
    FRIENDS("Friends Squad", "مع الأصدقاء", "Entre Amis", "👥"),
    FAMILY("Family & Kids", "العائلة والأطفال", "En Famille", "👨‍👩‍👧‍👦")
}

enum class BudgetLevel(val symbol: String, val labelEn: String, val labelAr: String, val maxAmountUsd: Double) {
    FREE("Free", "مجاني", "Gratuit", 0.0),
    BUDGET("$", "اقتصادي", "Économique", 15.0),
    MODERATE("$$", "متوسط", "Modéré", 35.0),
    EXPENSIVE("$$$", "مميز", "Élevé", 75.0),
    LUXURY("$$$$", "فاخر", "Luxe", 200.0)
}
