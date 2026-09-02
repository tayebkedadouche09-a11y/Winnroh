package com.example.data.local

import com.example.data.model.*

object SampleData {
    val initialPlaces = listOf(
        Place(
            id = "p_1",
            name = "Neon Nexus VR & Retro Arcade",
            arabicName = "نيون نكسس لألعاب الواقع الافتراضي والآركيد",
            category = CategoryType.GAMING,
            description = "A futuristic cyberpunk-inspired gaming lounge featuring state-of-the-art VR arenas, Japanese rhythm arcade cabinets, multiplayer sim rigs, and neon-lit bubble tea mocktails.",
            arabicDescription = "صالة ألعاب مستقبلية بتصميم سايبربانك تضم أحدث حلبات الواقع الافتراضي، ألعاب الآركيد اليابانية، ومشروبات منعشة وسط إضاءات نيون ساحرة.",
            rating = 4.9,
            reviewCount = 342,
            priceLevel = BudgetLevel.MODERATE,
            estimatedCostUsd = 22.0,
            address = "74 Horizon Boulevard, Tech District",
            distanceKm = 1.4,
            isOpenNow = true,
            openingHours = "14:00 - 02:00",
            isIndoor = true,
            weatherSuitability = "indoor_priority",
            suitableCompanions = listOf("SOLO", "FRIENDS", "COUPLE"),
            averageDurationMinutes = 120,
            coverImageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("VR Multiplayer", "Retro Pinball", "Chill Lounge", "Snacks & Drinks", "Free Wi-Fi"),
            latitude = 36.7538,
            longitude = 3.0588,
            whyMatchReason = "Top trending gaming spot with incredible social multiplayer vibes and cozy evening hours.",
            isTrending = true,
            isNew = false
        ),
        Place(
            id = "p_2",
            name = "Velvet Bloom Specialty Roasters",
            arabicName = "محمصة ومقهى فلفيت بلوم المختص",
            category = CategoryType.COFFEE,
            description = "Artisanal single-origin pour-overs, pistachio Spanish lattes, warm cardamom brioche, and a sunlit courtyard surrounded by cascading jasmine vines.",
            arabicDescription = "قهوة مقطرة بمحاصيل فاخرة، سبانش لاتيه بالفستق، بريوش الهيل الطازج في فناء مشمس محاط بأزهار الياسمين.",
            rating = 4.8,
            reviewCount = 512,
            priceLevel = BudgetLevel.BUDGET,
            estimatedCostUsd = 9.0,
            address = "18 Jasmine Lane, Old Town Quarter",
            distanceKm = 0.8,
            isOpenNow = true,
            openingHours = "07:00 - 23:00",
            isIndoor = true,
            weatherSuitability = "all_weather",
            suitableCompanions = listOf("SOLO", "COUPLE", "FRIENDS"),
            averageDurationMinutes = 60,
            coverImageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Specialty Beans", "Courtyard Patio", "Quiet Workspaces", "Artisan Bakery", "Power Outlets"),
            latitude = 36.7592,
            longitude = 3.0612,
            whyMatchReason = "Rated #1 for tranquil atmosphere, rich aromatic roasts, and comfortable reading nooks.",
            isTrending = true,
            isNew = false
        ),
        Place(
            id = "p_3",
            name = "Skyline Panorama Terrace & Grill",
            arabicName = "تراس ومطعم سكاي لاين البانورامي",
            category = CategoryType.FOOD,
            description = "360-degree hilltop city skyline view offering smoked wagyu smash burgers, truffle parmesan fries, artisan mocktails, and live sunset acoustic sessions.",
            arabicDescription = "إطلالة بانورامية ساحرة 360 درجة على أفق المدينة، يقدم برجر واغيو مدخن، بطاطس الكمأة، وجلسات غنائية هادئة وقت الغروب.",
            rating = 4.9,
            reviewCount = 680,
            priceLevel = BudgetLevel.MODERATE,
            estimatedCostUsd = 28.0,
            address = "Summit Ridge 12, Upper Heights",
            distanceKm = 3.2,
            isOpenNow = true,
            openingHours = "16:00 - 01:00",
            isIndoor = false,
            weatherSuitability = "outdoor_priority",
            suitableCompanions = listOf("COUPLE", "FRIENDS", "FAMILY"),
            averageDurationMinutes = 90,
            coverImageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1544025162-d76694265947?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Sunset View", "Outdoor Terrace", "Live Music", "Free Parking", "Table Reservations"),
            latitude = 36.7620,
            longitude = 3.0450,
            whyMatchReason = "Best golden hour sunset view in the city with exceptional grilled gastronomy.",
            isTrending = true,
            isNew = true
        ),
        Place(
            id = "p_4",
            name = "Pinecrest Cliffside Trail & Lookout",
            arabicName = "مسار ومطل غابات الصنوبر الجبلية",
            category = CategoryType.NATURE,
            description = "A scenic forested trail leading to a breathtaking sea cliff vantage point. Ideal for golden hour walks, meditative breeze, and picnic gatherings.",
            arabicDescription = "مسار طبيعي رائع وسط غابات الصنوبر ينتهي بمطل جبلي ساحلي خلاب. مثالي لنزهات وقت الغروب والمشي في الطبيعة.",
            rating = 4.7,
            reviewCount = 219,
            priceLevel = BudgetLevel.FREE,
            estimatedCostUsd = 0.0,
            address = "Pine Ridge Reserve, Coastal Park",
            distanceKm = 4.5,
            isOpenNow = true,
            openingHours = "06:00 - 20:00",
            isIndoor = false,
            weatherSuitability = "outdoor_priority",
            suitableCompanions = listOf("SOLO", "COUPLE", "FRIENDS", "FAMILY"),
            averageDurationMinutes = 110,
            coverImageUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Scenic Trail", "Free Entry", "Picnic Spots", "Bird Watching", "Sunset Point"),
            latitude = 36.7710,
            longitude = 3.0320,
            whyMatchReason = "Zero-cost outdoor retreat with fresh mountain air and sweeping horizon views.",
            isTrending = false,
            isNew = false
        ),
        Place(
            id = "p_5",
            name = "The Starlight Independent Cinema & Lounge",
            arabicName = "سينما ولاونج ضوء النجوم المستقلة",
            category = CategoryType.ENTERTAINMENT,
            description = "Boutique arthouse and blockbuster cinema with plush velvet reclining couches, artisanal truffle popcorn, and post-screening director talks.",
            arabicDescription = "سينما بوتيك فاخرة بمقاعد مخملية مريحة، فشار بالكمأة وزبدة مميزة، وعروض لأفلام حصرية وتجارب سينمائية غامرة.",
            rating = 4.8,
            reviewCount = 184,
            priceLevel = BudgetLevel.MODERATE,
            estimatedCostUsd = 18.0,
            address = "55 Art District Promenade",
            distanceKm = 2.1,
            isOpenNow = true,
            openingHours = "13:00 - 00:30",
            isIndoor = true,
            weatherSuitability = "indoor_priority",
            suitableCompanions = listOf("SOLO", "COUPLE", "FRIENDS"),
            averageDurationMinutes = 150,
            coverImageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
                "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Recliner Seats", "Dolby Atmos", "Gourmet Snacks", "Indie Films", "Late Shows"),
            latitude = 36.7555,
            longitude = 3.0550,
            whyMatchReason = "Cozy rainy-day escape with premium sound and unmatched comfort.",
            isTrending = true,
            isNew = false
        ),
        Place(
            id = "p_6",
            name = "Apex Padel Club & Rooftop Courts",
            arabicName = "نادي أبكس للبادل والملاعب البانورامية",
            category = CategoryType.SPORTS,
            description = "Modern panoramic glass-walled padel courts with tournament lighting, gear rental, recovery smoothie bar, and social round-robin tournaments.",
            arabicDescription = "ملاعب بادل زجاجية حديثة على السطح بإضاءة أولمبية، متجر تأجير مضارب، وبار سموذي صحي وجلسات رياضية حماسية.",
            rating = 4.9,
            reviewCount = 275,
            priceLevel = BudgetLevel.MODERATE,
            estimatedCostUsd = 25.0,
            address = "88 Olympic Way, Sports City",
            distanceKm = 3.8,
            isOpenNow = true,
            openingHours = "08:00 - 01:00",
            isIndoor = false,
            weatherSuitability = "all_weather",
            suitableCompanions = listOf("FRIENDS", "COUPLE"),
            averageDurationMinutes = 90,
            coverImageUrl = "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Padel Courts", "Racket Rental", "Smoothie Bar", "Coaching Sessions", "Showers & Lockers"),
            latitude = 36.7480,
            longitude = 3.0700,
            whyMatchReason = "High-energy competitive sport perfect for squad challenges.",
            isTrending = true,
            isNew = true
        ),
        Place(
            id = "p_7",
            name = "Old Fortress Heritage Museum & Walk",
            arabicName = "متحف القلعة الأثرية ومسار التراث العريق",
            category = CategoryType.TOURISM,
            description = "Historic 16th-century fortress featuring interactive historical exhibits, ancient ramparts with cannon viewpoints, and tranquil stone gardens.",
            arabicDescription = "قلعة تاريخية عريقة تعود للقرن السادس عشر مع معارض تفاعلية وإطلالة بحرية مهيبة وحدائق حجرية هادئة.",
            rating = 4.6,
            reviewCount = 420,
            priceLevel = BudgetLevel.BUDGET,
            estimatedCostUsd = 6.0,
            address = "1 Citadel Hill, Historic District",
            distanceKm = 1.9,
            isOpenNow = true,
            openingHours = "09:00 - 18:00",
            isIndoor = true,
            weatherSuitability = "all_weather",
            suitableCompanions = listOf("SOLO", "FAMILY", "COUPLE", "FRIENDS"),
            averageDurationMinutes = 100,
            coverImageUrl = "https://images.unsplash.com/photo-1513584684374-8bab748fbf90?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1513584684374-8bab748fbf90?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Audio Guides", "Historic Artifacts", "Sea Views", "Souvenir Boutique", "Guided Tours"),
            latitude = 36.7650,
            longitude = 3.0640,
            whyMatchReason = "Rich cultural immersion with picturesque architecture for photos.",
            isTrending = false,
            isNew = false
        ),
        Place(
            id = "p_8",
            name = "Moonlight Secret Escape Rooms",
            arabicName = "غرف الهروب والغموض ضوء القمر",
            category = CategoryType.ENTERTAINMENT,
            description = "Immersive multi-room puzzle adventures with live actors, Hollywood movie props, high-tech secret doors, and mystery storylines.",
            arabicDescription = "مغامرات غرف هروب وألغاز غامرة مع ممثلين أحياء وديكورات سينمائية وأبواب سرية إلكترونية مشوقة للمجموعات.",
            rating = 4.9,
            reviewCount = 310,
            priceLevel = BudgetLevel.EXPENSIVE,
            estimatedCostUsd = 35.0,
            address = "102 Mystery Court, Downtown",
            distanceKm = 1.7,
            isOpenNow = true,
            openingHours = "15:00 - 01:30",
            isIndoor = true,
            weatherSuitability = "indoor_priority",
            suitableCompanions = listOf("FRIENDS", "FAMILY", "COUPLE"),
            averageDurationMinutes = 75,
            coverImageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=900&auto=format&fit=crop&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80"
            ),
            features = listOf("Live Actors", "Immersive Props", "Team Challenges", "Photo Souvenir", "Private Rooms"),
            latitude = 36.7570,
            longitude = 3.0560,
            whyMatchReason = "Thrilling collaborative puzzle experience that bonds friends together.",
            isTrending = true,
            isNew = false
        )
    )

    val initialBadges = listOf(
        Badge(
            id = "b_first_discovery",
            nameEn = "First Discovery",
            nameAr = "أول استكشاف",
            descEn = "Discovered your very first spot on WAYGO",
            descAr = "استكشفت أول مكان لك عبر تطبيق WAYGO",
            iconEmoji = "🌟",
            category = BadgeCategory.EXPLORATION,
            xpReward = 50,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 15,
            progressCurrent = 1,
            progressMax = 1
        ),
        Badge(
            id = "b_coffee_hunter",
            nameEn = "Coffee Hunter",
            nameAr = "صياد القهوة",
            descEn = "Visited 5 specialty artisanal coffee shops",
            descAr = "زرت 5 مقاهي مختصة مميزة",
            iconEmoji = "☕",
            category = BadgeCategory.FOOD_AND_DRINK,
            xpReward = 150,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 5,
            progressCurrent = 5,
            progressMax = 5
        ),
        Badge(
            id = "b_night_owl",
            nameEn = "Night Explorer",
            nameAr = "مستكشف الليل",
            descEn = "Discovered 3 spots past midnight",
            descAr = "استكشفت 3 أماكن بعد منتصف الليل",
            iconEmoji = "🌙",
            category = BadgeCategory.NIGHT_LIFE,
            xpReward = 100,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L * 2,
            progressCurrent = 3,
            progressMax = 3
        ),
        Badge(
            id = "b_nature_lover",
            nameEn = "Nature Lover",
            nameAr = "عاشق الطبيعة",
            descEn = "Explored 3 outdoor parks or hiking trails",
            descAr = "استكشفت 3 حدائق أو مسارات طبيعية",
            iconEmoji = "🌲",
            category = BadgeCategory.EXPLORATION,
            xpReward = 120,
            isUnlocked = false,
            progressCurrent = 2,
            progressMax = 3
        ),
        Badge(
            id = "b_squad_leader",
            nameEn = "Squad Master",
            nameAr = "قائد الشلة",
            descEn = "Planned 5 group outings with friends",
            descAr = "خططت لـ 5 طلعات جماعية مع الأصدقاء",
            iconEmoji = "👥",
            category = BadgeCategory.SOCIAL,
            xpReward = 200,
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis() - 86400000L,
            progressCurrent = 5,
            progressMax = 5
        ),
        Badge(
            id = "b_surprise_gambler",
            nameEn = "Surprise Champion",
            nameAr = "بطل المفاجآت",
            descEn = "Rolled Surprise Me and completed the adventure",
            descAr = "رميت نرد فاجئني وأكملت المغامرة المقترحة",
            iconEmoji = "🎲",
            category = BadgeCategory.SPECIAL,
            xpReward = 250,
            isUnlocked = false,
            progressCurrent = 3,
            progressMax = 5
        )
    )

    val initialReviews = listOf(
        Review(
            id = "r_1",
            placeId = "p_1",
            userName = "Karim Z.",
            userAvatarUrl = "",
            rating = 5.0,
            text = "Absolute top tier VR multiplayer! We booked the 4-player cyber arena and spent 2 hours laughing and sweating. The staff explains everything so nicely.",
            timestamp = System.currentTimeMillis() - 86400000L * 2,
            helpfulLikesCount = 24,
            isVerifiedVisit = true
        ),
        Review(
            id = "r_2",
            placeId = "p_1",
            userName = "Sara M.",
            userAvatarUrl = "",
            rating = 4.8,
            text = "Great vibe for friends! The bubble tea and retro arcade cabinets brought back so much nostalgia. Definitely coming back this weekend.",
            timestamp = System.currentTimeMillis() - 86400000L * 4,
            helpfulLikesCount = 17,
            isVerifiedVisit = true
        ),
        Review(
            id = "r_3",
            placeId = "p_2",
            userName = "Yacine B.",
            userAvatarUrl = "",
            rating = 5.0,
            text = "Best Ethiopian pour-over in the city without question. The courtyard has incredible tranquil energy in the mornings.",
            timestamp = System.currentTimeMillis() - 86400000L * 1,
            helpfulLikesCount = 31,
            isVerifiedVisit = true
        )
    )

    val initialCollections = listOf(
        UserCollection(
            id = "col_1",
            title = "Weekend Vibe Gems",
            description = "Top picks for high-energy weekend nights with friends ⚡",
            coverEmoji = "🔥",
            isPublic = true,
            itemsCount = 4
        ),
        UserCollection(
            id = "col_2",
            title = "Cozy Coffee & Study Spots",
            description = "Quiet atmospheres, great beans and strong Wi-Fi ☕📖",
            coverEmoji = "☕",
            isPublic = true,
            itemsCount = 3
        ),
        UserCollection(
            id = "col_3",
            title = "Sunset & Date Night Escapes",
            description = "Breathtaking views, gentle lighting and romantic menus ❤️🌅",
            coverEmoji = "✨",
            isPublic = true,
            itemsCount = 2
        )
    )

    val initialNotifications = listOf(
        NotificationItem(
            id = "n_1",
            title = "Level 12 Unlocked! 🚀",
            body = "Congratulations Ahmed! You unlocked 'Traveler' rank and +100 bonus discovery points.",
            iconEmoji = "🎉",
            timestamp = System.currentTimeMillis() - 3600000L * 3,
            isRead = false
        ),
        NotificationItem(
            id = "n_2",
            title = "Trending near you ☕",
            body = "Velvet Bloom Specialty Roasters just launched their seasonal cold brews.",
            iconEmoji = "✨",
            timestamp = System.currentTimeMillis() - 86400000L,
            isRead = true
        )
    )
}
