package com.example.campaign

import com.example.model.*

object CampaignData {
    val initialSectors: List<CampaignSector> = listOf(
        CampaignSector(
            id = 1,
            name = "Sector Alfa: Órbita Terrestre",
            location = "Altitud Geoestacionaria 35,786 km",
            description = "Una vanguardia alienígena desconocida ha atravesado el Cinturón de Kuiper y asedia la red defensiva orbital de la NASA. Tu escuadrón debe interceptar a los cazas y derribar la nave capitana del General Xyrek.",
            primaryTarget = "Destruir la Fragata Insignia 'Void Harvester'",
            bossLeader = EnemyLeaderInfo(
                name = "General Xyrek",
                title = "Vanguardia de la Flota del Vacío",
                flagshipName = "Void Harvester X-1",
                faction = "Legión del Vacío",
                maxHealth = 1800f,
                specialAttack = "Descarga de Misiles de Iones Cuánticos",
                portraitColor = 0xFFFF1744,
                introQuote = "¡Criaturas terrestres! Vuestro sol pronto alimentará nuestras forjas estelares. ¡Fuego a discreción!"
            ),
            enemyFleetSize = 8,
            rewardCredits = 1200,
            rewardExoticMatter = 25,
            storyChoice = StoryChoice(
                id = "choice_s1",
                title = "Decisión Crítica: Protocolo de Emergencia Terrestre",
                dilemma = "Un crucero de evacuación civil está siendo atacado mientras la matriz de satélites militares de la NASA pierde energía. Solo puedes salvar uno con tus escudos.",
                optionA = ChoiceOption(
                    text = "Proteger el Crucero Civil de la NASA",
                    consequence = "Salvaste a 1,200 científicos y colonos. La moral humana se eleva.",
                    creditsReward = 400,
                    alignmentImpact = "HUMANIST"
                ),
                optionB = ChoiceOption(
                    text = "Asegurar la Matriz de Defensa Militar",
                    consequence = "Potenciaste las redes de defensa global pero la nave civil sufrió bajas.",
                    creditsReward = 800,
                    alignmentImpact = "TACTICAL"
                )
            ),
            isUnlocked = true,
            isCompleted = false
        ),
        CampaignSector(
            id = 2,
            name = "Sector Beta: Cañones de Marte y Fobos",
            location = "Meseta de Tharsis y Fobos",
            description = "Las fuerzas invasoras han establecido una bio-base en la órbita de Fobos, desatando bio-drones pesados dirigidos por la Reina Vespera. La gravedad irregular de Marte añade riesgo táctico a cada maniobra orbital.",
            primaryTarget = "Erradicar al Bio-Leviatán de Fobos",
            bossLeader = EnemyLeaderInfo(
                name = "Reina Vespera",
                title = "Matriarca del Enjambre Biomecánico",
                flagshipName = "Bio-Leviatán Hive-Omega",
                faction = "Enjambre Quimérico",
                maxHealth = 2900f,
                specialAttack = "Salva de Torpedos de Plasma Ácido",
                portraitColor = 0xFF00E676,
                introQuote = "Sentimos el silicio y la carne de vuestras naves. Seréis asimilados al gran caldo estelar."
            ),
            enemyFleetSize = 12,
            rewardCredits = 2200,
            rewardExoticMatter = 45,
            storyChoice = StoryChoice(
                id = "choice_s2",
                title = "Dilemma Táctico: Muestra Biotecnológica",
                dilemma = "Has capturado una cápsula bio-orgánica de la Reina Vespera. El alto mando de la NASA pide investigarla de inmediato.",
                optionA = ChoiceOption(
                    text = "Sintetizar Antídoto y Escudos Biológicos",
                    consequence = "Desbloqueas blindaje adaptativo y comprensión de la biología alienígena.",
                    creditsReward = 600,
                    alignmentImpact = "HUMANIST"
                ),
                optionB = ChoiceOption(
                    text = "Convertir la Bio-Materia en Ojivas Destructivas",
                    consequence = "Obtienes ojivas de plasma con 25% más de poder de aniquilación.",
                    creditsReward = 1100,
                    alignmentImpact = "AGGRESSIVE"
                )
            ),
            isUnlocked = false,
            isCompleted = false
        ),
        CampaignSector(
            id = 3,
            name = "Sector Gamma: Cinturón de Asteroides Ceres",
            location = "Campo de Escombros y Minería de Ceres",
            description = "Navega a través de densos campos de asteroides en órbita caótica. El señor de la guerra Malakor embosca a las naves de suministro de la NASA con acorazados de asedio pesados.",
            primaryTarget = "Destruir el Acorazado de Asedio 'Ira Estelar'",
            bossLeader = EnemyLeaderInfo(
                name = "Warmaster Malakor",
                title = "Comandante de las Legiones de Acero",
                flagshipName = "Dreadnought Ira Estelar",
                faction = "Cruzada de Hierro",
                maxHealth = 4200f,
                specialAttack = "Cañón Magnético Planetario de Rayo Continuo",
                portraitColor = 0xFFFF6D00,
                introQuote = "¡Vuestro metal es frágil y vuestra ciencia primitiva! ¡Que los asteroides sean vuestra tumba!"
            ),
            enemyFleetSize = 15,
            rewardCredits = 3500,
            rewardExoticMatter = 70,
            storyChoice = StoryChoice(
                id = "choice_s3",
                title = "Emboscada en el Asteroide Minero 433",
                dilemma = "Un transporte de combustible aliado está cercado en un campo de escombros denso.",
                optionA = ChoiceOption(
                    text = "Rescate Táctico con Maniobra Slingshot Orbital",
                    consequence = "Salvaste a los pilotos e integraste sus tanques de combustible de curvatura.",
                    creditsReward = 900,
                    alignmentImpact = "HUMANIST"
                ),
                optionB = ChoiceOption(
                    text = "Detonar los Asteroides para Aniquilar a la Flota Enemiga",
                    consequence = "Destruiste toda la flotilla enemiga de un solo golpe táctico implacable.",
                    creditsReward = 1500,
                    alignmentImpact = "TACTICAL"
                )
            ),
            isUnlocked = false,
            isCompleted = false
        ),
        CampaignSector(
            id = 4,
            name = "Sector Delta: Abismo Helado de Europa (Júpiter)",
            location = "Pozo Gravitatorio Joviano y Grieta de Europa",
            description = "Bajo las auroras masivas de Júpiter y su inmenso tirón gravitacional, el Arquitecto Cuántico Zhor manipula anomalías espaciotemporales con portales de salto.",
            primaryTarget = "Neutralizar el Núcleo Cuántico de Zhor",
            bossLeader = EnemyLeaderInfo(
                name = "Arquitecto Zhor",
                title = "Mente Suprema del Nexus Sintético",
                flagshipName = "Quantum Prism Sovereign",
                faction = "Nexus Cuántico",
                maxHealth = 5800f,
                specialAttack = "Desfase Cuántico y Salto Hiperespacial Ofensivo",
                portraitColor = 0xFFD500F9,
                introQuote = "La materia orgánica es obsoleta. Vuestra resistencia desafía la probabilidad matemática."
            ),
            enemyFleetSize = 18,
            rewardCredits = 5000,
            rewardExoticMatter = 110,
            storyChoice = StoryChoice(
                id = "choice_s4",
                title = "El Secreto del Prisma Cuántico",
                dilemma = "El núcleo de Zhor contiene la fórmula completa del motor hiperespacial cuántico ilimitado.",
                optionA = ChoiceOption(
                    text = "Firmar Tratado de Paz y Compartir el Conocimiento",
                    consequence = "Inicias la diplomacia interplanetaria más avanzada de la historia.",
                    creditsReward = 1200,
                    alignmentImpact = "HUMANIST"
                ),
                optionB = ChoiceOption(
                    text = "Apropiarse del Núcleo para Armamento Defensivo Militar",
                    consequence = "La Tierra se convierte en la potencia armamentística hegemónica del cuadrante.",
                    creditsReward = 2200,
                    alignmentImpact = "AGGRESSIVE"
                )
            ),
            isUnlocked = false,
            isCompleted = false
        ),
        CampaignSector(
            id = 5,
            name = "Sector Omega: Anillos de Saturno y Horizonte de Titán",
            location = "Horizonte del Agujero de Gusano en Titán",
            description = "La batalla final de la humanidad. Toda la flota de la NASA se reúne en formación de cuña frente a la superestructura Titán del Vacío. Una gravedad extrema y rayos cósmicos azotan el campo de batalla.",
            primaryTarget = "Destruir al Titán Supremo y Salvar a la Humanidad",
            bossLeader = EnemyLeaderInfo(
                name = "Singularidad Suprema",
                title = "Monarca de la Devastación Cósmica",
                flagshipName = "Titan Flagship Eclipse",
                faction = "Imperio del Vacío Profundo",
                maxHealth = 8500f,
                specialAttack = "Onda de Micro-Singularidad Gravitacional",
                portraitColor = 0xFFFF1744,
                introQuote = "El silencio del cosmos reclama su orden natural. ¡Contemplad vuestro ocaso estelar!"
            ),
            enemyFleetSize = 25,
            rewardCredits = 10000,
            rewardExoticMatter = 250,
            storyChoice = StoryChoice(
                id = "choice_s5",
                title = "El Destino del Sistema Solar",
                dilemma = "La Singularidad colapsa abriendo un vórtice gravitacional. Debes ordenar el protocolo final a la flota.",
                optionA = ChoiceOption(
                    text = "Establecer Alianza y Trascendencia Científica",
                    consequence = "Canalizas la energía para abrir corredores habitables hacia nuevas galaxias.",
                    creditsReward = 3000,
                    alignmentImpact = "HUMANIST"
                ),
                optionB = ChoiceOption(
                    text = "Sobrecargar Núcleos y Sellar el Vórtice con Fuego",
                    consequence = "Aniquilas para siempre toda amenaza extraterrestre del espacio profundo.",
                    creditsReward = 3000,
                    alignmentImpact = "AGGRESSIVE"
                )
            ),
            isUnlocked = false,
            isCompleted = false
        )
    )

    val secondaryMissions: List<SecondaryMission> = listOf(
        SecondaryMission(
            id = "sec_1",
            title = "Operación Rompehielos: Limpieza de Ceres",
            type = MissionType.ASTEROID_MINING_DEFENSE,
            sectorName = "Cinturón de Asteroides",
            description = "Destruye 12 asteroides ricos en minerales y neutraliza a los exploradores piratas que interceptan a las barcazas mineras de la NASA.",
            rewardCredits = 1400
        ),
        SecondaryMission(
            id = "sec_2",
            title = "Extracción de Caja Negra: Fragata Artemis III",
            type = MissionType.ORBITAL_SALVAGE_RUN,
            sectorName = "Órbita Marciana",
            description = "Recupera los datos de telemetría de una fragata destruida cerca de Fobos antes de que caiga en la atmósfera de Marte.",
            rewardCredits = 1800
        ),
        SecondaryMission(
            id = "sec_3",
            title = "Escolta de Convoy Científico James Webb II",
            type = MissionType.SURVIVOR_ESCORT,
            sectorName = "Punto de Lagrange L2",
            description = "Protege al observatorio estelar móvil de la NASA contra salvas de misiles de asedio alienígenas.",
            rewardCredits = 2500
        ),
        SecondaryMission(
            id = "sec_4",
            title = "Caza de Recompensas: El Corsario de Tritón",
            type = MissionType.ALIEN_RAIDER_HUNT,
            sectorName = "Anillos Exteriores",
            description = "Localiza y destruye al temido crucero mercenario 'Sombra Helada' equipado con camuflaje óptico.",
            rewardCredits = 3200
        )
    )
}
