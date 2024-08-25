package org.example.shared;

public enum Action {
    NEW,
    AMEND,
    CANCEL;

    public static Action from(final String actionString) {
        if (NEW.toString().equals(actionString)) {
            return NEW;
        } else if (AMEND.toString().equals(actionString)) {
            return AMEND;
        } else {
            return CANCEL;
        }
    }
}
