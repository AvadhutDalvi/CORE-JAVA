package com.banking.security;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Explicitly labeled DEVELOPMENT-ONLY OTP delivery mechanism.
 * In a production banking environment, this must be substituted with an SMS/Email Gateway
 * such as Twilio, AWS SNS, SendGrid, or internal bank notification infrastructure.
 */
public class SimulatedOtpDeliveryService implements OtpDeliveryService {

    private static final SimulatedOtpDeliveryService INSTANCE = new SimulatedOtpDeliveryService();
    private final List<BiConsumer<String, String>> listeners = new CopyOnWriteArrayList<>();
    private volatile String lastDeliveredOtp;
    private volatile String lastDestination;

    private SimulatedOtpDeliveryService() {}

    public static SimulatedOtpDeliveryService getInstance() {
        return INSTANCE;
    }

    @Override
    public void deliverOtp(String destination, String otp, String purpose) {
        this.lastDeliveredOtp = otp;
        this.lastDestination = destination;

        // Print explicitly labeled development notification to system console
        System.out.println("===============================================================");
        System.out.println("[DEVELOPMENT SIMULATED OTP DISPATCH]");
        System.out.println("Purpose: " + purpose);
        System.out.println("Simulated Destination: " + destination);
        System.out.println("Security OTP: " + otp);
        System.out.println("Notice: For production, integrate with Twilio/AWS/SendGrid API.");
        System.out.println("===============================================================");

        for (BiConsumer<String, String> listener : listeners) {
            try {
                listener.accept(destination, otp);
            } catch (Exception e) {
                System.err.println("Error notifying OTP listener: " + e.getMessage());
            }
        }
    }

    @Override
    public String getChannelName() {
        return "Simulated Dev Channel (Academic/Local)";
    }

    public void addListener(BiConsumer<String, String> listener) {
        listeners.add(listener);
    }

    public void removeListener(BiConsumer<String, String> listener) {
        listeners.remove(listener);
    }

    public String getLastDeliveredOtp() {
        return lastDeliveredOtp;
    }

    public String getLastDestination() {
        return lastDestination;
    }
}
