package lenala.azure.gradle.webapp.model;

import com.microsoft.azure.management.appservice.PricingTier;
import org.gradle.api.GradleException;

import java.util.HashMap;
import java.util.Map;

public enum PricingTierEnum {
    F1("F1"),
    f1("F1"),
    D1("D1"),
    d1("D1"),
    B1("B1"),
    b1("B1"),
    B2("B2"),
    b2("B2"),
    B3("B3"),
    b3("B3"),
    S1("S1"),
    s1("S1"),
    S2("S2"),
    s2("S2"),
    S3("S3"),
    s3("S3"),
    P1V2("P1V2"),
    p1v2("P1V2"),
    p1V2("P1V2"),
    P1v2("P1V2"),
    P2V2("P2V2"),
    P2v2("P2V2"),
    p2V2("P2V2"),
    p2v2("P2V2"),
    P3V2("P3V2"),
    p3V2("P3V2"),
    P3v2("P3V2"),
    p3v2("P3V2");

    private final String pricingTier;
    private static final Map<String, PricingTier> pricingTierMap = new HashMap<>();

    static {
        pricingTierMap.put("F1", PricingTier.FREE_F1);
        pricingTierMap.put("D1", PricingTier.SHARED_D1);
        pricingTierMap.put("B1", PricingTier.BASIC_B1);
        pricingTierMap.put("B2", PricingTier.BASIC_B2);
        pricingTierMap.put("B3", PricingTier.BASIC_B3);
        pricingTierMap.put("S1", PricingTier.STANDARD_S1);
        pricingTierMap.put("S2", PricingTier.STANDARD_S2);
        pricingTierMap.put("S3", PricingTier.STANDARD_S3);
        pricingTierMap.put("P1V2", PricingTier.PREMIUM_P1V2);
        pricingTierMap.put("P2V2", PricingTier.PREMIUM_P2V2);
        pricingTierMap.put("P3V2", PricingTier.PREMIUM_P3V2);
    }

    PricingTierEnum(final String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public PricingTier toPricingTier() throws GradleException {
        if (pricingTierMap.containsKey(pricingTier)) {
            return pricingTierMap.get(pricingTier);
        } else {
            throw new GradleException("Unknown value of the pricingTier, please correct it in build.gradle.");
        }
    }
}
