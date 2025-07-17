package com.waoap.addressbook.utils;

import javafx.scene.control.TextFormatter;

public class MTextFormatter {
    public static TextFormatter<String> getNameFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() < 20) {
                return change;
            } else {
                return null;
            }
        });
    }

    public static TextFormatter<String> getTelephoneFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*") && change.getControlNewText().length() <= 11) {
                return change;
            } else {
                return null;
            }
        });
    }

    public static TextFormatter<String> getEmailFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() < 30) {
                return change;
            } else {
                return null;
            }
        });
    }

    public static TextFormatter<String> getCommonFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() < 40) {
                return change;
            } else {
                return null;
            }
        });
    }
}
