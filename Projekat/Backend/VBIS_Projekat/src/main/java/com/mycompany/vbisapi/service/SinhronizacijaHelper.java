package com.mycompany.vbisapi.service;

final class SinhronizacijaHelper {

    private SinhronizacijaHelper() {
    }

    static void rollbackArangoUpis(String opis, String id, Runnable rollback, RuntimeException originalnaGreska) {
        try {
            rollback.run();
            System.out.println("Rollback Arango upisa izvrsen za " + opis + " " + id + ".");
        } catch (RuntimeException rollbackGreska) {
            originalnaGreska.addSuppressed(rollbackGreska);
            System.err.println("Rollback Arango upisa nije uspeo za " + opis + " " + id + ": "
                    + rollbackGreska.getMessage());
        }
    }
}
