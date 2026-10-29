package com.example.model

import androidx.compose.ui.graphics.Color

enum class ShipClass(
    val displayName: String,
    val role: String,
    val baseHull: Float,
    val baseShield: Float,
    val baseEnergy: Float,
    val baseSpeed: Float,
    val baseTurnRate: Float,
    val cost: Int,
    val description: String
) {
    ARTEMIS_SCOUT(
        displayName = "NASA Artemis Interceptor",
        role = "Caza Táctico Ligero",
        baseHull = 550f,
        baseShield = 400f,
        baseEnergy = 120f,
        baseSpeed = 480f,
        baseTurnRate = 4.2f,
        cost = 0,
        description = "Ágil nave de vanguardia con propulsión iónica rápida y capacidad de esquiva orbital."
    ),
    ORION_CRUISER(
        displayName = "NASA Orion Battlecruiser",
        role = "Crucero de Asalto Pesado",
        baseHull = 1200f,
        baseShield = 900f,
        baseEnergy = 180f,
        baseSpeed = 340f,
        baseTurnRate = 2.8f,
        cost = 1500,
        description = "Blindaje reforzado de titanio y cañones gauss dobles para combates de larga duración."
    ),
    PROMETHEUS_MOTHERSHIP(
        displayName = "NASA Prometheus Mothership",
        role = "Nave Nodriza de Comando",
        baseHull = 2400f,
        baseShield = 1800f,
        baseEnergy = 260f,
        baseSpeed = 260f,
        baseTurnRate = 2.0f,
        cost = 4000,
        description = "Colosal fortaleza orbital con torretas de defensa perimetral y bahías de recarga de plasma."
    ),
    DAEDALUS_TITAN(
        displayName = "NASA Daedalus Dreadnought",
        role = "Titán de Espacio Profundo",
        baseHull = 3800f,
        baseShield = 2800f,
        baseEnergy = 350f,
        baseSpeed = 220f,
        baseTurnRate = 1.6f,
        cost = 8000,
        description = "Superestructura insignia de la NASA diseñada para erradicar flotas alienígenas y resistir campos gravitatorios intensos."
    )
}

enum class WeaponType(
    val displayName: String,
    val damage: Float,
    val fireRate: Float, // shots per sec
    val energyCost: Float,
    val projectileSpeed: Float,
    val color: Long,
    val description: String
) {
    ION_PULSE(
        displayName = "Láser de Iones Rápidos",
        damage = 38f,
        fireRate = 6.5f,
        energyCost = 6f,
        projectileSpeed = 900f,
        color = 0xFF00E5FF,
        description = "Disparos veloces de alta precisión diseñados para colapsar escudos energéticos enemigos."
    ),
    DUAL_GAUSS(
        displayName = "Cañón Gauss Cinético Doble",
        damage = 85f,
        fireRate = 3.2f,
        energyCost = 10f,
        projectileSpeed = 820f,
        color = 0xFFFFB300,
        description = "Proyectiles hiperdensos que desgarran el blindaje físico de las naves nodrizas."
    ),
    HEAVY_PLASMA(
        displayName = "Batería de Plasma Térmico",
        damage = 160f,
        fireRate = 1.8f,
        energyCost = 22f,
        projectileSpeed = 700f,
        color = 0xFFFF2A55,
        description = "Orbes de plasma sobrecalentado que causan daño masivo y quemaduras en el casco."
    ),
    TACHYON_LANCE(
        displayName = "Lanza Taquiónica Cuántica",
        damage = 320f,
        fireRate = 0.9f,
        energyCost = 35f,
        projectileSpeed = 1200f,
        color = 0xFFD500F9,
        description = "Haz cuántico experimental capaz de penetrar múltiples objetivos en línea recta."
    )
}

enum class OrdnanceType(
    val displayName: String,
    val damage: Float,
    val cooldown: Float, // seconds
    val energyCost: Float,
    val description: String
) {
    PHOTON_TORPEDO(
        displayName = "Torpedos Fotónicos Teledirigidos",
        damage = 350f,
        cooldown = 4.0f,
        energyCost = 30f,
        description = "Misiles autoguiados de fotones con tremendo impacto cinético y radio de explosión."
    ),
    EMP_NOVA(
        displayName = "Pulso EMP Ondulatorio",
        damage = 180f,
        cooldown = 6.5f,
        energyCost = 40f,
        description = "Onda expansiva que paraliza naves enemigas cercanas y descarga sus escudos."
    ),
    MICRO_MISSILES(
        displayName = "Enjambre de Micro-Misiles",
        damage = 420f,
        cooldown = 5.0f,
        energyCost = 35f,
        description = "Lanza una salva de 8 micro-cohetes en abanico que saturan las defensas enemigas."
    ),
    REPAIR_NANITES(
        displayName = "Nanobots de Reparación Inmediata",
        damage = 0f,
        cooldown = 8.0f,
        energyCost = 45f,
        description = "Restaura instantáneamente 400 HP de casco y regenera el escudo de combate."
    )
}

enum class ReactorType(
    val displayName: String,
    val energyBonus: Float,
    val rechargeRate: Float,
    val cost: Int
) {
    FUSION_TOKAMAK("Reactor Tokamak de Fusión", 0f, 25f, 0),
    ANTIMATTER_CORE("Núcleo de Antimateria Pura", 80f, 42f, 1200),
    DARK_MATTER_SIPHON("Sifón de Materia Oscura", 160f, 65f, 2800)
}

enum class ThrusterType(
    val displayName: String,
    val speedMultiplier: Float,
    val maneuverBonus: Float,
    val cost: Int
) {
    VASIMR_ION("Propulsor Iónico VASIMR", 1.0f, 1.0f, 0),
    NUCLEAR_THERMAL("Empuje Térmico Nuclear", 1.25f, 1.15f, 1000),
    WARP_IMPULSE("Motores de Impulso de Curvatura", 1.55f, 1.35f, 2500)
}

enum class ArmorType(
    val displayName: String,
    val hullBonus: Float,
    val damageReduction: Float, // 0.0 to 1.0
    val cost: Int
) {
    TITANIUM_GRAPHENE("Compuesto Titanio-Grafeno", 0f, 0.10f, 0),
    TUNGSTEN_MATRIX("Matriz de Nanotubos de Wolframio", 500f, 0.22f, 1100),
    ABLATIVE_CERAMIC("Blindaje Cerámico Reactivo de Vacío", 1100f, 0.35f, 2600)
}

data class ShipCustomization(
    val shipClass: ShipClass = ShipClass.ARTEMIS_SCOUT,
    val primaryWeapon: WeaponType = WeaponType.ION_PULSE,
    val secondaryOrdnance: OrdnanceType = OrdnanceType.PHOTON_TORPEDO,
    val reactor: ReactorType = ReactorType.FUSION_TOKAMAK,
    val thruster: ThrusterType = ThrusterType.VASIMR_ION,
    val armor: ArmorType = ArmorType.TITANIUM_GRAPHENE,
    val paintColor: Long = 0xFF00E5FF,
    val shipName: String = "NASA Vanguard One"
) {
    val totalMaxHull: Float get() = (shipClass.baseHull + armor.hullBonus)
    val totalMaxShield: Float get() = shipClass.baseShield
    val totalMaxEnergy: Float get() = (shipClass.baseEnergy + reactor.energyBonus)
    val totalSpeed: Float get() = (shipClass.baseSpeed * thruster.speedMultiplier)
    val totalTurnRate: Float get() = (shipClass.baseTurnRate * thruster.maneuverBonus)
}

enum class PowerSystem {
    BALANCED,
    WEAPONS_FOCUS,
    SHIELDS_FOCUS,
    ENGINES_FOCUS
}

data class StoryChoice(
    val id: String,
    val title: String,
    val dilemma: String,
    val optionA: ChoiceOption,
    val optionB: ChoiceOption
)

data class ChoiceOption(
    val text: String,
    val consequence: String,
    val creditsReward: Int,
    val alignmentImpact: String // "HUMANIST", "TACTICAL", "AGGRESSIVE"
)

data class CampaignSector(
    val id: Int,
    val name: String,
    val location: String,
    val description: String,
    val primaryTarget: String,
    val bossLeader: EnemyLeaderInfo,
    val enemyFleetSize: Int,
    val rewardCredits: Int,
    val rewardExoticMatter: Int,
    val storyChoice: StoryChoice? = null,
    val isCompleted: Boolean = false,
    val isUnlocked: Boolean = false
)

data class EnemyLeaderInfo(
    val name: String,
    val title: String,
    val flagshipName: String,
    val faction: String,
    val maxHealth: Float,
    val specialAttack: String,
    val portraitColor: Long,
    val introQuote: String
)

data class SecondaryMission(
    val id: String,
    val title: String,
    val type: MissionType,
    val sectorName: String,
    val description: String,
    val rewardCredits: Int,
    val isCompleted: Boolean = false
)

enum class MissionType {
    ASTEROID_MINING_DEFENSE,
    ORBITAL_SALVAGE_RUN,
    SURVIVOR_ESCORT,
    ALIEN_RAIDER_HUNT
}

enum class GameEnding(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String
) {
    GUARDIAN_OF_SOLAR_SYSTEM(
        title = "Defensor Supremo del Sistema Solar",
        subtitle = "Final Humanista - Victoria Total de la NASA",
        description = "Gracias a tus decisiones estratégicas y rescate de las colonias, la humanidad y las flotas de la NASA establecen una red de defensa permanente, protegiendo la Tierra y los mundos exteriores por siglos venideros.",
        badge = "🛡️ VICTORIA GLORIOSA"
    ),
    QUANTUM_ASCENDANCE(
        title = "Trascendencia Cuántica Intergaláctica",
        subtitle = "Final Tecnológico - Alianza y Simbiosis",
        description = "Al asimilar la tecnología alienígena y perdonar a sus líderes científicos, la NASA desbloquea el viaje por agujeros de gusano interestelares, transformando a la humanidad en una civilización de Tipo II.",
        badge = "🌌 NUEVA ERA ESPACIAL"
    ),
    VOID_DREADNOUGHT(
        title = "Señor de la Flota del Vacío Profundo",
        subtitle = "Final Táctico Bélico - Dominio de Acero",
        description = "Priorizaste el poder de fuego implacable. Las fuerzas invasoras fueron aniquiladas hasta su último vestigio y la Tierra gobierna los sectores planetarios con una flota inquebrantable.",
        badge = "⚔️ CONQUISTA TOTAL"
    )
}

enum class CombatEventType(val tag: String, val defaultColor: Long) {
    TACTICAL_INFO("INFO", 0xFF00E5FF),
    WEAPONS_FIRE("FUEGO", 0xFFFFB300),
    ENEMY_DESTROYED("ELIMINADO", 0xFF00E676),
    SHIELD_WARNING("ESCUDO", 0xFF2979FF),
    HULL_CRITICAL("CRÍTICO", 0xFFFF1744),
    SALVAGE_ACQUIRED("BOTÍN", 0xFFFFD700),
    BOSS_THREAT("JEFE", 0xFFFF1744),
    ORBITAL_EVENT("ÓRBITA", 0xFF80D8FF),
    SYSTEM_DIVERT("SISTEMA", 0xFFD500F9)
}

data class CombatLogEvent(
    val id: Long,
    val timestampSeconds: Float,
    val timeLabel: String,
    val message: String,
    val type: CombatEventType,
    val color: Long = type.defaultColor
)

