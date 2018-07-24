package be.planty.agents.assistant;

public class PairingRequest {

    public final String name;
    public final String verificationCode;
    public final String publicKey;

    public PairingRequest(String name, String verificationCode, String publicKey) {
        this.name = name;
        this.verificationCode = verificationCode;
        this.publicKey = publicKey;
    }
}
