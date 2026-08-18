package uz.kmax.compress.core.monetization

import android.util.Base64
import com.android.billingclient.api.Purchase
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

import uz.kmax.compress.BuildConfig

@Singleton
class PurchaseVerifier @Inject constructor() {

    fun isPurchaseValid(purchase: Purchase): Boolean {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return false
        }
        
        val publicKey = BuildConfig.BILLING_PUBLIC_KEY
        
        // NO VERIFICATION CONFIGURATION -> SAFE FAILURE
        if (publicKey.isEmpty()) {
            android.util.Log.e("PurchaseVerifier", "CRITICAL: Purchase verification failed because public key is missing.")
            return false
        }
        
        return verifySignature(publicKey, purchase.originalJson, purchase.signature)
    }

    private fun verifySignature(base64PublicKey: String, signedData: String, signature: String): Boolean {
        if (signedData.isEmpty() || base64PublicKey.isEmpty() || signature.isEmpty()) {
            return false
        }
        return try {
            val keyBytes = Base64.decode(base64PublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
            
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(signedData.toByteArray())
            sig.verify(Base64.decode(signature, Base64.DEFAULT))
        } catch (e: Exception) {
            false
        }
    }
}
