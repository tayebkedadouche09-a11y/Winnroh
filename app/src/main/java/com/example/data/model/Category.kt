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
    CINEMA("cinema", "🍿", "Cinema & Movies", "سينما وأفلام", "Cinéma"),
    NATURE("nature", "🌳", "Nature & Parks", "طبيعة وحدائق", "Nature & Parcs"),
    ENTERTAINMENT("entertainment", "🎬", "Entertainment", "ترفيه وعروض", "Divertissement"),
    SPORTS("sports", "🏋️", "Sports & Fitness", "رياضة ولياقة", "Sports & Fitness"),
    SHOPPING("shopping", "🛍️", "Shopping & Malls", "تسوق ومولات", "Shopping & Centres"),
    TOURISM("tourism", "🏛️", "Tourism & Landmarks", "سياحة ومعالم", "Tourisme & Monuments"),
    CULTURE("culture", "🕌", "Culture & History", "ثقافة وتاريخ", "Culture & Histoire"),
    ART("art", "🎨", "Art & Galleries", "فنون ومعارض", "Art & Galeries"),
    MUSIC("music", "🎵", "Live Music", "موسيقى وحفلات", "Musique & Concerts"),
    NIGHTLIFE("nightlife", "🌙", "Nightlife & Lounges", "سهرات وأمسيات", "Vie Nocturne"),
    ADVENTURE("adventure", "🧗", "Adventure & Action", "مغامرات وتشويق", "Aventure"),
    DATE("date", "❤️", "Romantic & Date", "أجواء رومانسية", "Romantique"),
    FAMILY("family", "👨‍👩‍👧", "Family & Kids", "عائلي وللأطفال", "Famille"),
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
