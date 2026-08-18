package uz.kmax.compress.core.compressor

sealed interface CompressionQuality {
    val value: Int

    data object Low : CompressionQuality {
        override val value: Int = 30
    }

    data object Medium : CompressionQuality {
        override val value: Int = 60
    }

    data object High : CompressionQuality {
        override val value: Int = 80
    }

    data object Maximum : CompressionQuality {
        override val value: Int = 95
    }

    data class Custom(override val value: Int) : CompressionQuality {
        init {
            require(value in 0..100) { "Quality must be between 0 and 100" }
        }
    }
}
