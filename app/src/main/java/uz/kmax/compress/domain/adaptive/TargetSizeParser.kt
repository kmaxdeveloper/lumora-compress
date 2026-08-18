package uz.kmax.compress.domain.adaptive
object TargetSizeParser { fun parse(value: String): Long? { val text=value.trim().uppercase(); val number=text.substringBefore(' ').replace(',','.').toDoubleOrNull() ?: return null; return when { text.endsWith("MB") -> (number*1024*1024).toLong(); text.endsWith("KB") -> (number*1024).toLong(); else -> null } } }
