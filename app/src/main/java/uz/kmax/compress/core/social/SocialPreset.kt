package uz.kmax.compress.core.social

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import uz.kmax.compress.R
import uz.kmax.compress.core.compressor.CompressionFormat

enum class SocialPreset(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val format: CompressionFormat,
    val quality: Int,
    val maxWidth: Int?,
    val maxHeight: Int?,
    val targetSizeBytes: Long?,
    val requiresPremium: Boolean
) {
    INSTAGRAM(R.string.social_instagram, R.string.social_instagram_desc, R.drawable.image_instagram, CompressionFormat.JPEG, 90, 1080, 1350, null, false),
    WHATSAPP(R.string.social_whatsapp, R.string.social_whatsapp_desc, R.drawable.image_whatsapp, CompressionFormat.JPEG, 82, 1600, 1600, null, false),
    TELEGRAM(R.string.social_telegram, R.string.social_telegram_desc, R.drawable.image_telegram, CompressionFormat.WEBP_LOSSY, 90, null, null, null, false),
    FACEBOOK(R.string.social_facebook, R.string.social_facebook_desc, R.drawable.image_facebook, CompressionFormat.JPEG, 88, 2048, 2048, null, false),
    TIKTOK(R.string.social_tiktok, R.string.social_tiktok_desc, R.drawable.image_tik_tok, CompressionFormat.JPEG, 90, 1080, 1920, null, true),
    X(R.string.social_x, R.string.social_x_desc, R.drawable.image_x_social, CompressionFormat.JPEG, 88, 1600, 900, null, false),
    PINTEREST(R.string.social_pinterest, R.string.social_pinterest_desc, R.drawable.image_pinterest, CompressionFormat.JPEG, 90, 1000, 1500, null, true),
    LINKEDIN(R.string.social_linkedin, R.string.social_linkedin_desc, R.drawable.image_linkedin, CompressionFormat.JPEG, 90, 1200, 627, null, true),
    EMAIL(R.string.social_email, R.string.social_email_desc, R.drawable.image_mail, CompressionFormat.JPEG, 78, 1600, 1600, 200 * 1024L, false),
    WEBSITE(R.string.social_website, R.string.social_website_desc, R.drawable.image_internet, CompressionFormat.WEBP_LOSSY, 82, 1920, 1920, null, true),
    CUSTOM(R.string.social_custom, R.string.social_custom_desc, R.drawable.image_size, CompressionFormat.JPEG, 85, null, null, null, true)
}
