package com.banking.security;

/**
 * Strategy interface for delivering One-Time Passwords (OTPs) to users.
 * Pluggable architecture allows seamless replacement with Twilio, AWS SNS, SendGrid, etc.
 */
public interface OtpDeliveryService {

    /**
     * Dispatches the OTP to the recipient.
     *
     * @param destination the recipient identifier (email address or phone number)
     * @param otp the plain OTP value generated for delivery
     * @param purpose the context (e.g., "Fund Transfer", "Account Verification")
     */
    void deliverOtp(String destination, String otp, String purpose);

    /**
     * Retrieves the delivery channel name.
     */
    String getChannelName();
}
