package dev.mobase.purchases.google.util

import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import dev.mobase.purchases.model.Price
import dev.mobase.purchases.model.Product
import dev.mobase.purchases.model.SubscriptionPeriod

internal fun ProductDetails.toProduct(): Product? = when (productType) {
    ProductType.INAPP -> {
        val offer = oneTimePurchaseOfferDetails ?: return null
        Product(
            id = productId,
            title = title,
            description = description,
            type = Product.Type.IN_APP,
            price = Price(
                amountMicros = offer.priceAmountMicros,
                currencyCode = offer.priceCurrencyCode,
                formattedAmount = offer.formattedPrice
            )
        )
    }

    ProductType.SUBS -> {
        val basePlan = subscriptionOfferDetails?.firstOrNull { it.offerId == null } ?: return null

        val offerDetails = subscriptionOfferDetails?.firstOrNull() ?: return null
        val pricingPhase = basePlan.pricingPhases.pricingPhaseList.lastOrNull() ?: return null

        val subscriptionPeriod = basePlan
            .pricingPhases
            .pricingPhaseList
            .first()
            .billingPeriod
            .let(SubscriptionPeriod::parse)

        val trialPeriod = offerDetails.pricingPhases.pricingPhaseList
            .firstOrNull { it.priceAmountMicros == 0L }
            ?.billingPeriod
            ?.let(SubscriptionPeriod::parse)

        Product(
            id = productId,
            title = title,
            description = description,
            type = Product.Type.SUBSCRIPTION,
            price = Price(
                amountMicros = pricingPhase.priceAmountMicros,
                currencyCode = pricingPhase.priceCurrencyCode,
                formattedAmount = pricingPhase.formattedPrice
            ),
            subscriptionInfo = Product.SubscriptionInfo(
                basePlanId = basePlan.basePlanId,
                period = subscriptionPeriod,
                offerToken = offerDetails.offerToken,
                trialPeriod = trialPeriod
            )
        )
    }

    else -> null
}
