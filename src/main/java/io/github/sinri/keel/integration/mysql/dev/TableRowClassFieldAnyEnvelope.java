package io.github.sinri.keel.integration.mysql.dev;


import org.jetbrains.annotations.NotNull;

public class TableRowClassFieldAnyEnvelope {
    private final String envelopePackage;
    private final String envelopeName;

    public TableRowClassFieldAnyEnvelope(@NotNull String envelopeName, @NotNull String envelopePackage) {
        this.envelopePackage = envelopePackage;
        this.envelopeName = envelopeName;
    }

    public String buildCallClassMethodCode(@NotNull String parameter) {
        return "new " + envelopePackage + "." + envelopeName + "().decrypt(" + parameter + ");";
    }
}
