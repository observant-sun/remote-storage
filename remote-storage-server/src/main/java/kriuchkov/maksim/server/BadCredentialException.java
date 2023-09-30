package kriuchkov.maksim.server;

public class BadCredentialException extends Exception {


    public String getCredential() {
        return credential;
    }

    private String credential;

    BadCredentialException(String whatCredential, String msg) {
        super(msg);
        credential = whatCredential;
    }



}
