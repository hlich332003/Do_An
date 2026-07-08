package com.mycompany.myapp.web.rest.errors;

@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class PhoneNumberAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public PhoneNumberAlreadyUsedException() {
        super(ErrorConstants.PHONE_NUMBER_ALREADY_USED_TYPE, "Phone number is already in use!", "userManagement", "phoneexists");
    }
}
