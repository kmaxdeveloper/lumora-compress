package uz.kmax.compress.feature.dashboard.model
object PredictionFormatter { fun stars(quality: Int) = "★".repeat((quality / 20).coerceIn(1, 5)) + "☆".repeat((5 - quality / 20).coerceAtLeast(0)) }
