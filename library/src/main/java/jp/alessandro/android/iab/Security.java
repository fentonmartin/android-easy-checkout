/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.alessandro.android.iab;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;

import jp.alessandro.android.iab.logger.Logger;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    @SuppressWarnings("checkstyle:linelength")
    private static final String PRIVATE_KEY_BASE_64_ENCODED = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDtIS1XtZPW8kp1LV8GCyRiT5zyPphRrqTPw3AtsPsSQoaH7ShxKax17gF7CtAOKMcLTPoLGzezwqSzYkLvk1NlS9FBE3lPX0+jajBNdOuRPn5mHae3n/SWPtGczIHqpgx5V5sOHihSaPhiQt1DdCM6kuMZ6nXGMi6c68mukyI2RC5GXcQ0FuTARsMrNKq5dcyeCN+THY/Id+KtsTZ0NVeQbzkbnjYpIF84cXUBCkQ7uGJGDPxvklO5J7ig51hzXVYbhs3GculxR6HzHAT23FyKkOvPpxQV9voPeUvzM8jPJnGLAivT4bQ7uKx768gxo/Qk4Dz3V6qu4FUQjtY8LJPRAgMBAAECggEATUdYrZLhYVWI6nMk2qVa8Ccd8Nxxa31M/OCmeF2LFUJU8YtaeLaqG6y7EsxNTbAAXjBx9JikKJMwdb16LvWGYia5RUoBaNqY65q5rySBeM4zBzh25iLc5PIIAd+sHzqKKilgwNMXNPQ8rlk4HrmEmZwxIssEItlL05wMGDafGaux8OVBlLqRMIGAQjaKjGc66SgFxkiiiolUlQRcvm7szXC/wXi28f7JNImFXeH5FwhHB41fbHF7eHci2/9PRCTI6pawiiSVJqj3g0A7TNuYXSB9AtZdHX1iOr72N33P/MvWwnapGXkKDm6TX+my6XTQY0qZc1MtPlEuWKMUWsgweQKBgQD/DhNkBhaY8DpOflgksmJFumG2po8CK9eGQreUs/NoE1nKxItQAVLjohVd8+aoTuiG2IUCX9Pe5OYOAOjNQ4owvFx5KBty6lhGXaOOrRUbfRtn3PYTgDsc+n75AIkn6UyabaDEIY8EmyC8wr3PX/fEod5vf1J+mKSMLn13gj1KXwKBgQDuAhlMeYMXA0sJyUhwCKMa6dnBEoxKNjHDclLDfpPVf47ogA+P2MTvKnOn7EfwfLmiU/KqbYM+8KgJRyaofMyWvoIB873PI0G/l/d8DW3rMv1K8zPLrgknUpKDMt0rFzxlSm5tYFwvSTseOUZLPvEJLcYUKfuf2uWk82gdI8ovzwKBgFBYeclHlbTF8Egrys58lzKJ/SARpfk0IGe9+qDQczv05JNYiN5CHH9y3rJDFAUvHlbkPDo8P7z2dHYy2SNYRF8H50WPWd5AbmB0PQLECWMobQqx856/BWAilP8RqSM2fhgjssI2JBx6VbzAyBRckeuSZkTPYghZQ3SZbJLKJ06XAoGAJy6XRZy3dQFoyAqn7zGs0FBxNbS8/bagSKG4eFCNO9eNCj+S0EaKXSkq8xkV2sRdtxiE2YO/2Iu7zhM1jQVGlQZ11qZut/wA5e65omV/k/nH8x/Ihh53iU6xqgGkoWRo3+/57+2uH2a54cbiCJ8rBSzQ8B7dOrrJlXcwy6NJtMcCgYAdM7gR+aVFXsedq1QEXvpnggua70VPu56xHJ8GCh1zrDu9UubkZQ9bB74kNakzvhGBmLRs+Grp6wLIm66C4MgmlUbxDnOWQLkmHvBDVn9z60RE/MTxADLqlGWDkuUpSZHN1WSfKlRpj/VeLVpAREWYBSXqjWZA5sD/GKG8l6OTJg==";

    private Security() {
    }

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the verified purchase. The data is in JSON format and signed
     * with a private key. The data also contains the purchase state
     * and product ID of the purchase.
     *
     * @param purchaseData    the purchase data used for debug validation.
     * @param logger          the logger to use for printing events
     * @param base64PublicKey rsa public key generated by Google Play Developer Console
     * @param signedData      the signed JSON string (signed, not encrypted)
     * @param signature       the signature for the data, signed with the private key
     */
    public static boolean verifyPurchase(String purchaseData, Logger logger, String base64PublicKey,
                                         String signedData, String signature) {

        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey) ||
                TextUtils.isEmpty(signature)) {

            if (isTestingStaticResponse(purchaseData)) {
                logger.e(Logger.TAG, String.format("Testing static response: %s", purchaseData));
                return true;
            }
            logger.e(Logger.TAG, "Purchase verification failed: missing data.");
            return false;
        }
        PublicKey key = Security.generatePublicKey(logger, base64PublicKey);
        return Security.verify(logger, key, signedData, signature);
    }

    /**
     * In case of tests it will return true because test purchases doesn't have a signature
     * See https://developer.android.com/google/play/billing/billing_testing.html
     *
     * @param purchaseData the data used to purchase
     */
    private static boolean isTestingStaticResponse(String purchaseData) {
        if (BuildConfig.DEBUG &&
                (purchaseData.equals("android.test.purchased")
                        || purchaseData.equals("android.test.canceled")
                        || purchaseData.equals("android.test.refunded")
                        || purchaseData.equals("android.test.item_unavailable"))) {
            return true;
        }
        return false;
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param logger           the logger to use for printing events
     * @param encodedPublicKey rsa public key generated by Google Play Developer Console
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    private static PublicKey generatePublicKey(Logger logger, String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param logger     the logger to use for printing events
     * @param publicKey  rsa public key generated by Google Play Developer Console
     * @param signedData signed data from server
     * @param signature  server signature
     * @return true if the data and signature match
     */
    private static boolean verify(Logger logger, PublicKey publicKey,
                                  String signedData, String signature) {
        try {
            byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);

            sig.initVerify(publicKey);
            sig.update(signedData.getBytes("UTF-8"));
            if (!sig.verify(signatureBytes)) {
                logger.e(Logger.TAG, "Signature verification failed.");
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
        } catch (InvalidKeyException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
        } catch (SignatureException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            logger.e(Logger.TAG, e.getMessage(), e);
        }
        return false;
    }

    /**
     * Sign some data for testing
     *
     * @param signedData
     * @return
     */
    static String signData(String signedData) {
        String baseEncodedSign = null;
        try {
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decode(PRIVATE_KEY_BASE_64_ENCODED.getBytes("UTF-8"), Base64.DEFAULT));
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            PrivateKey privateKey = kf.generatePrivate(spec);

            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initSign(privateKey);
            sig.update(signedData.getBytes("UTF-8"));
            baseEncodedSign = Base64.encodeToString(sig.sign(), Base64.DEFAULT);

            Log.d(Logger.TAG, String.format(Locale.ENGLISH, "BaseEncodedSign: %s", baseEncodedSign));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return baseEncodedSign;
    }
}